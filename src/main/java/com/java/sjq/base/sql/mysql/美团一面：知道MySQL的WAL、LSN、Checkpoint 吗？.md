

在疯狂创客圈 的社群面试交流中，小伙伴在美团面试中， 遇到下面的问题：

**知道MySQL的WAL、LSN、checkpoint吗？简单说说底层原理**

现在把这个 题目以及参考答案，收入咱们的 《尼恩Java面试宝典》V41版，

供后面的小伙伴参考，提升大家的 3高 架构、设计、开发水平。

![图片](%E7%BE%8E%E5%9B%A2%E4%B8%80%E9%9D%A2%EF%BC%9A%E7%9F%A5%E9%81%93MySQL%E7%9A%84WAL%E3%80%81LSN%E3%80%81Checkpoint%20%E5%90%97%EF%BC%9F.assets/640.png)

## 1\. WAL （预写式日志）技术

WAL的全称是 Write-Ahead Logging。

修改的数据要持久化到磁盘，会先写入磁盘的文件系统缓存，然后可以由后台线程异步慢慢地刷回到磁盘。所以WAL技术修改数据需要写两次磁盘。

### **1.1 两次磁盘写**

从内存到磁盘文件系统缓存，顺序IO

从文件系统缓存持久化到磁盘，随机IO

### **1.2 WAL的好处**

节省了随机写磁盘的 IO 消耗（转成顺序写）。

## 2\. LSN（日志序列号）

LSN是Log Sequence Number的缩写，即日志序列号，表示Redo Log 的序号。

### **2.1 特性**

LSN占用8字节，LSN的值会随着日志的写入而逐渐增大，每写入一个 Redo Log 时，LSN 就会递增该 Redo Log 写入的字节数。

### **2.2 LSN的不同含义**

重做日志写入的总量，单位字节。

**通过 LSN 开始号码和结束号码可以计算出写入的日志量。**

-   checkpoint的位置
    

_最近一次刷盘的页，即最近一次检查点(checkpoint)，也是通过LSN来记录的，**它也会被写入redo log里**。_

-   数据页的版本号。
    

在每个页的头部，有一个FIL\_PAGE\_LSN，记录的该页的LSN。表示该页最后刷新时LSN的大小。

其可以用来标记数据页的“版本号”。因此页中的LSN用来判断页是否需要进行恢复操作。

> 通过数据页中的 LSN 值和redo log中的 LSN 值比较，如果页中的 LSN 值小于redo log中 LSN 值，则表示数据丢失了一部分，这时候可以通过redo log的记录来恢复到redo log中记录的 LSN 值时的状态。

### **2.3 查看LSN**

redo log的LSN信息可以通过 show engine innodb status 命令来查看。

```
---
LOG
---
Log sequence number 15114138
Log flushed up to   15114138
Pages flushed up to 15114138
Last checkpoint at  15114129
0 pending log flushes, 0 pending chkp writes
10 log i/o's done, 0.00 log i/o's/second
```

其中：

-   log sequence number就是当前的redo log(in buffer)中的lsn；
    
-   log flushed up to是刷到redo log file on disk中的lsn；
    
-   pages flushed up to是已经刷到磁盘数据页上的LSN；
    
-   last checkpoint at是上一次检查点所在位置的LSN。
    

## 3\. Checkpoint(检查点)

### **3.1 背景**

缓冲池的容量和重做日志（redo log）容量是有限的。

### 3.2 目的

Checkpoint所做的事就是把脏页给刷新回磁盘。

### **3.3 定义**

一个时间点，由一个LSN值（Checkpoint LSN）表示的整型值，在checkpoint LSN之前的每个数据页(buffer pool中的脏页)的更改都已经落盘(刷新到数据文件中)，**checkpoint 完成后，在checkpoint LSN之前的Redo Log就不再需要了。**

所以：checkpoint是通过LSN实现的。

### **3.4 分类**

Sharp Checkpont

该机制下，在数据库发生关闭时将所有的脏页都刷新回磁盘。

Fuzzy Checkpoint

在该机制下，只刷新一部分脏页，而不是刷新所有脏页回磁盘。

```
数据库关闭时，使用 Sharp Checkpont 机制刷新脏页。数据库运行时，使用 Fuzzy Checkpoint 机制刷新脏页。
```

### **3.5 检查点 checkpoint 触发时机**

-   Master Thread Checkpoint
    

后台异步线程以每秒或每十秒的速度从缓冲池的脏页列表中刷新一定比例的页回磁盘。

-   FLUSH\_LRU\_LIST Checkpoint
    

为了**保证LRU列表中可用页的数量**（通过参数innodb\_lru\_scan\_depth控制，默认值1024），后台线程定期检测LRU列表中空闲列表的数量，若不满足，就会将移除LRU列表尾端的页，若移除的页为脏页，则需要进行Checkpoint。

```
show VARIABLES like 'innodb_lru_scan_depth'
```

-   Async/sync Flush Checkpoint
    

**当重做日志不可用**（即redo log写满）时，需要强制将一些页刷新回磁盘，此时脏页从脏页列表中获取。

-   Dirty Page too much Checkpoint
    

即**脏页数量太多**，会强制推进CheckPoint。目的是保证缓冲区有足够的空闲页。innodb\_max\_dirty\_pages\_pct的默认值为75，表示当缓冲池脏页比例达到该值时，就会强制进行Checkpoint，刷新一部分脏页到磁盘。

```
show VARIABLES like 'innodb_max_dirty_pages_pct'
```

### **3.6 解决的问题**

-   缩短数据库的恢复时间。
    
-   缓冲池不够用时，刷新脏页到磁盘。
    
-   重做日志满时，刷新脏页。
    

## 4\. LSN与checkpoint的联系

LSN号串联起一个事务开始到恢复的过程。

重启 innodb 时，Redo log 完不完整，采用 Redo log 相关知识。用 Redo log 恢复，启动数据库时，InnoDB 会扫描数据磁盘的数据页 data disk lsn 和日志磁盘中的 checkpoint lsn。

两者相等则从 checkpoint lsn 点开始恢复，恢复过程是利用 redo log 到 buffer pool，直到 checkpoint lsn 等于 redo log file lsn，则恢复完成。如果 checkpoint lsn 小于 data disk lsn，说明在检查点触发后还没结束刷盘时数据库宕机了。

因为 checkpoint lsn 最新值是在数据刷盘结束后才记录的，检查点之后有一部分数据已经刷入数据磁盘，这个时候数据磁盘已经写入部分的部分恢复将不会重做，==直接跳到没有恢复的 lsn 值开始恢复==。（从checkpoint lsn 跳到 datadisk lsn，把datadisk lsn 到redolog lsn的数据从redolog文件恢复到 redo log buffer）

> [美团一面：聊聊MySQL的七种日志](https://mp.weixin.qq.com/s/IWdqFq9ZtWug-M5pWxPCUQ)
>
> Redo log 有两个 LSN，一个writepos就是常说的redolog的LSN，一个checkpoint是存在redolog头部的而另一个lsn
>
> #### 1.3 crash-safe
>
> 因为redo log的存在使得**Innodb**引擎具有了**crash-safe**的能力，即MySQL宕机重启，系统会自动去检查redo log，将修改还未写入磁盘的数据从redo log恢复到MySQL中。
>
> MySQL启动时，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。
>
> 会先检查数据页中的**LSN**，如果这个 LSN 小于 redo log 中的LSN，即**write pos**位置，说明在**redo log**上记录着数据页上尚未完成的操作，接着就会从最近的一个**check point**出发，开始同步数据。
>
> 简单理解，比如：redo log的**LSN**是500，数据页的`LSN`是300，表明重启前有部分数据未完全刷入到磁盘中，那么系统则将redo log中**LSN**序号300到500的记录进行**重放刷盘**。==上面说是恢复到buffer poll，这儿说是重放刷盘。刷盘是肯定的，刷盘难道是先从redolog文件恢复到内存的bufferpool，然后再从bufferpool刷盘？应该是，因为redolog是逻辑日志。==

两个不等：1.如果 checkpoint lsn 小于 data disk lsn，说明在检查点触发后还没结束刷盘时数据库宕机了。2. checkpoint lsn == data disk lsn，checkpoint 小于 redolog lsn，

> ![图片](assets/640.png)
>
> #### 1.2 redo log 大小固定
>
> redo log采用固定大小，循环写入的格式，当redo log写满之后，重新从头开始如此循环写，形成一个环状。
>
> 那为什么要如此设计呢？
>
> 因为redo log记录的是数据页上的修改，如果**Buffer Pool**中数据页已经刷磁盘后，那这些记录就失效了，新日志会将这些失效的记录进行覆盖擦除。
>
> <img src="assets/640-1678723852575-6.png" alt="图片" style="zoom:25%;" />
>
> 上图中的**write pos**表示redo log当前记录的日志序列号**LSN**(log sequence number)，写入还未刷盘，循环往后递增；
>
> **check point**表示redo log中的修改记录已刷入磁盘后的LSN，循环往后递增，这个LSN之前的数据已经全落盘。
>
> **write pos**到**check point**之间的部分是redo log空余的部分（绿色），用来记录新的日志；
>
> **check point**到**write pos**之间是redo log已经记录的数据页修改数据，此时数据页还未刷回磁盘的部分。
>
> 当**write pos**追上**check point**时，会先推动**check point**向前移动，空出位置（刷盘）再记录新的日志。

## 5\. 总结

日志空间中的每条日志对应一个LSN值，而在数据页的头部也记录了当前页最后一次修改的LSN号，每次当数据页刷新到磁盘后，会去更新日志文件中checkpoint，以减少需要恢复执行的日志记录。

极端情况下，数据页刷新到磁盘成功后，去更新checkpoint时如果宕机，则在恢复过程中，由于checkpoint还未更新，则数据页中的记录相当于被重复执行，不过由于在日志文件中的操作记录具有幂等性，所以同一条redo log执行多次，不影响数据的恢复。（==但是上面说的是跳到 磁盘数据页的LSN开始恢复==，因为幂等，所以上面说的和这儿说的重checkpoit lsn更新是一样的。说不定幂等就是判断过数据了，就往后面跳）

End

此真题面试题，收录于《尼恩Java面试宝典》V41

## 6 我的笔记

- 通过数据页中的 LSN 值和redo log中的 LSN 值比较，如果页中的 LSN 值小于redo log中 LSN 值，则表示数据丢失了一部分，这时候可以通过redo log的记录来恢复到redo log中记录的 LSN 值时的状态。
- 

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

硬核面试题推荐            

-   [京东一面：20多种异步方式，你知道几个？     Java协程](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487978&idx=1&sn=f4ba06dbc32eba5f14c2f77585e8b7ad&chksm=c142ab6ef635227839d6fd37db005b6110bc23e1b82a7e90d2cdc29b29bd8b2af4f85618a5e5&scene=21#wechat_redirect)异步[](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487978&idx=1&sn=f4ba06dbc32eba5f14c2f77585e8b7ad&chksm=c142ab6ef635227839d6fd37db005b6110bc23e1b82a7e90d2cdc29b29bd8b2af4f85618a5e5&scene=21#wechat_redirect)
    
-   [美团一面：InndoDB 单表最多 2000W，为什么？小伙伴竟然面挂](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487864&idx=1&sn=3544971b08091ef90cc5ffd656e8ec47&chksm=c142abfcf63522ea192bf5701dc22fdf7e2d1f27521a3e8dfa3b4d23d254c55bf49cbf664b6b&scene=21#wechat_redirect)
    
-   [阿里二面：BigKey、HotKey 问题严重，如何 预防、解决](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487830&idx=1&sn=85a608a56aa414f059135e31f7642375&chksm=c142abd2f63522c4a9679645617246b45e20b88d9e8c4aace137759b3c589a3e4760a381622a&scene=21#wechat_redirect)
    
-   [阿里二面：千万级、亿级数据，如何性能优化？教科书级 答案来了](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487812&idx=1&sn=b48cd2c6a4d279ad6afe4ae4e2a7e8a1&chksm=c142abc0f63522d6b863c1f15bbf56725ab06c487b91241395efbb359fa2ba28f9fbe93687f2&scene=21#wechat_redirect)
    
-   [字节二面：100Wqps短链系统，如何设计？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487759&idx=1&sn=8e48e5d51db9194cad0cd96db8c1aa03&chksm=c142ab8bf635229d37c25383dff2258aaf2ef78c7394ccd1ae1050f6340a191b90a9d3f681e2&scene=21#wechat_redirect)
    
-   [网易二面：CPU狂飙900%，该怎么处理？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487733&idx=1&sn=acd0d043f77b46d400a02265351eddff&chksm=c142aa71f6352367d8974231e3876f90d089844b902c1bc8a5a2b0c2a368c203ddedc4b6a733&scene=21#wechat_redirect)
    
-   [阿里二面：为什么MySQL默认Repeatable Read隔离级别，大厂要改成RC？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487704&idx=1&sn=20c12db022a8ee5dc14e669c27c6a4b8&chksm=c142aa5cf635234a1829deb04b182c0e98b5f236c57f4bf2eee7229f57dc837120cb9e3a0efc&scene=21#wechat_redirect)
    
-   [美团一面：聊聊MySQL的七种日志](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487645&idx=1&sn=95d8db950a7bfb4b08fb542bfdf5b27d&chksm=c142aa19f635230f31b65d3b6ac533a68cea96f3c030f6cd961490a0fa8e72a9902b0e00417b&scene=21#wechat_redirect)
    
-   [架构必备：10WQPS超高并发架构的10大思想](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486628&idx=1&sn=357b84473f56a599712dcd73db41e42a&chksm=c142b620f6353f363579b495110ef1e3984408d41fe848af9f035107c08928dd9fb865cd6051&scene=21#wechat_redirect)
    
-   [如何写简历：2023春招，让简历 人见人爱 的8大绝招 | 附100模板](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486482&idx=1&sn=c8509e44488590dcb0f4e5edd4308971&chksm=c142b696f6353f8026d20c8b6a609966c493377b6285e05749b16cd1c5f7c3f65209ccc3749e&scene=21#wechat_redirect)
    
-   [场景题：假设10W人突访，系统能不 crash 吗？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486423&idx=1&sn=fcab28ef08140f0ac5cdab03fb479582&chksm=c142b153f6353845112d7de1e060f44ac0ccb63abfbaddf1a3c3ce7fcc52f9574b4577888c2b&scene=21#wechat_redirect)
    

硬核文章推荐            

-   [顶级、一流的Java 轮子 项目，在这 ！！](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487615&idx=1&sn=7b7976f36dc5817678ca322e28501e58&chksm=c142aafbf63523edabc2693be63c472eaa7af87f95000e1c03f81cd222b6fa2770bd4b9870be&scene=21#wechat_redirect)
    
-   [一直写不好技术方案，原来是缺一份好模板！顶级的技术模板](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487607&idx=1&sn=931ae314ea6bb6035ee8e6cfa9475e94&chksm=c142aaf3f63523e57b245ba1145e87d3cad095df9d9273d72dcbfc1781c9ed7b7ef3e4f205e8&scene=21#wechat_redirect)来了[](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487607&idx=1&sn=931ae314ea6bb6035ee8e6cfa9475e94&chksm=c142aaf3f63523e57b245ba1145e87d3cad095df9d9273d72dcbfc1781c9ed7b7ef3e4f205e8&scene=21#wechat_redirect)
    
-   [100w人在线的 弹幕 系统，是怎么架构的？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487578&idx=1&sn=1691379db2928ba49153f52f6fab950c&chksm=c142aadef63523c8b304d8e93687d85ce163043c8b4ef0c7c67ed9e473ec4df10f9871a0e197&scene=21#wechat_redirect)
    
-   [峰值21WQps、亿级DAU，小游戏《羊了个羊》是怎么架构的？](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247487443&idx=1&sn=604f42dc056410ad03ee59878afe1ade&chksm=c142b557f6353c41107f7a62d1e8e73a483769a8f39cb780bec1fa778a624ad2fc7de44dc6ee&scene=21#wechat_redirect)
    
-   [2个大厂 100亿级 超大流量 红包 架构方案](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247486998&idx=1&sn=24df4a3223bb8e71dc26e84827bbbe00&chksm=c142b492f6353d8449d1f354574f534cde70a308b25453e7922137df7d2fab3c2f2dda77eef7&scene=21#wechat_redirect)
    
-   [一文秒懂：多级时间轮，最顶尖的Java调度算法](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247485259&idx=1&sn=68763802596d99698d3956386b2d263e&chksm=c142bdcff63534d97f08725e20e217238266bebe0e11e73f9db848acd337cc6e3bdd0ab8b549&scene=21#wechat_redirect)
    
-   [一文搞懂：缓存之王 Caffeine 架构、源码、原理（5W长文）](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247485218&idx=1&sn=125af2a5f356e97dd1bb939616ce9016&chksm=c142bda6f63534b0540412fef91513a33e7a0f1ecde60572618a970a2c9ae1b6ebae51416e24&scene=21#wechat_redirect)
    

硬核电子书            

[**👍**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=4&sn=ace256db11ca343c860527c30492b0ad&chksm=c142be0af635371cd87a94318246353cfa1fe3a38910e186e42796c351d17d00024b10776095&scene=21#wechat_redirect)《[尼恩Java面试宝典](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=3&sn=3c498b0f8e3897e899acf154ad1ac8ee&chksm=c142be0af635371c06b830243517aae063e23814195df1cc75ab2123c5b3ab9cb1217cbd80e3&scene=21#wechat_redirect)》（极致经典，不断升级）**全网下载超过300万次**  

[**👍**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=4&sn=ace256db11ca343c860527c30492b0ad&chksm=c142be0af635371cd87a94318246353cfa1fe3a38910e186e42796c351d17d00024b10776095&scene=21#wechat_redirect)尼恩Java高并发三部曲：**全网下载超过200万次**

-   [**👍**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=4&sn=ace256db11ca343c860527c30492b0ad&chksm=c142be0af635371cd87a94318246353cfa1fe3a38910e186e42796c351d17d00024b10776095&scene=21#wechat_redirect)《[Java高并发核心编程-卷1（加强版）](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=2&sn=9bc00924281da1785fe3f472e59eb035&chksm=c142be0af635371c77541a6634875fde3a7e6f512501e0a9de2e73b67e9d447d6b5e73b713f4&scene=21#wechat_redirect)》，不断升级
    
-   [**👍**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=4&sn=ace256db11ca343c860527c30492b0ad&chksm=c142be0af635371cd87a94318246353cfa1fe3a38910e186e42796c351d17d00024b10776095&scene=21#wechat_redirect)《[Java高并发核心编程-卷2（加强版）](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=2&sn=9bc00924281da1785fe3f472e59eb035&chksm=c142be0af635371c77541a6634875fde3a7e6f512501e0a9de2e73b67e9d447d6b5e73b713f4&scene=21#wechat_redirect)》，不断升级
    
-   **👍**《[Java高并发核心编程-卷3（加强版）](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=2&sn=9bc00924281da1785fe3f472e59eb035&chksm=c142be0af635371c77541a6634875fde3a7e6f512501e0a9de2e73b67e9d447d6b5e73b713f4&scene=21#wechat_redirect)》，不断升级
    

[**👍**《顶级3高架构行业案例 + 尼恩架构笔记 》N 篇+](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=4&sn=ace256db11ca343c860527c30492b0ad&chksm=c142be0af635371cd87a94318246353cfa1fe3a38910e186e42796c351d17d00024b10776095&scene=21#wechat_redirect)，不断添加

[**👍**](http://mp.weixin.qq.com/s?__biz=MzkxNzIyMTM1NQ==&mid=2247484558&idx=4&sn=ace256db11ca343c860527c30492b0ad&chksm=c142be0af635371cd87a94318246353cfa1fe3a38910e186e42796c351d17d00024b10776095&scene=21#wechat_redirect)100份简历模板

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)