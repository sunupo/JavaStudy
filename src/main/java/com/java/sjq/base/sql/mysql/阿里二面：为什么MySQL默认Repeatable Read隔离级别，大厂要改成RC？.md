[阿里二面：为什么MySQL默认Repeatable Read隔离级别，大厂要改成RC？](https://mp.weixin.qq.com/s/ghySMSaOv68BgOR-2oAeGw)



查看数据库当前的隔离级别，MySQL 默认隔离级别是RR.

```
select @@tx_isolation;
```

不同事务隔离级别下的加锁情况：

| 事务隔离级别 | 读         | 加锁、解锁时间                       | 写           | 加锁、解锁时间                         |
| ------------ | ---------- | ------------------------------------ | ------------ | -------------------------------------- |
| 读未提交  RU | X          | X                                    | 行级共享锁   | 修改的时候加锁<br />修改结束解锁<br /> |
| 读已提交 RC  | 行级共享锁 | 读到时才加锁<br />读完该行，立即释放 | 行级排他锁   | 更新瞬间加锁<br />事物结束解锁         |
| 可重复读 RR  | 行级共享锁 | 读取瞬间加锁<br />事物结束释放       | 行级排他锁   | 更新瞬间加锁<br />事物结束解锁         |
| 串行化       | 表级共享锁 | 读取瞬间加锁<br />事务结束解锁       | 表级共排它锁 | 更新瞬间加锁<br />事务结束解锁<br />   |

-   **RU**隔离级别下，可能发生脏读、幻读、不可重复读等问题。
    
    未提交读的数据库锁情况（实现原理）
    
    > 事务在**读**数据的时候并**未对数据加锁**。
    >
    > 事务在**修改数据**的时候只对数据增**加行级共享锁**。
    
-   **RC**隔离级别下，解决了脏读的问题，存在幻读、不可重复读的问题。
    
    提交读的数据库锁情况
    
    > 事务对当前被**读**取的数据加 行级共享锁（当读到时才加锁），一旦读完该行，立即释放该行级共享锁；
    >
    > 事务在更新某数据的瞬间（就是发生更新的瞬间），必须先对其加 行级排他锁，直到事务结束才释放。
    
-   RR隔离级别下，解决了脏读、不可重复读的问题，存在幻读的问题。
    
    可重复读的数据库锁情况
    
    > 事务在读取某数据的瞬间（就是开始读取的瞬间），必须先对其加 行级共享锁，直到事务结束才释放；
    >
    > 事务在更新某数据的瞬间（就是发生更新的瞬间），必须先对其加 行级排他锁，直到事务结束才释放。
    
-   Serializable隔离级别下，解决了脏读、幻读、不可重复读的问题。
    
    可序列化的数据库锁情况
    
    > 事务在读取数据时，必须先对其加 表级共享锁 ，直到事务结束才释放；
    >
    > 事务在更新数据时，必须先对其加 表级排他锁 ，直到事务结束才释放。
    

虽然可序列化解决了脏读、不可重复读、幻读等读现象。但是序列化事务会产生以下效果：

-   1.无法读取其它事务已修改但未提交的记录。
    
-   2.在当前事务完成之前，其它事务不能修改目前事务已读取的记录。
    
-   3.在当前事务完成之前，其它事务所插入的新记录，其索引键值不能在当前事务的任何语句所读取的索引键范围中。

那么，为什么 MySQL 默认隔离级别是RR，为什么阿里等大厂会改成RC？

**根本的原因：提升MYSQL 吞吐量、并发量。允许很短的时间内数据不可重读、幻读，或者在业务维度去规避。**

MySQL的RR”可重复读“这种隔离级别，带来了很大的性能损耗。无论是超高并发读的场景，还是超高并发写的场景，带来了一些的性能损耗。

## RR在高并发写场景的性能损耗

在 MySQL 中，有三种类型的锁，分别是Record Lock、Gap Lock和 Next-Key Lock。

> Record Lock表示记录锁，锁的是索引记录。
>
> Gap Lock是间隙锁，锁的是索引记录之间的间隙。
>
> Next-Key Lock是Record Lock和Gap Lock的组合，同时锁索引记录和间隙。他的范围是左开右闭的。

在 RC 中，只会对索引增加Record Lock，不会添加Gap Lock和Next-Key Lock。

在 RR 中，为了解决幻读的问题，在支持Record Lock的同时，还支持Gap Lock和Next-Key Lock；

### 间隙锁的触发条件：

-   **事务隔离级别为RR**。因为间隙锁只有在事务隔离级别RR中才会产生，隔离级别级别是RC的话，间隙锁将会失效
    
-   **显式加锁**。比如使用了类似于select…for update这样的加锁语句
    
-   **查询条件必须有索引**：
    

> (1)若查询条件走唯一索引：只有锁住多条记录或者一条不存在的记录的时候，才会产生间隙锁；如果查询单条存在的记录，不会产生间隙锁
>
> (2)若查询条件走普通索引：不管是锁住单条，还是多条记录，都会产生间隙锁

间隙锁属于**排它锁**，它会把查询sql中最靠近检索条件的左右两边的数据间隙锁住，防止其它事务在这个间隙内插入、修改、删除数据，从而保证该事务内，任何时候以相同检索条件读取的数据都是相同，也即保证可重复读。

在高并发下，由于间隙被锁住，导致需要往间隙内插入、删除、修改数据的并发线程必须等待，会带来一定性能问题，并且**最终锁影响的范围可能远远超过我们想要操作的数据**。

间隙锁主要是解决RR级别的幻读问题而来。但在现实中，一个事务里重复同一条sql再次查询的场景极低，且出于性能的考虑，一般也会尽量避免在同一事务内对同一数据进行多次查询。

在高并发、分布式RPC应用场景，通过加Redis 分布式Cache的方式，其实也就避免了多次查询。没有多次查询，或者避免多次查询，也就无所谓幻读。

如果实际业务场景中，如果无需锁住数据间隙，建议关闭间隙锁，或者将MySQL隔离级别由RR改为RC，否则会带来无谓的性能开销，甚至会**引发死锁**，影响业务运行。

## RR在高并发读场景的性能损耗

为了提升性能，RR与RC下的普通读都是快照读，这里提到的普通读,  是指除了如下2种之外的select都是普通读

```
select * from table where ... for update;select * from table where ... lock in share mode;
```

所以普通读的范围，是非常大的。

### 快照读，又称为一致性读。

快照即当前行数据之前的历史版本。快照读就是使用快照信息显示基于某个时间点的查询结果，而不考虑与此同时运行的其他事务所执行的更改。

**在MySQL 中，只有READ COMMITTED 和 REPEATABLE READ这两种事务隔离级别才会使用一致性读。**

RR与RC下的普通读都是快照读，但两者的快照读有所不同：

-   RC下，事务内每次都是读最新版本的快照数据
    
-   RR下，事务内每次都是读同一版本的快照数据(即首次read时的版本)
    

RR下，事务会以第一次普通读时快照数据为准，该事务后续其他的普通读都是读的该份快照数据，也即事务内是同一份快照读。这就意味着，Mysql为了维护同一版本的快照数据，需要额外的资源耗损，含计算耗损、内存耗损。

但在现实中，一个事务里重复同一条sql再次查询的场景极低，且出于性能的考虑，一般也会尽量避免在同一事务内对同一数据进行多次查询。因此，RR下所谓的事务内同一份快照读意义并不大。

## 总之：

MySQL的RR”可重复读“这种隔离级别，带来了很大的性能损耗。

无论是超高并发读的场景，还是超高并发写的场景，带来了一些的性能损耗。

MySQL 默认隔离级别是RR，为什么阿里等大厂会改成RC，主要是出于性能考虑。

另外，通过程序手段，去规避幻读、可重复读的问题。

End

此真题面试题，收录于《尼恩Java面试宝典》V34

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

硬核面试题推荐            

-   [**京东、阿里一面：Mysql索引15个夺命连环炮**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487691&idx=1&sn=d33c78918327813fd8b1de0e530920f4&chksm=c142aa4ff63523594c12fd2a9789b6d53555d81d8156645770420e94446e1992a127cd4cc568&scene=21#wechat_redirect)
    
-   [美团一面：聊聊MySQL的七种日志](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487645&idx=1&sn=95d8db950a7bfb4b08fb542bfdf5b27d&chksm=c142aa19f635230f31b65d3b6ac533a68cea96f3c030f6cd961490a0fa8e72a9902b0e00417b&scene=21#wechat_redirect)
    
-   [架构必备：10WQPS超高并发架构的10大思想](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486628&idx=1&sn=357b84473f56a599712dcd73db41e42a&chksm=c142b620f6353f363579b495110ef1e3984408d41fe848af9f035107c08928dd9fb865cd6051&scene=21#wechat_redirect)  
    
-   [如何写简历：2023春招，让简历 人见人爱 的8大绝招 | 附100模板](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486482&idx=1&sn=c8509e44488590dcb0f4e5edd4308971&chksm=c142b696f6353f8026d20c8b6a609966c493377b6285e05749b16cd1c5f7c3f65209ccc3749e&scene=21#wechat_redirect)  
    
-   [**场景题：假设10W人突访，系统能不crash 吗？**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486423&idx=1&sn=fcab28ef08140f0ac5cdab03fb479582&chksm=c142b153f6353845112d7de1e060f44ac0ccb63abfbaddf1a3c3ce7fcc52f9574b4577888c2b&scene=21#wechat_redirect)  
    
-   [每天100w次登陆请求, 8G 内存该如何设置JVM参数？来看看年薪100W的架构师，是怎么配置的](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486317&idx=1&sn=bb44e3c19cc77dd9fa117671fa5a2230&chksm=c142b1e9f63538ff54c6782f9a08ca67e7b06234e4cf7ed9c25c06ededdb190a892697e22fb8&scene=21#wechat_redirect)
    

硬核文章推荐            

-   [顶级、一流的Java 轮子 项目，在这 ！！](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487615&idx=1&sn=7b7976f36dc5817678ca322e28501e58&chksm=c142aafbf63523edabc2693be63c472eaa7af87f95000e1c03f81cd222b6fa2770bd4b9870be&scene=21#wechat_redirect)  
    
-   [一直写不好技术方案，原来是缺一份好模板！顶级模板在这](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487607&idx=1&sn=931ae314ea6bb6035ee8e6cfa9475e94&chksm=c142aaf3f63523e57b245ba1145e87d3cad095df9d9273d72dcbfc1781c9ed7b7ef3e4f205e8&scene=21#wechat_redirect)
    
-   [100w人在线的 弹幕 系统，是怎么架构的？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487578&idx=1&sn=1691379db2928ba49153f52f6fab950c&chksm=c142aadef63523c8b304d8e93687d85ce163043c8b4ef0c7c67ed9e473ec4df10f9871a0e197&scene=21#wechat_redirect)
    
-   [峰值21WQps、亿级DAU，小游戏《羊了个羊》是怎么架构的？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487443&idx=1&sn=604f42dc056410ad03ee59878afe1ade&chksm=c142b557f6353c41107f7a62d1e8e73a483769a8f39cb780bec1fa778a624ad2fc7de44dc6ee&scene=21#wechat_redirect)
    
-   [**2个大厂 100亿级 超大流量 红包 架构方案**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486998&idx=1&sn=24df4a3223bb8e71dc26e84827bbbe00&chksm=c142b492f6353d8449d1f354574f534cde70a308b25453e7922137df7d2fab3c2f2dda77eef7&scene=21#wechat_redirect)
    
-   [一文秒懂：多级时间轮，最顶尖的Java调度算法](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247485259&idx=1&sn=68763802596d99698d3956386b2d263e&chksm=c142bdcff63534d97f08725e20e217238266bebe0e11e73f9db848acd337cc6e3bdd0ab8b549&scene=21#wechat_redirect)  
    
-   [一文搞懂：缓存之王 Caffeine 架构、源码、原理（5W长文）](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247485218&idx=1&sn=125af2a5f356e97dd1bb939616ce9016&chksm=c142bda6f63534b0540412fef91513a33e7a0f1ecde60572618a970a2c9ae1b6ebae51416e24&scene=21#wechat_redirect)
    

硬核电子书            

[**👍**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=4&sn=ace256db11ca343c860527c30492b0ad&chksm=c142be0af635371cd87a94318246353cfa1fe3a38910e186e42796c351d17d00024b10776095&scene=21#wechat_redirect)《[尼恩Java面试宝典](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=3&sn=3c498b0f8e3897e899acf154ad1ac8ee&chksm=c142be0af635371c06b830243517aae063e23814195df1cc75ab2123c5b3ab9cb1217cbd80e3&scene=21#wechat_redirect)》（极致经典，不断升级）**全网下载超过300万次**