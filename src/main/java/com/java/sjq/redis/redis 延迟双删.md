
[(122条消息) redis数据一致性之延时双删详解\_redis延时双删\_无形风的博客-CSDN博客](https://blog.csdn.net/xukaiqiang123/article/details/113544712)

## 在使用redis时，需要保持redis和数据库数据的一致性，最流行的解决方案之一就是延时双删策略，今天我们就来详细刨析一下

**注意：要知道经常修改的数据表不适合使用redis，因为双删策略执行的结果是把redis中保存的那条数据删除了，以后的查询就都会去查询数据库。所以redis使用的是读远远大于改的数据缓存。**

**1，首先要理解在并发环境下redis数据一致性的问题所在**

```
在多线程并发情况下，假设有两个数据库修改请求，为保证数据库与redis的数据一致性，
修改请求的实现中需要修改数据库后，级联修改redis中的数据。
请求一：1.1修改数据库数据1.2 修改redis数据
请求二：2.1修改数据库数据2.2 修改redis数据
并发情况下就会存在1.1 ---> 2.1 ---> 2.2 ---> 1.2的情况 
（一定要理解线程并发执行多组原子操作执行顺序是可能存在交叉现象的）

此时存在的问题就是：
1.1修改数据库的数据最终保存到了redis中，2.1在1.1之后也修改了数据库数据。
此时出现了redis中数据和数据库数据不一致的情况，在后面的查询过程中就会长时间去先查redis，
从而出现查询到的数据并不是数据库中的真实数据的严重问题。
问题解决：
修改数据库级联修改redis数据改为  修改数据库数据后级联删除redis数据
至于是先执行1.2的redis删除，还是先执行2.2的redis删除，无关紧要。
结果都是redis中数据已被删除。之后的查询就会由于redis中没有数据而去查数据库，
此时即不会存在查询到的数据和数据库的数据不一致的情况。
```

**2，上面详解了redis数据一致性的问题所在，并提供了单删策略来解决问题  
但此时依然存在比较严重的问题。**

```
上面的单删策略情况如下：
修改请求的实现中需要修改数据库后，级联删除redis中的数据。
请求一：1.1修改数据库数据1.2 删除redis数据
请求二：2.1修改数据库数据2.2 删除redis数据

假设现在并发存在一个查询请求
请求三：3.1查询redis中数据   3.2查询数据库数据    3.3 新查到的数据写入redis
（一定要理解带redis的查询请求实现逻辑，先查redis，数据不存在查数据库，
查到的数据写入redis以便以后的查询不去直接查数据库）

此时并发情况下就会存在1.1 ---> 1.2 ---> 3.1 ---> 3.2 ---> 2.1 ---> 2.2 ---> 3.3的情况 

此时存在的问题就是：
此时数据库中的数据保存的是2.1修改后的数据，而redis中保存的数据是3.2中在1.1修改数据后的结果，
此时出现了redis中数据和数据库数据不一致的情况，在后面的查询过程中就会长时间去先查redis，
从而出现查询到的数据并不是数据库中的真实数据的严重问题。
```

**3，上面刨析到了单删策略来解决redis数据一致性存在的问题，下面我们来说双删策略**

```
上面的单删策略存在问题的情况如下：
请求一：1.1修改数据库数据1.2 删除redis数据
请求二：2.1修改数据库数据2.2 删除redis数据
请求三：3.1查询redis中数据   3.2查询数据库数据    3.3 新查到的数据写入redis

添加延时双删策略后的情况
请求一：1.1修改数据库数据1.2 删除redis数据    1.3 延时3--5s再去删除redis中数据
请求二：2.1修改数据库数据2.2 删除redis数据    2.3 延时3--5s再去删除redis中数据
请求三：3.1查询redis中数据     3.2 查询数据库数据    3.3 新查到的数据写入redis

双删策略为什么能解决问题：
因为存在了延时时间，故1.3或2.3 一定是最后执行的一步操作（并发中的延时一定要理解）
延时的根本目的就是为了让程序先把3.3执行完，再去删除redis
```

**4，如何实现延时3–5s的操作**

```
比较好的：   项目整合quartz等定时任务框架，去实现延时3--5s再去执行最后一步任务
比较一般的：  创建线程池，线程池中拿一个线程，线程体中延时3-5s再去执行最后一步任务（不能忘了启动线程）
比较差的：   单独创建一个线程去实现延时执行
```

## 收藏加关注，再来不迷路！！！