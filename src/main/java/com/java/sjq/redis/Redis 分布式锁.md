[Redis分布式锁的正确加锁与解锁方式 - murphy\_gb - 博客园](https://www.cnblogs.com/kyoner/p/12298902.html)



现在的业务场景越来越复杂，使用的架构也就越来越复杂，分布式、高并发已经是业务要求的常态。像腾讯系的不少服务，还有CDN优化、异地多备份等处理。  
说到分布式，就必然涉及到分布式锁的概念，如何保证不同机器不同线程的分布式锁同步呢？

## 实现要点

1.  互斥性，同一时刻，只能有一个客户端持有锁。
2.  防止死锁发生，如果持有锁的客户端因崩溃而没有主动释放锁，也要保证锁可以释放并且其他客户端可以正常加锁。
3.  加锁和释放锁必须是同一个客户端。
4.  容错性，只要redis还有节点存活，就可以进行正常的加锁解锁操作。

## 正确的redis分布式锁实现

#### 错误加锁方式一

##### 设置锁、设置过期不是原子操作

保证互斥和防止死锁，首先想到的使用redis的setnx命令保证互斥，为了防止死锁，锁需要设置一个超时时间。

```java
    public static void wrongLock(Jedis jedis, String key, String uniqueId, int expireTime) {
        Long result = jedis.setnx(key, uniqueId);
        if (1 == result) {
            //如果该redis实例崩溃，那就无法设置过期时间了
            jedis.expire(key, expireTime);
        }
    }
```

在多线程并发环境下，任何非原子性的操作，都可能导致问题。这段代码中，如果设置过期时间时，redis实例崩溃，就无法设置过期时间。如果客户端没有正确的释放锁，那么该锁(永远不会过期)，就永远不会被释放。

#### 错误加锁方式二

##### 设置value为expire。时钟同步、无线程标识

比较容易想到的就是设置值和超时时间为原子原子操作就可以解决问题。那使用setnx命令，将value设置为过期时间不就ok了吗？

```java
    public static boolean wrongLock(Jedis jedis, String key, int expireTime) {
        long expireTs = System.currentTimeMillis() + expireTime;
        // 锁不存在，当前线程加锁成果
        if (jedis.setnx(key, String.valueOf(expireTs)) == 1) {
            return true;
        }

        String value = jedis.get(key);
        //如果当前锁存在,且锁已过期
        if (value != null && NumberUtils.toLong(value) < System.currentTimeMillis()) {
            //锁过期，设置新的过期时间
            String oldValue = jedis.getSet(key, String.valueOf(expireTs));
            if (oldValue != null && oldValue.equals(value)) {
                // 多线程并发下，只有一个线程会设置成功
                // 设置成功的这个线程，key的旧值一定和设置之前的key的值一致
                return true;
            }
        }
        // 其他情况，加锁失败
        return true;
    }
```

乍看之下，没有什么问题。但仔细分析，有如下问题：

1.  value设置为过期时间，就要求各个客户端严格的时钟同步，这就需要使用到同步时钟。即使有同步时钟，分布式的服务器一般来说时间肯定是存在少许误差的。
2.  锁过期时，使用 jedis.getSet虽然可以保证只有一个线程设置成功，但是不能保证加锁和解锁为同一个客户端，因为没有标志锁是哪个客户端设置的嘛。

#### 错误解锁方式一

##### 直接删除key，会删除其它线程的锁

```java
    public static void wrongReleaseLock(Jedis jedis, String key) {
        //不是自己加锁的key，也会被释放
        jedis.del(key);
    }
```

简单粗暴，直接解锁，但是不是自己加锁的，也会被删除，这好像有点太随意了吧！

#### 错误解锁方式二

##### 判断线程和删除操作不是原子性

判断自己是不是锁的持有者，如果是，则只有持有者才可以释放锁。

```java
    public static void wrongReleaseLock(Jedis jedis, String key, String uniqueId) {
        if (uniqueId.equals(jedis.get(key))) {
            // 如果这时锁过期自动释放，又被其他线程加锁，该线程就会释放不属于自己的锁
            jedis.del(key);
        }
    }
```

看起来很完美啊，但是如果你判断的时候锁是自己持有的，这时锁超时自动释放了。然后又被其他客户端重新上锁，然后当前线程执行到jedis.del(key)，这样这个线程不就删除了其他线程上的锁嘛，好像有点乱套了哦！

#### 正确的加解锁方式

基本上避免了以上几种错误方式之外，就是正确的方式了。要满足以下几个条件：

1.  命令必须保证互斥
2.  设置的 key必须要有过期时间，防止崩溃时锁无法释放
3.  value使用唯一id标志每个客户端，保证只有锁的持有者才可以释放锁

加锁直接使用set命令同时设置唯一id和过期时间；其中解锁稍微复杂些，加锁之后可以返回唯一id，标志此锁是该客户端锁拥有；释放锁时要先判断拥有者是否是自己，然后删除，这个需要redis的lua脚本保证两个命令的原子性执行。  
下面是具体的加锁和释放锁的代码：

```java
@Slf4j
public class RedisDistributedLock {
    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    // 锁的超时时间
    private static int EXPIRE_TIME = 5 * 1000;
    // 锁等待时间
    private static int WAIT_TIME = 1 * 1000;

    private Jedis jedis;
    private String key;

    public RedisDistributedLock(Jedis jedis, String key) {
        this.jedis = jedis;
        this.key = key;
    }

    // 不断尝试加锁
    public String lock() {
        try {
            // 超过等待时间，加锁失败
            long waitEnd = System.currentTimeMillis() + WAIT_TIME;
            String value = UUID.randomUUID().toString();
            while (System.currentTimeMillis() < waitEnd) {
                String result = jedis.set(key, value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, EXPIRE_TIME);
                if (LOCK_SUCCESS.equals(result)) {
                    return value;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception ex) {
            log.error("lock error", ex);
        }
        return null;
    }

    public boolean release(String value) {
        if (value == null) {
            return false;
        }
        // 判断key存在并且删除key必须是一个原子操作
        // 且谁拥有锁，谁释放
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = new Object();
        try {
            result = jedis.eval(script, Collections.singletonList(key),
                    Collections.singletonList(value));
            if (RELEASE_SUCCESS.equals(result)) {
                log.info("release lock success, value:{}", value);
                return true;
            }
        } catch (Exception e) {
            log.error("release lock error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        log.info("release lock failed, value:{}, result:{}", value, result);
        return false;
    }
}
```

#### 仍存在的问题（锁过期）

> [(130条消息) 阿里二面：redis分布式锁过期了但业务还没有执行完，怎么办\_架构师小秘圈的博客-CSDN博客](https://blog.csdn.net/g6U8W7p06dCO99fQ3/article/details/120170020?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-120170020-blog-128816902.pc_relevant_3mothn_strategy_recovery&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-120170020-blog-128816902.pc_relevant_3mothn_strategy_recovery&utm_relevant_index=1)