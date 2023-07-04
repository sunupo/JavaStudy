# 一、主从同步

## Redis

### 异步：[Redis replication](https://redis.io/docs/management/replication/)

> [Redis 主从同步.md](C:\Users\sunupo\IdeaProjects\JavaStudy\src\main\java\com\java\sjq\redis\Redis 主从同步.md)

Redis 默认使用异步复制，在slave客户端执行周期性任务的时候执行psync

## 同步

客户端可以使用 `WAIT` 命令请求某些数据的同步复制。但是 `WAIT` 只能确保其他 Redis 实例中有指定数量的已确认副本，它不会将一组 Redis 实例转换为具有强一致性的 CP 系统：在故障转移期间，已确认的写入仍可能丢失，具体取决于 Redis 持久性的确切配置。但是，使用 `WAIT` 时，故障事件后丢失写入的可能性大大降低到某些难以触发的故障模式。

> wait命令的作用： 此命令将阻塞当前客户端，直到当前Session连接（主节点上）所有的写命令都被传送到指定数据量的slave节点。 如果到达超时(以毫秒为单位)，则即使尚未完全传送到达指定数量的salve节点，该命令也会返回（成功传送到的节点的个数）。
>
> [Redis主从结构主节点执行写入后wait命令对性能的影响 - 腾讯云开发者社区-腾讯云](https://cloud.tencent.com/developer/article/2076545)

## rocket

> [(125条消息) 【RocketMQ】学习RocketMQ必须要知道的主从同步原理\_rocketmq 主从\_三月是你的微笑的博客-CSDN博客](https://blog.csdn.net/sinat_14840559/article/details/115970738)

### 异步

主从同步由异步的方法完成，大致步骤如下：

#### Master端：

- 监听端口
  org.apache.rocketmq.store.ha.HAService.AcceptSocketService#beginAccept

- 建立连接
  org.apache.rocketmq.store.ha.HAService.AcceptSocketService#run

- 读取slave上报的maxOffset
  org.apache.rocketmq.store.ha.HAConnection.ReadSocketService#run

- 传输数据给slave
  org.apache.rocketmq.store.ha.HAConnection.WriteSocketService#run

#### Slave端：

- 连接master
  org.apache.rocketmq.store.ha.HAService.HAClient#connectMaster

- 定时报告maxOffset给master
  org.apache.rocketmq.store.ha.HAService.HAClient#run

- 接收master传输来的数据
  org.apache.rocketmq.store.ha.HAService.HAClient#processReadEvent

### 同步

#### master发，slave通过`GroupTransferService`的 `HAConnection`去==拉==消息，master等待结果。

如果是同步主从模式，消息发送者将消息刷写到磁盘后，需要继续等待新数据被传输到从服务器，`从服务器`数据的复制是在另外一个线程`HAConnection`中去拉取，所以消息发送者在这里需要等待数据传输的结果。该功能由`GroupTransferService`实现。

```java
        private void doWaitTransfer() {
            synchronized (this.requestsRead) {
                if (!this.requestsRead.isEmpty()) {
                    for (CommitLog.GroupCommitRequest req : this.requestsRead) {
                        boolean transferOK = HAService.this.push2SlaveMaxOffset.get() >= req.getNextOffset();
                        long waitUntilWhen = HAService.this.defaultMessageStore.getSystemClock().now()
                            + HAService.this.defaultMessageStore.getMessageStoreConfig().getSyncFlushTimeout();
                        while (!transferOK && HAService.this.defaultMessageStore.getSystemClock().now() < waitUntilWhen) {
                            this.notifyTransferObject.waitForRunning(1000);
                            transferOK = HAService.this.push2SlaveMaxOffset.get() >= req.getNextOffset();
                        }

                        if (!transferOK) {
                            log.warn("transfer messsage to slave timeout, " + req.getNextOffset());
                        }

                        req.wakeupCustomer(transferOK ? PutMessageStatus.PUT_OK : PutMessageStatus.FLUSH_SLAVE_TIMEOUT);
                    }

                    this.requestsRead.clear();
                }
            }
        }

```



 ==GroupTransferService的职责==<u>**是负责当主从同步复制结束后，通知由于等待HA同步结果而阻塞的消息==发送者线程==。**</u>

- 判断主从同步是否完成的依据是`Slave`中已成功复制的最大偏移量是否大于等于消息生产者发送消息后消息服务端返回下一条消息的起始偏移量，

  - 如果是则 表示主从同步复制已经完成，唤醒消息发送线程，
  - 否则等待 1s再次判断，每一个任务在一批任务中循环判断5次。

- 消息发送者返回有两种情况:

  -  等待超过5s

  - 或GroupTransferService通知主从复制完成，

  - 可以通过 `syncFlushTimeout` 来设置==发送线程==等待超时时间。**Group­TransferService通知主从复制的实现如下**：org.apache.rocketmq.store.ha.HAService#notifyTransferSome。

  - ```java
        public void notifyTransferSome(final long offset) {
            for (long value = this.push2SlaveMaxOffset.get(); offset > value; ) {
                boolean ok = this.push2SlaveMaxOffset.compareAndSet(value, offset);
                if (ok) {
                    this.groupTransferService.notifyTransferSome();
                    break;
                } else {
                    value = this.push2SlaveMaxOffset.get();
                }
            }
        }
    
    ```

    该方法在`Master`收到`Slave`的==拉==取请求后被调用，表示`Slave`当前已同步的偏移量，

    既然 `master` 收到 `slave` 的反馈信息，`master`需要唤醒某些消息==发送者线程==。

    如果`Slave`收到的确认偏移量大于 `push2SlaveMaxOffset`，则更新 push2SlaveMaxOffset，然后唤醒 GroupTransferService线程，各消息发送者线程再次判断自己本次发送的消息是否已经成功复制到从服务器。



> HAService代表broker主机，HAClient代表broker从机。HAClient是HAService的内部类，是一个Runnalble；只有在从机上HAClient实例才可以正常运行；
> [(125条消息) rocketmq的HA主从同步源码解读一（HAService#start）\_orcharddd\_real的博客-CSDN博客](https://blog.csdn.net/u014570939/article/details/123932676)

# 二、故障转移 failover

## redis

### 哨兵机制

```
min-slaves-to-write 1
min-slaves-max-lag 10
```

这两个参数表示至少有 1 个 salve 的与master的同步复制延迟不能超过10s

# 三、AP or CP

## Redis

### AP



## kafka

Kafka提供了一些配置，用户可以根据具体的业务需求，进行不同的配置，使得Kafka满足AP或者CP，或者它们之间的一种平衡。

```
default.replication.factor和min.insync.replicas的区别 

default.replication.factor: 是指分区的总的副本个数，
min.insync.replicas: 是指ISR列表中最少的在线副本的个数（含leader）,
当在线的副本个数小于min.insync.replicas时，生产者发送消息会失败。
default.replication.factor=3，min.insync.replicas=2表示消息总共有3个副本，当在线的副本大于或者等于2时，生产者可以继续发送消息，能够容忍1个备份不可用，否则不能发送消息。
推荐配置：每个分区一个物理存储磁盘，每个分区一个consumer
```



 下面这种配置，保证强一致性，使得Kafka满足CP。

### CP

```swift
replication.factor = 3
min.insync.replicas = 3
acks = all
```

下面的配置，就主要保证可用性，使得Kafka满足AP。

### AP

```swift
replication.factor = 3
min.insync.replicas = 3
acks = 1
```

还有一种配置是公认比较推荐的一种配置，使得Kafka满足的是一种介于AP和CP之间的一种平衡状态。

### balance

```swift
replication.factor = 3
min.insync.replicas = 2
acks = all
```

对于这种配置，其实Kafka不光可以容忍一个节点宕机，同时也可以容忍这个节点和其它节点产生网络分区，它们都可以看成是Kafka的容错（Fault tolerance）机制。

## rocket

### AP

# 四、push or pull

## rocket

RocketMQ没有真正意义的push，都是pull，虽然有push类，但实际底层实现采用的是长轮询机制，即拉取方式



# 删除数据吗

## rocket and kafka

RocketMQ Broker中的消息被消费后会立即删除吗？
同kafka一样是不会的，每条消息都会持久化到CommitLog中，每个Consumer连接到Broker后会维持消费进度信息，当有消息消费后只是当前Consumer的消费进度（CommitLog的offset）更新了。**那么消息会堆积吗？什么时候清理过期消息那么消息会堆积吗？什么时候清理过期消息**

4.6版本默认48小时后会删除不再使用的CommitLog文件

1. 检查这个文件最后访问时间
2. 判断是否大于过期时间
3. 指定时间删除，默认凌晨4点

# Producer发送消息三种模式

## rocket

单向：不需要返回结果
同步
异步	