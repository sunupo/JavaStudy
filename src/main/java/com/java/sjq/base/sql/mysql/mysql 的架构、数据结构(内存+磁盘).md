[mysql的架构、数据结构(内存+磁盘)](https://blog.csdn.net/early_or_later/article/details/106407689)

启停：

> ```
> 重启：systemctl restart mysqld.service停止：systemctl stop mysqld.service查看状态：systemctl status mysqld.service
> ```

mysql的内部模块：

![](https://img-blog.csdnimg.cn/20200528171934105.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

![](https://img-blog.csdnimg.cn/20200801164523715.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

上面是查询的逻辑，一条更新语句是如何查询的

     更新语句的基本流程和查询前面是一致的，区别在于，更新语句，拿到了符合条件数据之后的操作是怎么进行处理的?

下面介绍一下基本概念：

[MySQL :: MySQL 5.7 Reference Manual :: 14.4 InnoDB Architecture](https://dev.mysql.com/doc/refman/5.7/en/innodb-architecture.html)

![InnoDB architecture diagram showing in-memory and on-disk structures.](https://dev.mysql.com/doc/refman/5.7/en/images/innodb-architecture-5-7.png)

[MySQL :: MySQL 8.0 Reference Manual :: 15.4 InnoDB Architecture](https://dev.mysql.com/doc/refman/8.0/en/innodb-architecture.html)

![InnoDB architecture diagram showing in-memory and on-disk structures. In-memory structures include the buffer pool, adaptive hash index, change buffer, and log buffer. On-disk structures include tablespaces, redo logs, and doublewrite buffer files.](https://dev.mysql.com/doc/refman/8.0/en/images/innodb-architecture-8-0.png)

![](https://img-blog.csdnimg.cn/20200529092652381.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

### mysql 内存结构： 

mysql内存主要结构：buffer pool、change buffer、Log Buffer 、adaptive Hash Index

![](https://img-blog.csdnimg.cn/c2d9eae7366e4cbbb1a3157032b724ea.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_12,color_FFFFFF,t_70,g_se,x_16)

1、缓冲池 [buffer pool](https://dev.mysql.com/doc/refman/8.0/en/innodb-buffer-pool.html)

 Innodb的数据上面我们也介绍了，数据是存在磁盘的idb文件中，交互时速度必然是比较慢的。这时就需要将数据加载到内存中。

![](https://img-blog.csdnimg.cn/97f57f79b6c34ab8ad7dcbfbc9741187.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_20,color_FFFFFF,t_70,g_se,x_16)

innodb逻辑上的最小单位是页(16k)，那么每次加载的页到内存的区域（预读），就是buffer pool。下一次取数据（数据页或者索引页）时，会优先判断在不在buffer页中。修改时，会修改内存中的数据，当buffer pool中数据和磁盘不一致时，这时就叫脏页。会有工作线程会定时同步，这个操作就叫刷脏

满了怎么办？默认大小是128M  LRU的淘汰策略（简单来说就是链表，有头和尾，并且划分年轻代和老年代，以解决预读和扫表产生的问题）

> mysql的LRU算法:
>
> 普通的LRU：
>
> -   新数据插入到链表头部;
> -   每当缓存命中（即缓存数据被访问），则将数据移到链表头部；
> -   当链表满的时候，将链表尾部的数据丢弃。
>
>  mysql改进的LRU：
>
> 1.    buffer pool 分为了young（5/8）和old（5/3）两个部分
> 2.    数据会在用户的sql用到了页中的数据，或者mysql猜测你很可能会用到的数据-预读，这两种情况加载到buffer pool
> 3.    数据优先进入old，old满了会移出队列尾部。young区呢？ 用户sql使用的页，会被移入young（且需要在old中待满配置的秒数）。而预读的数据，没被使用则会一直在old区，直到被清除
>
> 问题： 当一次大表扫描，会导致大量数进入young？ 
>
> 解决：mysql防止一次扫描数据过大，替换了大量热数据，有参数控制 innodb\_old\_blocks\_time(需要在old区待满多少秒，且再被用户sql调用时，才会进入young)。

![](https://img-blog.csdnimg.cn/a89f4c0ad25f4b4db64511a2e1dfc772.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_20,color_FFFFFF,t_70,g_se,x_16)

2、change Buffer

> **change Buffer**
>
> 当数据不存在于buffer pool时，且更新操作的数据没有唯一索引(非唯一，非主键，不是降序索引)，不存在重复数据的情况，可以直接在change buffer中记录操作。
>
> 原因： 这样的数据，不需要校验唯一性，直接更新即可，没必要加载数据到buffer pool，而且查询时，数据也是随机分布在不同的页上面，可能需要加载多个页的数据，产生大量IO操作。
>
> change buffer的数据什么时候会同步出去：
>
> 1.  访问这个数据页时、执行merge操作，将修改操作合并，后面同步到磁盘
> 2.  空闲时后台线程处理
> 3.  shoutdown 

3、Log Buffer

> **Log Buffer**
>
> 官方描述：日志缓冲区是保存要写入磁盘上日志文件的数据的内存区域。日志缓冲区大小由 [innodb\_log\_buffer\_size](https://dev.mysql.com/doc/refman/5.6/en/innodb-parameters.html#sysvar_innodb_log_buffer_size "innodb_log_buffer_size")变量定义。默认大小为 16MB。日志缓冲区的内容会定期刷新到磁盘。大的日志缓冲区使大事务能够运行，而无需在事务提交之前将redo log 数据写入磁盘。因此，如果您有更新、插入或删除许多行的事务，则增加日志缓冲区的大小可以节省磁盘 I/O。
>
> 用来记录操作日志，先写入到log buffer ，然后事务提交，或者缓冲区满了的时候写入磁盘文件 redo log
>
> redo log
>
> 是一种基于磁盘的数据结构，用于在崩溃恢复期间纠正由不完整事务写入的数据。
>
> 作用: 主要用来做崩溃恢复，用来实现事务持久性
>
> 内部机制：数据存在缓冲区，没有进行刷盘操作时，如果数据库宕机或者重启，会导致数据丢失。 redo log的作用就是，数据写入缓存后，写一下redo log ，记录的不是最终的结果，而是要进行的操作。崩溃恢复时，将redo log数据重新加载到缓冲区。
>
> 策略： 0（延时写）  1  2（实时写，延时刷）  默认是1 事务操作实时记录并刷新数据页
>
> 疑问：写到日志文件还是一次磁盘io，为什么不直接写到数据页 ？   
>
> -   顺序IO和随机IO  **写日志是顺序io**，效率高（可以参考Kakfa）
> -   记录的是操作，不是结果，数据量其实不大

4、Adaptive Hash Index

> **Adaptive Hash Index**
>
> 1111

内存和磁盘中间其实还有一层osCache，用来提高效率。 

> 磁盘：
>
> 磁盘中主要组成部分是5类表空间
>
> 系统表空间（ibdata1）：
>
> 组成：数据字典（表和索引的元信息） 、双写缓冲区 、undo log、Change buffer
>
> 双写缓冲： Innodb从buffer pool  flushed数据页过程中，如果出现崩溃等异常情况（简单理解就是，刷数据时出现刷一半崩溃的情况，内存被清空了，磁盘也刷了一半数据，这种场景需要处理），  innodb 为我们提供双写机制，先拷贝一份数据的副本，如果崩溃，就将副本恢复。虽然数据需要双写，但是并不是两倍的IO开销，因为写入缓冲区是一次 large sequential chunk （官网这一句理解不了，暂时理解为顺序写入开销不大）
>
> 独占表空间： 可以为每个表设置为独占表空间
>
> 临时表空间：
>
> 通用表空间：
>
> redo log：记录数据到磁盘，用于崩溃恢复，保证事务的持久性
>
> undo log：撤销回滚日志 记录事务发生之前的状态，如果发生异常，可以使用undo log回滚，存储的是旧数据，并发事务时使用回滚段来控制。
>
> bin log ，server层的日志。主要是用来主从复制（原理就是读取主库的bin log，执行一遍sql），数据恢复。记录所有的sql语句，有点aof的味道，可以将操作重复实现数据恢复。和redo log不一样，他的内容可以追加，没有大小限制

更新的流程：

1、事务开始，尝试走buffer pool中获取数据，(唯一索引这种，从磁盘拿数据)返回给server执行器

2、记录数据到 undo log (旧数据)

3、调用存储引擎api，将结果修改内存（buffer pool）

4、记录数据到log buffer (redo log) 将这一行数据记录为prepare  (二阶段提交 prepare commit)

5、写入bin log

6、commit 提交事务

7、同步数据到log buffer 将redo log事务状态设置为commit

8、等待工作线程刷脏

![](https://img-blog.csdnimg.cn/20200531155849552.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

![](https://img-blog.csdnimg.cn/89c4bd26fd904e56a758d8c8298f03c5.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_20,color_FFFFFF,t_70,g_se,x_16)

上面是mysql的一些底层存储情况，下面介绍一下索引

索引是什么？

     是数据库中一种排序的数据结构，用来协助于快速查询和更新数据表。
    
     索引记录的是索引字段信息+对应的磁盘地址，以便于快速查找
    
     ![](https://img-blog.csdnimg.cn/20200531200304164.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

innodb的索引：  
聚集索引    非聚集索引(又叫二级索引，普通索引)

-   有主键，主键会作为聚集索引
-   无主键，第一个非空的唯一索引作为聚集索引
-   没有合适的唯一索引，默认创建row id 来作为聚集索引

聚集索引的作用： 优化查询、插入和其他数据库操作的性能  聚集索引直接指向数据对应的页，所以节省io  

二级索引和聚集索引的关系：二级索引中其实包含主键信息，通过主键值到聚集索引中找到对应的行数据。所以PK其实越小越好

![](https://img-blog.csdnimg.cn/4f5b51834f744584a778f28b26cd0f18.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_20,color_FFFFFF,t_70,g_se,x_16)

索引类型：

普通索引nomol  、 唯一索引unique、全文索引 fulltext

普通索引，没有任何限制 

唯一索引，唯一性约束，主键是特殊的唯一索引

全文索引，针对比较大的数据，查询部分内容，使用like性能低，我们可以再文本类型上建全文索引

> mysql索引存储模型的演进推倒：
>
> 1、我们想用什么数据结构，来存储，实现快速定位？
>
>    数组或者链表，但是存在修改的情况，使用链表  但存在问题，链表从上往下查询，数据量大时，效率低O(n)
>
> 2、链表效率低，2分增加效率 使用二叉查找树
>
>   提高了效率，但是二叉查找树极端情况会变成链表，依旧时O(n)
>
> 3、平衡二叉查找数 可以在数据分布不均匀的时候，利用左旋和右旋，实现平衡
>
>   平衡二叉树解决了分布问题，但是查询效率受树的度所影响，会出现瘦长的树，效率不高
>
> 4、B Tree 可以使用多路的特性（路数比关键字数多1），将二叉演进成多叉，将瘦长的树，转换为矮胖的那种，符合条件
>
>   还能不能再进一步了，优化一波
>
> 5、B+Tree (加强版) 
>
>   特点：路数和关键字数相等 
>
>             根节点和枝节点不存数据，只有叶子节点存数据
>                 
>             叶子节点维护了一个指针，指向相邻的叶子节点，形成一个有序列表
>                 
>             根据左闭右开的区间来检索数据
>
>    优势： 1、存储的数据量大，一个三层的B+Tree就可以存储千万级数据，查询效率高
>
>                     数据量大的原因：
>                 
>                                   路数和关键字相等
>                 
>                                   根节点和枝节点不存数据，所消耗的空间就更少，所存储的子节点的路数就更多
>                 
>                2、所有的数据都存在叶子节点，查询效率稳定
>                 
>                3、叶子节点维护了一个类似于链表的指针，范围查询时快，不需要每次都从上往下查一遍
>                 
>                4、扫库扫表能力强，所有数据都在叶子节点上
>
> 了解：
>
> 为什么不用红黑树？
>
>       红黑树的特点：节点分为红色和黑色 
>     
>                                 根节点必须是黑色
>                 
>                                 叶子节点都是黑色的null节点
>                 
>                                 红色节点的两个子节点都是黑色（不允许相邻两个红节点）   
>                 
>                                从任意节点，到每个叶子节点路径上包含相同的黑色节点
>     
>      原因：红黑树是二叉，只有两路   不够平衡             

MYISAM和INNODB的索引存储形式：

show VARIABLES LIKE 'datadir';查看数据存储路径  


MYISAM:

生成的文件：

user\_myisam.frm  
user\_myisam.MYD  
user\_myisam.MYI

.frm存储的是表结构定义信息等数据。

.MYD D代表data 存储的数据文件，存放的是所有的数据信息

.MYI  I代表index 存储的是索引信息

也就是说，在MYISAM内部，其实B+Tree中维护的叶子节点，也全部都是数据文件的地址信息，根据MYI中的index的地址，然后查询.MYD。主键索引和辅助索引都是这种形式

INNODB内部结构：

user\_innodb.frm  存储表结构定义信息  
user\_innodb.ibd  存储索引文件和数据（B+Tree内部）

主键索引的所有数据，都存在叶子节点上

![](https://img-blog.csdnimg.cn/20200531212336381.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

辅助索引，通过主键索引来实现查找（回表）

辅助索引存储的是主键的值，然后通过这个值去主键索引中查找

为什么存主键的值，不存地址？因为会存在分裂合并的情况，地址会变，不利于维护

![](https://img-blog.csdnimg.cn/20200531212415551.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

如果没有主键怎么办？

1、定义了主键，会使用主键

2、没有主键，会选择第一个不包含null的一个唯一索引

3、没有唯一索引，会使用内置的ROWID，并随着记录写入，递增

索引使用的原则：

   肯定不是越多越好，占空间，插入需要写入更多的，耗性能

列的离散度  count(distinct(column\_name)) : count(\*)  离散度越高，数据区分度越大，重复值越少，适合

联合索引的最左匹配：（name，phone）

![](https://img-blog.csdnimg.cn/20200531213114456.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Vhcmx5X29yX2xhdGVy,size_16,color_FFFFFF,t_70)

联合索引是复合数据结构，按照从左到右的顺序来创建索引树

查询时是先根据name去匹配，匹配到了，再根据phone匹配。匹配不到，就不知道往哪去查了

所以建联合索引的时候，一定要把最常用的列放在最左边。

思考下哪个用不到索引：

SELECT \* FROM user\_innodb WHERE name= '权亮' AND phone='15204661800';  
SELECT \* FROM user\_innodb WHERE name= '权亮';  
SELECT \* FROM user\_innodb WHERE phone='15204661800';

创建的话，按照最左匹配原则，（a,b,c）其实本质已经有了（a）(a,b) (a,b,c)无需重复创建

覆盖索引：

上面也介绍过，辅助索引有回表的情况，多了一次io，但是如果只查询索引包含的数据，就不用回表，也提醒我们，不要select \*，只查询自己需要的数据

索引的创建和使用原则：

1、在where和order join使用的字段上创建索引

2、索引个数不要太多，按需（浪费空间，更新变慢）

3、区分度低的数据，不要建索引（大量数据一致，扫描行数过高）

4、频繁更新的值，不要作为主键或者索引（页分裂）

5、组合索引 区分度高的放在左边，最左原则

6、适当使用复合索引

什么时候用不到索引：

1、索引列上使用函数、计算  SELECT\*FROM\`t2\`whereid+1=4;

2、字符串不加引号，出现隐式转换

3、like条件中前面使用%

索引介绍完了，讲一下事物

事物的定义：

       数据库的逻辑单元（最小的工作单位），由一个有限的数据库操作序列构成（一个或多个DML）。

事物的特性ACID：

1、原子性Atomicity：事物是数据库的最小的逻辑单元，内部所有操作，要么全部成功，要么全部失败。（使用undo log来实现）

2、一致性consistent:  主要是两个方面，一个是数据库层面（完整性约束保持一致，不会被破坏） 一个是业务层面，业务约束满足（举例：A转账B A1000 转账成功  B 收到500 也成功，满足原子性都成功了，但是不满足一致性，账目不对）

3、隔离性 Isolation： 数据库有事物存在，就会同时有许多事物共同运行，隔离性主要就是为了让事物之间操作透明，互补干扰

4、持久性 Durable ：持久性的意思就是，一个事物操作，只要事物提交了，结果就是持久性的，不会因为服务器宕机重启丢失，实现方式其实主要就是redo log + dubbo write 来实现。

\*\*\*：原子性 隔离性 持久性 是手段，目的都是为了实现一致性

什么时候出现事物：

1、自动提交，默认打开 show variables like 'autocommit';

2、手动： begin; / start transaction;    commit;  rollback;

事物并发会带来什么问题？

1、脏读 （读到另外一个事物里面未提交的数据）

2、不可重复读（读到另外一个事物已提交的事物-修改，数据前后不一致情况）

3、幻读  （读到另外一个事物，新增的数据）

这三个统一是读一致性问题，必须由数据库厂家的事物隔离界别

### MVCC 多版本并发控制

引用《高性能mysql第三版》的话，理解下

![](https://img-blog.csdnimg.cn/91fdbc2175cb43f7b1c2d8ab91acc78f.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_20,color_FFFFFF,t_70,g_se,x_16)

![](https://img-blog.csdnimg.cn/032b4e1e4d2c403bb3f28d6f6cf21de1.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_20,color_FFFFFF,t_70,g_se,x_16)

![](https://img-blog.csdnimg.cn/d9d034d0bc1747189408b0a27f36486b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAZWFybHlfb3JfbGF0ZXI=,size_20,color_FFFFFF,t_70,g_se,x_16)