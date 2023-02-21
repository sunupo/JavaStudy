[MySQL binlog归档日志和redo log事务日志详解](https://blog.csdn.net/qq_56263094/article/details/125956984)

大家好 我是积极向上的湘锅锅💪💪💪

## 1\. redo log事务日志

redo log是事务ACID中持久性的保证，意思就是如果MySQL宕机了，就可以根据redo log进行恢复

**Buffer pool**：  
Buffer pool是InnoDB存储引擎层的内存缓存池，查询都是先从Buffer pool先查询，没有命中再从磁盘加载，这样就减少了IO的消耗  
更新表数据的时候，也是在Buffer pool里面修改，同时，会将修改的记录记录到**redo log buffer**里面，根据选择的刷盘时机刷屏到redo log里面  
![在这里插入图片描述](https://img-blog.csdnimg.cn/7bb689cef34d4bc096599a4e5476e351.png#pic_center)

### 1.1 刷盘时机

InnoDB 存储引擎为 redo log 的刷盘策略提供了 **innodb_flush_log_at_trx_commit** 参数，它支持三种策略：

-   0 ：设置为 0 的时候，表示每次事务提交时不进行刷盘操作
-   1 ：设置为 1 的时候，表示每次事务提交时都将进行刷盘操作（默认值）
-   2 ：设置为 2 的时候，表示每次事务提交时都只把 redo log buffer 内容写入 page cache

**另外，InnoDB 存储引擎有一个后台线程，每隔1秒，就会把 redo log buffer 中的内容写到[文件系统缓存（page cache）](https://blog.csdn.net/CoolBoySilverBullet/article/details/121747994)，然后调用 fsync 刷盘**  
![在这里插入图片描述](https://img-blog.csdnimg.cn/5a49531e11774584a499351326f9f427.png#pic_center)  
**1\. 当设置为0时，表示每次事务提交时不进行刷盘操作：**

![在这里插入图片描述](https://img-blog.csdnimg.cn/4d936062fe4f495ab9f2c5336ee65682.png#pic_center)  
也就是说不管事务有没有提交，全权由后台线程来每隔1秒将redo log buffer写入了page cache，然后调用 fsync 刷盘

也就是说不管事务成功还是失败，如果MySQL宕机了，刚刚写入redo log buffer的数据还没写入到磁盘，那就会产生一秒的数据丢失

**2.设置为 1 的时候，表示每次事务提交时都将进行刷盘操作（默认值）**  
![在这里插入图片描述](https://img-blog.csdnimg.cn/a99e0ab7f7dd4304b8a214c4ed99b618.png#pic_center)  
在上一个为0的策略里面，多加了一个只要提交事务，就会主动刷盘的操作  
那跟事务的特性其实相得益彰，只要提交事务，说明这个数据是有效的，数据也必须保留到磁盘里面

如果事务执行期间MySQL挂了或宕机，这部分日志丢了，但是事务并没有提交，所以日志丢了也不会有损失，一般来说大家都不选择这个默认值，因为刷到磁盘是一次比较昂贵的操作，但是从安全角度考虑的话是最好的，mysqld进程和操作系统崩溃都不会丢失事务。(最安全，最慢)

**3\. 设置为 2 的时候，表示每次事务提交时都只把 redo log buffer 内容写入 page cache**

![在这里插入图片描述](https://img-blog.csdnimg.cn/cb40b5f883724917875008d1a787ee60.png#pic_center)  
如果仅仅只是MySQL挂了不会有任何数据丢失，但是**宕机**可能会有1秒数据的丢失

因为事务提交以后，可以认为redo log日志条目写入到文件系统缓存page cache中，之后交给后台线程和操作系统完成，但是操作系统宕机可能会有1秒数据的丢失

___

## [binlog](https://so.csdn.net/so/search?q=binlog&spm=1001.2101.3001.7020)归档日志

binlog 是逻辑日志，记录内容是语句的原始逻辑，类似于“给 ID=2 这一行的 c 字段加 1”，属于MySQL Server 层，不管用什么存储引擎，只要发生了表数据更新，都会产生 binlog 日志

可以说My[SQL数据库](https://so.csdn.net/so/search?q=SQL%E6%95%B0%E6%8D%AE%E5%BA%93&spm=1001.2101.3001.7020)的数据备份、主备、主主、主从都离不开binlog，需要依靠binlog来同步数据，保证数据一致性

binlog 日志有三种格式，可以通过binlog\_format参数指定。

-   statement
-   row
-   mixed

**statement** ：会记录SQL语句原文，但是会发生比如说 update\_time=now()，这里会获取当前系统时间，直接执行会导致与原库的数据不一致

**row**： 可以解决上诉问题，因为记录的内容不再是简单的SQL语句了，还包含操作的具体数据，但是显而易见的，这种方式比较占空间，恢复和同步的时候更消耗IO资源，影响执行速度

**mixed**： 顾名思义，就是这俩者的结合，MySQL会判断这条SQL语句是否可能引起数据不一致，如果是，就用row格式，否则就用statement格式

### 2.1 写入机制

**binlog为了确保每一个线程不管事务有多大，都可以保证一次性写入，所有每一个线程都有一个独立的内存空间，叫做binlog cache，其作用是在binlog写到里面后，在事务提交的时候再刷盘到binlog文件中**

binlog日志刷盘流程如下

![在这里插入图片描述](https://img-blog.csdnimg.cn/dec60f27bffc4ec3ac767ac123d77937.png#pic_center)  
跟redo log一样，都是先写到page cache里面，然后再通过fsync刷盘到磁盘中

### 2.2 刷盘时机

binlog 也有自己的刷盘时机，总共为三种，可以由参数sync\_binlog控制，默认是0

**1\. 为0时**：表示每次提交事务都只write，由系统自行判断什么时候执行fsync  
![在这里插入图片描述](https://img-blog.csdnimg.cn/f831857c3ceb42f685715b76260d1c93.png#pic_center)  
binlog没有像redo log那样的1s执行fsync的后台线程，所有总的来说性能得到了提升，但同样的，如果机器宕机，page cache里面的 binlog 会丢失

**2\. 为1时**： 每次提交事务都会fsync，跟redo log日志一致  
这样来说的话时比较安全的，在还没有提交事务的过程中如果宕机了，损失的数据影响不大，但是fsync也是一次比较重的操作，在提升安全的同时也损失了性能

**3\. 为N时：** 表示每次提交事务都write，但累积N个事务后才fsync  
![在这里插入图片描述](https://img-blog.csdnimg.cn/3b689c4582874bd48fb337aee847741f.png#pic_center)  
批量的操作在很多地方都有体现，

-   比如kafka的批量消息处理
-   在缓存模式中的异步写入模式，可以消息队列批量写入数据库
-   批量处理数据库操作，比如将三次操作合并为一次

为的都是提高性能，但是如果机器宕机，会丢失最近N个事务的binlog日志

### 2.3 俩阶段提交

redo log（重做日志）让InnoDB存储引擎拥有了崩溃恢复能力。

binlog（归档日志）保证了MySQL集群架构的数据一致性

虽然它们都属于持久化的保证，但是侧重点不同

写入时机也不同，比如redo log可以在事务的过程中不断写入，但是binlog只能在事务提交之后写入

![在这里插入图片描述](https://img-blog.csdnimg.cn/7033b710a6d848d59c59b92cb436001d.png#pic_center)  
同样的，因为写入时机不同，如果出现宕机，那就可能导致redo log与binlog两份日志之间的逻辑不一致

分别是俩种情况

-   先写 redo log 后写 binlog，也就是事务可能还没有提交，系统崩溃了，我们知道即使系统崩溃，但是也可以通过redo log恢复，但是binlog还没有这条数据，就会造成主从不一致
-   先写 binlog 后写 redo log，这个过程说明事务已经提交了，系统崩溃了，binlog里面有数据，但是redo log里面没有，也会发生主从不一致，并且redo log因为不全，即使重新恢复的时候也不是最新的数据

为了解决两份日志之间的逻辑一致问题，InnoDB存储引擎使用**两阶段提交**方案

首先，就是将redo log的写入拆成了两个步骤**prepare和commit**，这就是两阶段提交

![在这里插入图片描述](https://img-blog.csdnimg.cn/ff5ada508468443fa08340f5cd60366c.png#pic_center)

**写入binlog发生异常：**  
这个时候redo log还在prepare状态，也没有对应的binlog文件，说明binlog还没执行写入，所以就会就会回滚该事务

![在这里插入图片描述](https://img-blog.csdnimg.cn/4668ce891acf4dc7841e7a06d9f52db1.png#pic_center)  
**设置commit阶段发生异常：**

**这个时候是可以通过事务id查询的到binlog的**，只是说redo log还处于一个prepare的状态，但是redo log和binlog 还是完整的，就不会回滚事务，就会提交事务恢复数据

>  [Linux---回写机制](https://www.cnblogs.com/r1chie/p/10818039.html)
>
>  [【page cache】简介](https://blog.csdn.net/CoolBoySilverBullet/article/details/121747994)
>
>  [MySQL中的redo log和刷盘策略](https://blog.csdn.net/qq_42946376/article/details/123196937)
>
>  [redolog和binlog 刷盘参数](https://blog.csdn.net/qq_43490312/article/details/125932864#:~:text=1%20%E8%AE%BE%E7%BD%AE%E4%B8%BA%200%20%E7%9A%84%E6%97%B6%E5%80%99%EF%BC%8C%E8%A1%A8%E7%A4%BA%E6%AF%8F%E6%AC%A1%E4%BA%8B%E5%8A%A1%E6%8F%90%E4%BA%A4%E6%97%B6%E9%83%BD%E5%8F%AA%E6%98%AF%E6%8A%8A%20redo%20log%20%E7%95%99%E5%9C%A8%20redo,%E8%AE%BE%E7%BD%AE%E4%B8%BA%202%20%E7%9A%84%E6%97%B6%E5%80%99%EF%BC%8C%E8%A1%A8%E7%A4%BA%E6%AF%8F%E6%AC%A1%E4%BA%8B%E5%8A%A1%E6%8F%90%E4%BA%A4%E6%97%B6%E9%83%BD%E5%8F%AA%E6%98%AF%E6%8A%8A%20redo%20log%20%E5%86%99%E5%88%B0%20page%20cache%E3%80%82)



