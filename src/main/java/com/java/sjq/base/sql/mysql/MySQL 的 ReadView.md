[(130条消息) 【数据库】MySQL的ReadView\_mysql readview\_BigBig\_Wayen的博客-CSDN博客](https://blog.csdn.net/Cool_Wayen/article/details/126369405)



[原文：【数据库】MySQL的ReadView\_thesprit的博客-CSDN博客\_mysql readview](https://blog.csdn.net/thesprit/article/details/112970122 "原文：【数据库】MySQL的ReadView_thesprit的博客-CSDN博客_mysql readview")

## **MySQL的ReadView**


**前言**

1、根据事务的隔离级别，我们已经知道读未提交、读已提交、可重复读、串行化，随着隔离级别的加强，能解决脏写、脏读、不可重复读、幻读的问题。

2、InnoDB 是 MySQL(mysql-5.1版本后) 默认的存储引擎，InnoDB 默认的隔离级别就是可重复读。在这个隔离级别下，开启事务之后，多次读写同一行数据，读到的值永远是一致。

3、当 MySQL 执行写操作之前，会把即将被修改的数据记录到 undo log 日志里面。MySQL可从 undo log 日志中，读取到原插入、修改、删除之前的值，最终把值重新变回去，这就是回滚操作。

**undo log 版本链**

1、undo log 版本链是基于 undo log 实现的。undo log 中主要保存了数据的基本信息，比如说日志开始的位置、结束的位置，主键的长度、表id，日志编号、日志类型

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192322560.png#pic_center)

 此外，undo log 还包含两个隐藏字段 trx\_id 和 roll\_pointer。trx\_id 表示当前这个事务的 id，MySQL 会为每个事务分配一个 id，这个 id 是递增的。roll\_pointer 是一个指针，指向这个事务之前的 undo log。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192333997.png#pic_center)

 2、例子  
执行：

```
INSERT INTO student VALUES (1, '张三');
```

产生：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192349964.png#pic_center)

 继续执行：

```
UPDATE student SET name='李四' WHERE id=1;
```

产生：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192402415.png#pic_center)

 继续执行：

```
UPDATE student SET name='王五' WHERE id=1;
```

产生：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192413555.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3RoZXNwcml0,size_16,color_FFFFFF,t_70#pic_center)

 为了保证事务并发操作时，在写各自的undo log时不产生冲突，InnoDB采用回滚段的方式来维护undo log的并发写入和持久化。回滚段实际上是一种Undo文件组织方式。

**ReadView 机制**

ReadView 其实就是一个保存事务ID的list列表。记录的是本事务执行时，MySQL还有哪些事务在执行，且还没有提交。(当前系统中还有哪些活跃的读写事务)

它主要包含这样几部分：

-   ==m\_ids==，当前有哪些事务正在执行，且还没有提交，这些事务的 id 就会存在这里；
-   ==min\_trx\_id==，是指 m\_ids 里最小的值；
-   ==max\_trx\_id==，是指下一个要生成的事务 id。下一个要生成的事务 id 肯定比现在所有事务的 id 都大；
-   ==creator\_trx\_id==，每开启一个事务都会生成一个 ReadView，而 creator\_trx\_id 就是这个开启的事务的 id。

这样在访问某条记录时，只需要按照下边的步骤判断该记录在版本链中的某个版本（trx\_id）是否可见：  
        **1**、trx\_id < m\_ids列表中最小的事务id  
        表明生成该版本的事务在生成ReadView前已经提交，所以该版本可以被当前事务访问。  
        **2**、trx\_id > m\_ids列表中最大的事务id  
        表明生成该版本的事务在生成ReadView 后才生成，所以该版本不可以被当前事务访问。  
        **3**、m\_ids列表中最小的事务id < trx\_id < m\_ids列表中最大的事务id  
        此处比如m\_ids为\[5,6,7,9,10\]  
        **①**、若trx\_id在m\_ids中，比如是6，说明创建 ReadView 时生成该版本的事务还是活跃的，该版本不可以被访问。  
        **②**、若trx\_id不在m\_ids中，比如是8：说明创建 ReadView 时生成该版本的事务已经被提交，该版本可以被访问。

**一句话说：当trx\_id在m\_ids中，或者大于m\_ids列表中最大的事务id的时候，这个版本就不能被访问。**

如果某个版本的数据对当前事务不可见的话，那就顺着版本链找到下一个版本的数据，继续按照上边的步骤判断可见性，依此类推，直到版本链中的最后一个版本，如果最后一个版本也不可见的话，那么就意味着该条记录对该事务不可见，查询结果就不包含该记录。

**例子：**

事务是可以并发执行的，现在有事务 A、事务 B 这两个事务，且这两个都没有提交。事务 A 将会执行多次读操作，来模拟可重复读中多次读取同一行数据的场景。事务 B 则会修改这一行数据。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192435808.png#pic_center)

==事务 A 开启事务的时候会生成一个 ReadView==，所以说这个 ReadView 的创建者就是事务 A，事务 A 的事务 id 是 10，所以 creator\_trx\_id 就是 10。

此时，总共就只有事务 A、事务 B 这两个事务，而且它们都还没有提交，所以说 m\_ids 会把这两个事务 id，10、18 都记录下来。min\_trx\_id 是 m\_ids 里面的最小值，10、18 中最小的显然是 10。当前最大的事务 id 是 18，那么下一个事务的 id 就是 19，max\_trx\_id 就是 19。

==ReadView 生成之后，事务 A 就要去 undo log 版本链中读取值了==。

现在只有一条 undo log 日志，但这并不意味着事务 A 就能读到这条日志的值 X。==它要先判断这行日志的 trx\_id 是否小于当前事务的 min\_trx\_id。看图我们可以很轻松地发现，日志的 trx\_id = 8 小于 ReadView 中 min\_trx\_id = 10。==

==这就意味着，这个事务 A 开始执行之前，修改这行数据的事务已经提交了，所以事务 A 是可以查到值 X 的。==

**在此基础上再增添一点操作，实现可重复读**

我们继续看，事务 A 第一次读完之后，事务 B 要修改这行数据了。undo log 会为所有写操作生成日志，所以就会生成一条 undo log 日志，并且它的 roll\_pointer 会指向上一条 undo log 日志。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192504483.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3RoZXNwcml0,size_16,color_FFFFFF,t_70#pic_center)

紧接着，事务 A 第二次去读这行数据了，情况如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210121192523998.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3RoZXNwcml0,size_16,color_FFFFFF,t_70#pic_center)

第一次读的时候，开启事务 A 的时候就生成了一个 ReadView

此时事务 A 第二次去查询的时候，先查到的是 trx\_id = 18 的那条数据，它会发现 18 比最小的事务编号 10 大。那就说明事务编号为 18 的事务，有可能它是读不到的。

接着就要去 m\_ids 里确认是否有 18 这条数据了。发现有 18，那就说明在事务 A 开启事务的时候，这个事务是没有提交的，它修改的数据就不应该被读到。

事务 A 就会顺着 roll\_pointer 指针继续往下找，找到了 trx\_id = 8 这条日志，发现这条能读，读到的值任然是 x，与第一次读到的结果一致。实现可重复读。