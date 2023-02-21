从·[【史上最全】MySQL各种锁详解：一文搞懂MySQL的各种锁](https://mp.weixin.qq.com/s/mSWS6nCFadjlSohsK_7iqA)

![图片](MySQL%20%E4%B8%AD%E7%9A%84%E5%90%84%E7%A7%8D%E9%94%81%EF%BC%88%E8%AF%A6%E8%A7%A3%EF%BC%89.assets/640.png)

**前言**

锁在 MySQL 中是非常重要的一部分，锁对 MySQL 的数据访问并发有着举足轻重的影响。锁涉及到的知识篇幅也很多，所以要啃完并消化到自己的肚子里，是需要静下心好好反反复复几遍地细细品味。本文是对锁的一个大概的整理，一些相关深入的细节，还是需要找到相关书籍来继续夯实。

**锁的认识**

1.1 锁的解释

计算机协调多个进程或线程并发访问某一资源的机制。

1.2 锁的重要性

在数据库中，除传统计算资源（CPU、RAM、I/O等）的争抢，数据也是一种供多用户共享的资源。

如何保证数据并发访问的一致性，有效性，是所有数据库必须要解决的问题。

锁冲突也是影响数据库并发访问性能的一个重要因素，因此锁对数据库尤其重要。

1.3 锁的缺点

加锁是消耗资源的，锁的各种操作，包括获得锁、检测锁是否已解除、释放锁等 ，都会增加系统的开销。

1.4 简单的例子

现如今网购已经特别普遍了，比如淘宝双十一活动，当天的人流量是千万及亿级别的，但商家的库存是有限的。

系统为了保证商家的商品库存不发生超卖现象，会对商品的库存进行锁控制。当有用户正在下单某款商品最后一件时，

系统会立马对该件商品进行锁定，防止其他用户也重复下单，直到支付动作完成才会释放（支付成功则立即减库存售罄，支付失败则立即释放）。

**锁的类型**

**2.1 表锁**

种类

读锁（read lock），也叫共享锁（shared lock）：针对同一份数据，多个读操作可以同时进行而不会互相影响（select）。

写锁（write lock），也叫排他锁（exclusive lock）：当前操作没完成之前，会阻塞其它读和写操作（update、insert、delete）。

存储引擎默认锁

MyISAM

特点

1\. 对整张表加锁

2\. 开销小

3\. 加锁快

4\. 无死锁

5\. 锁粒度大，发生锁冲突概率大，并发性低

结论

1\. 读锁会阻塞写操作，不会阻塞读操作

2\. 写锁会阻塞读和写操作

建议

MyISAM的读写锁调度是写优先，这也是MyISAM不适合做写为主表的引擎，因为写锁以后，其它线程不能做任何操作，大量的更新使查询很难得到锁，从而造成永远阻塞。

**2.2 行锁**

种类

读锁（read lock），也叫共享锁（shared lock）

允许一个事务去读一行，阻止其他事务获得相同数据集的排他锁

写锁（write lock），也叫排他锁（exclusive lock）

允许获得排他锁的事务更新数据，阻止其他事务取得相同数据集的共享锁和排他锁

意向共享锁（IS）

一个事务给一个数据行加共享锁时，必须先获得表的IS锁

意向排它锁（IX）

一个事务给一个数据行加排他锁时，必须先获得该表的IX锁

存储引擎默认锁

InnoDB

特点

1\. 对一行数据加锁

2\. 开销大

3\. 加锁慢

4\. 会出现死锁

5\. 锁粒度小，发生锁冲突概率最低，并发性高

事务并发带来的问题

1\. 更新丢失

解决：让事务变成串行操作，而不是并发的操作，即对每个事务开始---对读取记录加排他锁

2\. 脏读

解决：隔离级别为Read uncommitted

3\. 不可重读

解决：使用Next-Key Lock算法来避免

4\. 幻读

解决：间隙锁（Gap Lock）

**2.3 页锁**

开销、加锁时间和锁粒度介于表锁和行锁之间，会出现死锁，并发处理能力一般（此锁不做多介绍）

**如何上锁？**

3.1 表锁

隐式上锁（默认，自动加锁自动释放）

```
select //上读锁
```

显式上锁（手动）

```
lock table tableName read;//读锁
```

解锁（手动）

```
unlock tables;//所有锁表
```

```
lock table teacher read;// 上读锁
```

**3.2 行锁**

隐式上锁（默认，自动加锁自动释放）

```
select //不会上锁
```

显式上锁（手动）

```
select * from tableName lock in share mode;//读锁
```

解锁（手动）

_1\. 提交事务（commit）_

_2\. 回滚事务（rollback）_

_3\. kill 阻塞进程_

```
begin;
```

```
begin;
```

> 为什么上了写锁，别的事务还可以读操作？
>
> 因为InnoDB有_**MVCC机制（多版本并发控制）**_，可以使用快照读，而不会被阻塞。

**行锁的实现算法**

**4.1 Record Lock 锁**

单个行记录上的锁（点查）

Record Lock总是会去锁住索引记录，如果InnoDB存储引擎表建立的时候没有设置任何一个索引，这时InnoDB存储引擎会使用隐式的主键来进行锁定

**4.2 Gap Lock 锁（where范围数据）**

当我们用范围条件而不是相等条件检索数据，并请求共享或排他锁时，InnoDB会给符合条件的已有数据记录的索引加锁，对于键值在条件范围内但并不存在的记录。

优点：解决了事务并发的幻读问题

不足：因为query执行过程中**通过范围查找**的话，他会_锁定整个范围内所有的索引键值，即使这个键值并不存在。_

间隙锁有一个致命的弱点，就是当锁定一个范围键值之后，即使某些不存在的键值也会被无辜的锁定，而造成锁定的时候无法插入锁定键值范围内任何数据。在某些场景下这可能会对性能造成很大的危害。

**4.3 Next-key Lock 锁**

同时锁住数据+间隙锁

在Repeatable Read隔离级别下，Next-key Lock 算法是默认的行记录锁定算法。

**4.4 行锁的注意点**

1\. 只有通过索引条件检索数据时，InnoDB才会使用行级锁，否则会使用表级锁(索引失效，行锁变表锁)

2\. 即使是访问不同行的记录，如果使用的是相同的索引键，会发生锁冲突

3\. 如果数据表建有多个索引时，可以通过不同的索引锁定不同的行

**如何排查锁？**

**5.1 表锁**

查看表锁情况

show open tables;

**表锁分析**

show status like 'table%';

_1\. table\_locks\_waited_

出现表级锁定争用而发生等待的次数（不能立即获取锁的次数，每等待一次值加1），此值高说明存在着较严重的表级锁争用情况

_2\. table\_locks\_immediate_

产生表级锁定次数，不是可以立即获取锁的查询次数，每立即获取锁加1

**5.2 行锁**

行锁分析

```
show status like 'innodb_row_lock%';
```

1\. innodb\_row\_lock\_current\_waits //当前正在等待锁定的数量

2\. innodb\_row\_lock\_time //从系统启动到现在锁定总时间长度

3\. innodb\_row\_lock\_time\_avg //每次等待所花平均时间

4\. innodb\_row\_lock\_time\_max //从系统启动到现在等待最长的一次所花时间

5\. innodb\_row\_lock\_waits //系统启动后到现在总共等待的次数

information\_schema 库

1\. innodb\_lock\_waits表

2\. innodb\_locks表

3\. innodb\_trx表

优化建议

1\. 尽可能让所有数据检索都通过索引来完成，避免无索引行锁升级为表锁

2\. 合理设计索引，尽量缩小锁的范围

3\. 尽可能较少检索条件，避免间隙锁

4\. 尽量控制事务大小，减少锁定资源量和时间长度

5\. 尽可能低级别事务隔离

**死锁**

6.1 解释

指两个或者多个事务在同一资源上相互占用，并请求锁定对方占用的资源，从而导致恶性循环的现象

6.2 产生的条件

1\. 互斥条件：一个资源每次只能被一个进程使用

2\. 请求与保持条件：一个进程因请求资源而阻塞时，对已获得的资源保持不放

3\. 不剥夺条件：进程已获得的资源，在没有使用完之前，不能强行剥夺

4\. 循环等待条件：多个进程之间形成的一种互相循环等待的资源的关系

6.1 解决

1\. 查看死锁：show engine innodb status \\G

2\. 自动检测机制，超时自动回滚代价较小的事务（innodb\_lock\_wait\_timeout 默认50s）

3\. 人为解决，kill阻塞进程（show processlist）

4\. wait for graph 等待图（主动检测）

6.1 如何避免

1\. 加锁顺序一致，尽可能一次性锁定所需的数据行

2\. 尽量基于primary（主键）或unique key更新数据

3\. 单次操作数据量不宜过多，涉及表尽量少

4\. 减少表上索引，减少锁定资源

5\. 尽量使用较低的隔离级别

6\. 尽量使用相同条件访问数据，这样可以避免间隙锁对并发的插入影响

7\. 精心设计索引，尽量使用索引访问数据

8\. 借助相关工具：pt-deadlock-logger

**乐观锁与悲观锁**

7.1 悲观锁

解释

假定会发生并发冲突，屏蔽一切可能违反数据完整性的操作

实现机制

表锁、行锁等

实现层面

数据库本身

适用场景

并发量大

7.2 乐观锁

解释

假设不会发生并发冲突，只在提交操作时检查是否违反数据完整性

实现机制

提交更新时检查版本号或者时间戳是否符合

实现层面

业务代码

适用场景

并发量小

## MySQL锁机制起步

锁是计算机用以协调多个进程间并发访问同一共享资源的一种机制。MySQL中为了保证数据访问的一致性与有效性等功能，实现了锁机制，MySQL中的锁是在服务器层或者存储引擎层实现的。

**MVCC ，多版本并发控制，Multiversion Concurrency Control**

　　大部分的MySQL的存储 引擎，比如InnoDB，Falcon，以及PBXT并不是简简单单的使用行锁机制。它们都使用了行锁结合一种提高并发的技术，被称为MVCC（多版本并发控制）。MVCC并不单单应用在MySQL中，其他的数据库如Oracle,PostgreSQL,以及其他数据库也使用这个技术。

　　MVCC避免了许多需要加锁的情形以及降低消耗。这取决于它实现的方式，它允许**非阻塞读**取，在写的操作的时候阻塞必要的记录。

　　MVCC保存了**某一时刻数据的一个快照**。意思就是无论事物运行了多久，它们都能看到一致的数据。也就是说在相同的时间下，不同的事务，查询相同表的数据是不同的。如果你从来没有这方面的经验，可能说这些有点令人困惑。但是在以后这个会很容易理解和熟悉的。

　　每个存储引擎实现MVCC方式都是不同的。有许多种包含了乐观（optimistic）和悲观（pessimistic）的并发控制。

我们用简单的InnoDb的行为来举例说明MVCC工作方式。

　 　InnoDB实现MVCC的方法是，它存储了**每一行的两个额外的隐藏字段**，这两个隐藏字段分别记录了**行的创建的时间和删除的时间**。在每个事件发生的时 候，每行存储**版本号**，而不是存储事件实际发生的时间。每次事务的开始这个版本号都会增加。

自记录时间开始，每个事务都会保存记录的系统版本号。依照事务的版本来检查每行的版本号。在事务隔离级别为可重复读（RR）的情况下，来看看怎样应用它。

　　SELECT

　　InnoDB检查每行，要确定它符合两个标准。

　　InnoDB必须知道行的版本号，这个行的版本号至少要和事物版本号一样的老。（也就是是说它的版本号可能少于或者和事物版本号相同）。这个既能确定事物开始之前行是存在的，也能确定事物创建或修改了这行。

　　行的删除操作的版本一定是未定义的或者大于事物的版本号。确定了事物开始之前，行没有被删除。

　　符合了以上两点。会返回查询结果。

　　INSERT

　　InnoDB记录了当前新增行的系统版本号。

　　DELETE

　　InnoDB记录的删除行的系统版本号作为行的删除ID。

　　UPDATE

　　InnoDB复制了一行。这个新行的版本号使用了系统版本号。它也把系统版本号作为了删除行的版本。

　　所有其他记录的结果保存是，从未获得锁的查询。这样它们查询的数据就会尽可能的快。要确定查询行要遵循这些标准。缺点是存储引擎要为每一行存储更多的数据，检查行的时候要做更多的处理以及其他内部的一些操作。

　　MVCC只能在可重复读和可提交读的隔离级别下生效。不可提交读不能使用它的原因是不能读取符合事物版本的行版本。它们总是读取最新的行版本。可序列化不能使用MVCC的原因是，它总是要锁定行。

　　下面的表说明了在MySQL中不同锁的模式以及并发级别。

<table><tbody><tr><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px; word-break: break-all;"><strong><span>锁的策略 　 　 　 　 　 　 　 　 　　</span></strong></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><strong><span>并发性 　 　 　 　 　 　 　　</span></strong></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px; word-break: break-all;"><strong><span>开销 　 　 　 　 　 　 　 　　</span></strong></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><strong><span>引擎</span></strong></td></tr><tr><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>表</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>最低</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>最低</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>MyISAM,Merge,Memory</span></td></tr><tr><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>行</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>高</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>高</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>NDB Cluster</span></td></tr><tr><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>行和MVCC</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>最高</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>最高</span></td><td data-style="font-size: 12px; color: rgb(69, 69, 69); font-family: Verdana, Geneva, Arial, Helvetica, sans-serif; border-color: rgb(192, 192, 192); border-collapse: collapse; padding: 8px 14px; min-width: 50px;"><span>InnoDB,Falcon,PBXT,solidD</span></td></tr></tbody></table>

## 行锁与表锁

首先我们来了解行锁与表锁的基本概念，从名字中我们就可以了解：表锁就是对整张表进行加锁，而行锁则是锁定某行、某几行数据或者行之间的间隙。

各引擎对锁的支持情况如下：

|   
 | 行锁 | 表锁 | 页锁 |
| --- | --- | --- | --- |
| MyISAM |   
 | √ |   
 |
| BDB |   
 | √ | √ |
| InnoDB | √ | √ |   
 |

### 行锁

> A record lock is a lock on an index record. Record locks always lock index records, even if a table is defined with no indexes. For such cases, InnoDB creates a hidden clustered index and uses this index for record locking.

上文出自MySQL的官方文档，从这里我们可以看出行锁是作用在索引上的，哪怕你在建表的时候没有定义一个索引，InnoDB也会创建一个聚簇索引并将其作为锁作用的索引。

这里还是讲一下InnoDB中的聚簇索引。每一个InnoDB表都需要一个聚簇索引，有且只有一个。如果你为该表表定义一个主键，那么MySQL将使用主键作为聚簇索引；如果你不为定义一个主键，那么MySQL将会把第一个唯一索引（而且要求NOT NULL）作为聚簇索引；如果上诉两种情况都GG，那么MySQL将自动创建一个名字为`GEN_CLUST_INDEX`的隐藏聚簇索引。

因为是聚簇索引，所以B+树上的叶子节点都存储了数据行，那么如果现在是二级索引呢？InnoDB中的二级索引的叶节点存储的是主键值（或者说聚簇索引的值），所以通过二级索引查询数据时，还需要将对应的主键去聚簇索引中再次进行查询。

关于索引的问题就到这，我们用一张直观的图来表示行锁：

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

接下来以两条SQL的执行为例，讲解一下InnoDB对于单行数据的加锁原理：

```
Copyupdate user set age = 10 where id = 49;update user set age = 10 where name = 'Tom';
```

第一条SQL使用主键查询，只需要在 id = 49 这个主键索引上加上锁。第二条 SQL 使用二级索引来查询，那么首先在 name = Tom 这个索引上加写锁，然后由于使用 InnoDB 二级索引还需再次根据主键索引查询，所以还需要在 id = 49 这个主键索引上加锁。

也就是说使用主键索引需要加一把锁，使用二级索引需要在二级索引和主键索引上各加一把锁。

根据索引对单行数据进行更新的加锁原理了解了，那如果更新操作涉及多个行呢，比如下面 SQL 的执行场景。

```
Copyupdate user set age = 10 where id > 49;
```

上述 SQL 的执行过程如下图所示。MySQL Server 会根据 WHERE 条件读取第一条满足条件的记录，然后 InnoDB 引擎会将第一条记录返回并加锁，接着 MySQL Server 发起更新改行记录的 UPDATE 请求，更新这条记录。一条记录操作完成，再读取下一条记录，直至没有匹配的记录为止。

### 表锁

上面我们讲解行锁的时候，操作语句中的条件判断列都是有建立索引的，那么如果现在的判断列不存在索引呢？InnoDB既支持行锁，也支持表锁，当没有查询列没有索引时，InnoDB就不会去搞什么行锁了，毕竟行锁一定要有索引，所以它现在搞表锁，把整张表给锁住了。那么具体啥是表锁？还有其他什么情况下也会进行锁表呢？

表锁使用的是一次性锁技术，也就是说，在会话开始的地方使用 lock 命令将后续需要用到的表都加上锁，在表释放前，只能访问这些加锁的表，不能访问其他表，直到最后通过 unlock tables 释放所有表锁。

除了使用 unlock tables 显示释放锁之外，会话持有其他表锁时执行lock table 语句会释放会话之前持有的锁；会话持有其他表锁时执行 start transaction 或者 begin 开启事务时，也会释放之前持有的锁。

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

表锁由 MySQL Server 实现，行锁则是存储引擎实现，不同的引擎实现的不同。在 MySQL 的常用引擎中 InnoDB 支持行锁，而 MyISAM 则只能使用 MySQL Server 提供的表锁。

### 两种锁的比较

表锁：加锁过程的开销小，加锁的速度快；不会出现死锁的情况；锁定的粒度大，发生锁冲突的几率大，并发度低；

-   一般在执行DDL语句时会对整个表进行加锁，比如说 ALTER TABLE 等操作；
    
-   如果对InnoDB的表使用行锁，被锁定字段不是主键，也没有针对它建立索引的话，那么将会锁整张表；
    
-   表级锁更适合于以查询为主，并发用户少，只有少量按索引条件更新数据的应用，如Web 应用。
    

行锁：加锁过程的开销大，加锁的速度慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低，并发度也最高；

-   最大程度的支持并发，同时也带来了最大的锁开销。
    
-   在 InnoDB 中，除单个 SQL 组成的事务外，锁是逐步获得的，这就决定了在 InnoDB 中发生死锁是可能的。
    
-   行级锁只在存储引擎层实现，而 MySQL 服务器层没有实现。行级锁更适合于有大量按索引条件并发更新少量不同数据，同时又有并发查询的应用，如一些在线事务处理（OLTP）系统。
    

## MyISAM表锁

### MyISAM表级锁模式

-   表共享读锁（Table Read Lock）：不会阻塞其他线程对同一个表的读操作请求，但会阻塞其他线程的写操作请求；
    
-   表独占写锁（Table Write Lock）：一旦表被加上独占写锁，那么无论其他线程是读操作还是写操作，都会被阻塞；
    

默认情况下，写锁比读锁具有更高的优先级；当一个锁释放后，那么它会优先相应写锁等待队列中的锁请求，然后再是读锁中等待的获取锁的请求。

> This ensures that updates to a table are not “starved” even when there is heavy SELECT activity for the table. However, if there are many updates for a table, SELECT statements wait until there are no more updates.

这种设定也是MyISAM表不适合于有大量更新操作和查询操作的原因。大量更新操作可能会造成查询操作很难以获取读锁，从而过长的阻塞。同时一些需要长时间运行的查询操作，也会使得线程“饿死”，应用中应尽量避免出现长时间运行的查询操作（在可能的情况下可以通过使用中间表等措施对SQL语句做一定的“分解”，使每一步查询都能在较短的时间内完成，从而减少锁冲突。如果复杂查询不可避免，应尽量安排在数据库空闲时段执行，比如一些定期统计可以安排在夜间执行。）

我们可以通过一些设置来调节MyISAM的调度行为：

-   通过指定启动参数`low-priority-updates`，使MyISAM引擎默认给予读请求以优先的权利；
    
-   通过执行命令`SET LOW_PRIORITY_UPDATES=1`，使该连接发出的更新请求优先级降低；
    
-   通过指定INSERT、UPDATE、DELETE语句的`LOW_PRIORITY`属性，降低该语句的优先级；
    
-   给系统参数`max_write_lock_count`设置一个合适的值，当一个表的读锁达到这个值后，MySQL就暂时将写请求的优先级降低，给读进程一定获得锁的机会。
    

### MyISAM对表加锁分析

MyISAM在执行查询语句（SELECT）前，会自动给涉及的所有表加读锁，在执行更新操作（UPDATE、DELETE、INSERT等）前，会自动给涉及的表加写锁，这个过程并不需要用户干预，因此用户一般不需要直接用 LOCK TABLE 命令给 MyISAM 表显式加锁。在自动加锁的情况下，MyISAM 总是一次获得 SQL 语句所需要的全部锁，这也正是 MyISAM 表不会出现死锁（Deadlock Free）的原因。

MyISAM存储引擎支持并发插入，以减少给定表的读操作和写操作之间的争用：

如果MyISAM表在数据文件中没有空闲块（由于删除或更新导致的空行），则行始终插入数据文件的末尾。在这种情况下，你可以自由混合并发使用MyISAM表的 INSERT 和 SELECT 语句而不需要加锁（你可以在其他线程进行读操作的情况下，同时将行插入到MyISAM表中）。如果文件中有空闲块，则并发插入会被禁止，但当所有的空闲块重新填充有新数据时，它又会自动启用。要控制此行为，可以使用MySQL的concurrent\_insert系统变量。

-   当concurrent\_insert=0时，不允许并发插入功能。
    
-   当concurrent\_insert=1时，允许对没有空闲块的表使用并发插入，新数据位于数据文件结尾（缺省）。
    
-   当concurrent\_insert=2时，不管表有没有空想快，都允许在数据文件结尾并发插入。
    

### 显式加表锁的应用

上面已经提及了表锁的加锁方式，一般表锁都是隐式加锁的，不需要我们去主动声明，但是也有需要显式加锁的情况，这里简单做下介绍：

给MyISAM表显式加锁，一般是为了一定程度模拟事务操作，实现对某一时间点多个表的一致性读取。例如，有一个订单表orders，其中记录有订单的总金额total，同时还有一个订单明细表 order\_detail，其中记录有订单每一产品的金额小计 subtotal，假设我们需要检查这两个表的金额合计是否相等，可能就需要执行如下两条SQL：

```
CopySELECT SUM(total) FROM orders;SELECT SUM(subtotal) FROM order_detail;
```

这时，如果不先给这两个表加锁，就可能产生错误的结果，因为第一条语句执行过程中，order\_detail表可能已经发生了改变。因此，正确的方法应该是：

```
CopyLOCK tables orders read local,order_detail read local;SELECT SUM(total) FROM orders;SELECT SUM(subtotal) FROM order_detail;Unlock tables;
```

### 查看表锁争用情况：

可以通过检查 table\_locks\_waited 和 table\_locks\_immediate 状态变量来分析系统上的表锁的争夺，如果 Table\_locks\_waited 的值比较高，则说明存在着较严重的表级锁争用情况：

```
Copymysql> SHOW STATUS LIKE 'Table%';+-----------------------+---------+| Variable_name | Value |+-----------------------+---------+| Table_locks_immediate | 1151552 || Table_locks_waited | 15324 |+-----------------------+---------+
```

## InnoDB行锁与表锁

### InnoDB锁模式

1）InnoDB中的行锁

InnoDB实现了以下两种类型的行锁：

-   共享锁（S）：加了锁的记录，所有事务都能去读取但不能修改，同时阻止其他事务获得相同数据集的排他锁；
    
-   排他锁（X）：允许已经获得排他锁的事务去更新数据，阻止其他事务取得相同数据集的共享读锁和排他写锁；
    

2）InnoDB表锁——意向锁

由于表锁和行锁虽然锁定范围不同，但是会相互冲突。当你要加表锁时，势必要先遍历该表的所有记录，判断是否有排他锁。这种遍历检查的方式显然是一种低效的方式，MySQL引入了意向锁，来检测表锁和行锁的冲突。

> Intention locks are table-level locks that indicate which type of lock (shared or exclusive) a transaction requires later for a row in a table。
>
> The intention locking protocol is as follows:
>
> -   Before a transaction can acquire a shared lock on a row in a table, it must first acquire an IS lock or stronger on the table.
>
> -   Before a transaction can acquire an exclusive lock on a row in a table, it must first acquire an IX lock on the table.
>

意向锁也是表级锁，分为读意向锁（IS锁）和写意向锁（IX锁）。当事务要在记录上加上行锁时，要首先在表上加上意向锁。这样判断表中是否有记录正在加锁就很简单了，只要看下表上是否有意向锁就行了，从而就能提高效率。

意向锁之间是不会产生冲突的，它只会阻塞表级读锁或写锁。意向锁不于行级锁发生冲突。

### 锁模式的兼容矩阵

下面表显示了了各种锁之间的兼容情况：

|   
 | X | IX | S | IS |
| --- | --- | --- | --- | --- |
| X |   
 |   
 |   
 |   
 |
| IX |   
 | 兼容 |   
 | 兼容 |
| S |   
 |   
 | 兼容 | 兼容 |
| IS |   
 | 兼容 | 兼容 | 兼容 |

（注意上面的X与S是说表级的X锁和S锁，意向锁不和行级锁发生冲突）

如果一个事务请求的锁模式与当前的锁兼容，InnoDB就将请求的锁授予该事务；如果两者不兼容，那么该事务就需要等待锁的释放。

### InnoDB的加锁方法

-   意向锁是 InnoDB 自动加的，不需要用户干预；
    
-   对于UPDATE、DELETE和INSERT语句，InnoDB会自动给涉及的数据集加上排他锁；
    
-   对于普通的SELECT语句，InnoDB不会加任何锁；事务可以通过以下语句显示给记录集添加共享锁或排他锁：
    
-   共享锁（S）：`select * from table_name where ... lock in share mode`。此时其他 session 仍然可以查询记录，并也可以对该记录加 share mode 的共享锁。但是如果当前事务需要对该记录进行更新操作，则很有可能造成死锁。
    
-   排他锁（X）：`select * from table_name where ... for update`。其他session可以查询记录，但是不能对该记录加共享锁或排他锁，只能等待锁释放后在加锁。
    

#### select for update

在执行这个 select 查询语句的时候，会将对应的索引访问条目加上排他锁（X锁），也就是说这个语句对应的锁就相当于update带来的效果；

**使用场景**：为了让确保自己查找到的数据一定是最新数据，并且查找到后的数据值允许自己来修改，此时就需要用到select for update语句；

**性能分析**：select for update语句相当于一个update语句。在业务繁忙的情况下，如果事务没有及时地commit或者rollback可能会造成事务长时间的等待，从而影响数据库的并发使用效率。

#### select lock in share mode

in share mode 子句的作用就是将查找的数据加上一个share锁，这个就是表示其他的事务只能对这些数据进行简单的 select 操作，而不能进行 DML 操作。

**使用场景**：为了确保自己查询的数据不会被其他事务正在修改，也就是确保自己查询到的数据是最新的数据，并且不允许其他事务来修改数据。与select for update不同的是，本事务在查找完之后不一定能去更新数据，因为有可能其他事务也对同数据集使用了 in share mode 的方式加上了S锁；

**性能分析**：select lock in share mode 语句是一个给查找的数据上一个共享锁（S 锁）的功能，它允许其他的事务也对该数据上S锁，但是不能够允许对该数据进行修改。如果不及时的commit 或者rollback 也可能会造成大量的事务等待。

### InnoDB的锁争用情况

可以通过检查 InnoDB\_row\_lock 状态变量来分析系统上的行锁的争夺情况：

```
Copymysql> show status like 'innodb_row_lock%'; +-------------------------------+-------+ | Variable_name | Value | +-------------------------------+-------+ | InnoDB_row_lock_current_waits | 0 | | InnoDB_row_lock_time | 0 | | InnoDB_row_lock_time_avg | 0 | | InnoDB_row_lock_time_max | 0 | | InnoDB_row_lock_waits | 0 | +-------------------------------+-------+ 5 rows in set (0.01 sec)
```

## 行锁的类型

上面我们根据了锁的粒度将锁分为了行锁与表锁，接下来根据使用场景的不同，又可以将行锁进行进一步的划分：Next-Key Lock、Gap Lock、Record Lock以及插入意向GAP锁。

不同的锁锁定的位置是不同的，比如说记录锁只锁定对应的记录，而间隙锁锁住记录和记录之间的间隙，Next-key Lock则锁住所属记录之间的间隙。不同的锁类型锁定的范围大致如图所示：

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

### 记录锁（Record Lock）

记录锁最简单的一种行锁形式，上面我们以及稍微提及过了。这里补充下的点就是：行锁是加在索引上的，如果当你的查询语句不走索引的话，那么它就会升级到表锁，最终造成效率低下，所以在写SQL语句时需要特别注意。

### 间隙锁（Gap Lock）

> A gap lock is a lock on a gap between index records, or a lock on the gap before the first or after the last index record。

当我们使用范围条件而不是相等条件去检索，并请求锁时，InnoDB就会给符合条件的记录的索引项加上锁；而对于键值在条件范围内但并不存在（参考上面所说的空闲块）的记录，就叫做间隙，InnoDB在此时也会对间隙加锁，这种记录锁+间隙锁的机制叫Next-Key Lock。额，扯的有点快。

从上面这句话可以表明间隙锁是所在两个存在的索引之间，是一个开区间，像最开始的那张索引图，15和18之间，是有（16，17）这个间隙存在的。

> Gap locks in InnoDB are “purely inhibitive”, which means that their only purpose is to prevent other transactions from inserting to the gap. Gap locks can co-exist. A gap lock taken by one transaction does not prevent another transaction from taking a gap lock on the same gap. There is no difference between shared and exclusive gap locks. They do not conflict with each other, and they perform the same function.

上面这段话表明间隙锁是可以共存的，共享间隙锁与独占间隙锁之间是没有区别的，两者之间并不冲突。其存在的目的都是防止其他事务往间隙中插入新的纪录，故而一个事务所采取的间隙锁是不会去阻止另外一个事务在同一个间隙中加锁的。

当然也不是在什么时候都会去加间隙锁的：

> Gap locking can be disabled explicitly. This occurs if you change the transaction isolation level to READ COMMITTED. Under these circumstances, gap locking is disabled for searches and index scans and is used only for foreign-key constraint checking and duplicate-key checking.

这段话表明，在 RU 和 RC 两种隔离级别下，即使你使用 select in share mode 或 select for update，也无法防止**幻读**（读后写的场景）。因为这两种隔离级别下只会有**行锁**，而不会有**间隙锁**。而如果是 RR 隔离级别的话，就会在间隙上加上间隙锁。

### 临键锁（Next-key Lock）

> A next-key lock is a combination of a record lock on the index record and a gap lock on the gap before the index record.

临键锁是记录锁与与间隙锁的结合，所以临键锁与间隙锁是一个同时存在的概念，并且临键锁是个左开有闭的却比如(16, 18\]。

关于临键锁与幻读，官方文档有这么一条说明：

> By default, InnoDB operates in REPEATABLE READ transaction isolation level. In this case, InnoDB uses next-key locks for searches and index scans, which prevents phantom rows.

就是说 MySQL 默认隔离级别是RR，在这种级别下，如果你使用 select in share mode 或者 select for update 语句，那么InnoDB会使用临键锁（记录锁 + 间隙锁），因而可以防止幻读；

但是我也在网上看到相关描述：即使你的隔离级别是 RR，如果你这是使用普通的select语句，那么此时 InnoDB 引擎将是使用快照读，而不会使用任何锁，因而还是无法防止幻读。（其实普通读应该是快照读没错，但是快照读应该是不会有幻读幻读问题，mmp）。

### 插入意向锁（Insert Intention Lock）

> An insert intention lock is a type of gap lock set by INSERT operations prior to row insertion. This lock signals the intent to insert in such a way that multiple transactions inserting into the same index gap need not wait for each other if they are not inserting at the same position within the gap. Suppose that there are index records with values of 4 and 7. Separate transactions that attempt to insert values of 5 and 6, respectively, each lock the gap between 4 and 7 with insert intention locks prior to obtaining the exclusive lock on the inserted row, but do not block each other because the rows are nonconflicting.

官方文档已经解释得很清楚了，这里我做个翻译机：

插入意图锁是一种间隙锁，在行执行 INSERT 之前的插入操作设置。如果多个事务 INSERT 到同一个索引间隙之间，但没有在同一位置上插入，则不会产生任何的冲突。假设有值为4和7的索引记录，现在有两事务分别尝试插入值为 5 和 6 的记录，在获得插入行的排他锁之前，都使用插入意向锁锁住 4 和 7 之间的间隙，但两者之间并不会相互阻塞，因为这两行并不冲突。

插入意向锁只会和 间隙或者 Next-key 锁冲突，正如上面所说，间隙锁作用就是防止其他事务插入记录造成幻读，正是由于在执行 INSERT 语句时需要加插入意向锁，而插入意向锁和间隙锁冲突，从而阻止了插入操作的执行。

### 不同类型锁之间的兼容

不同类型的锁之间的兼容如下表所示：

|   
 | RECORED | GAP | NEXT-KEY | II GAP（插入意向锁） |
| --- | --- | --- | --- | --- |
| RECORED |   
 | 兼容 |   
 | 兼容 |
| GAP | 兼容 | 兼容 | 兼容 | 兼容 |
| NEXT-KEY |   
 | 兼容 |   
 | 兼容 |
| II GAP | 兼容 |   
 |   
 | 兼容 |

（其中行表示已有的锁，列表示意图加上的锁）

其中，第一行表示已有的锁，第一列表示要加的锁。插入意向锁较为特殊，所以我们先对插入意向锁做个总结，如下：

-   插入意向锁不影响其他事务加其他任何锁。也就是说，一个事务已经获取了插入意向锁，对其他事务是没有任何影响的；
    
-   插入意向锁与间隙锁和 Next-key 锁冲突。也就是说，一个事务想要获取插入意向锁，如果有其他事务已经加了间隙锁或 Next-key 锁，则会阻塞。
    

其他类型的锁的规则较为简单：

-   间隙锁不和其他锁（不包括插入意向锁）冲突；
    
-   记录锁和记录锁冲突，Next-key 锁和 Next-key 锁冲突，记录锁和 Next-key 锁冲突；
    

> 文章写到这里吧，再写下去有点长了。上面文章中很多信息都来源网络，我只是个搬运工，假若哪里有表述错误，请评论区留言。

参考资料：

-   InnoDB Locking
    
-   把MySQL中的各种锁及其原理都画出来
    
-   MySQL中的锁（表锁、行锁）
    
-   关于MySQL MyISAM 表并发
    

___

**【更多阅读：禅与计算机程序设计艺术**】****

-   [软件架构的本质](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658342245&idx=1&sn=2670cf7e8ee958c2c388f5ee7fcb1cd0&chksm=8b02bafcbc7533ea5aad6a04347ee24915842a8f9e98f93261e0bd87e810f7f678dce2471d1c&scene=21#wechat_redirect)  
    
-   [CORBA 架构体系指南（通用对象请求代理体系架构）](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658342223&idx=1&sn=85524b931628f126c255a848abc31b27&chksm=8b02bad6bc7533c01a16fb039ccf22c0868fb874b8563cb877fdcd5bbaba9a09a4031835af00&scene=21#wechat_redirect)  
    
-   [软件架构师成长之路: Master Plan for becoming a Software Architect](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658342058&idx=1&sn=4ed296175a74307f90f549342ef58c7b&chksm=8b02bb33bc753225ebb39325f858de5fa19dad28ef28cd2f610c9c1829d0c17d19130cd70b37&scene=21#wechat_redirect)  
    
-   [快看软件架构风格总结: 各种历史和现代软件架构风格的快速总结](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658342058&idx=2&sn=9a88a4aa1934f5ad20f1b8f84fc02a1d&chksm=8b02bb33bc753225d7cc629a44a370d75cf0fba32c09dc9220439fcce6fa9233c10b6a4f9e6a&scene=21#wechat_redirect)  
    
-   [怎样才算是好程序员？关于好程序员与好代码的杂谈](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658342030&idx=1&sn=b805fef075a3c54623d8f0cc78253729&chksm=8b02bb17bc753201948127dc6bfcb6a19a95018fa2aca14f8272b021a59670a93f3fd61d8588&scene=21#wechat_redirect)  
    
-   [关于软件架构设计的核心思想与标准 ( IEEE 1471 2000 )](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658341912&idx=1&sn=e3d277d741a3013988ab0f5434d31cfb&chksm=8b02bb81bc753297439fb12b6c136153c49348c63afd60aa4d412a3b867c1aedb0e2845f5231&scene=21#wechat_redirect)  
    
-   [关系代数（Relational Algebra）——极简教程](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658341900&idx=1&sn=babce99e4abfdda61da25c7f1cbca6fa&chksm=8b02bb95bc7532839ed127ef53961375be95bf9a7c737ba90266815806230a7c51f8ae6795f5&scene=21#wechat_redirect)  
    
-   [【操作系统架构原理】资源管理技术与进程的抽象设计思想](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658341175&idx=1&sn=1e30c5247586c3b7f6d9be89ff580370&chksm=8b02beaebc7537b8ccfec568c8d6e63e77096435a4b75cfd0c19a81f401de52aa3d6730f5706&scene=21#wechat_redirect)  
    
-   [软件“生命”系统进化论——软件以负熵为生！](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658341156&idx=1&sn=f42fdd905706fdcc621aa4820157a826&chksm=8b02bebdbc7537abb8e23e832fae6aeccd9cb53cdb7ca6d4cc91d93eaeb459dd4f715add9542&scene=21#wechat_redirect)  
    
-   [图文详解: 操作系统之内存管理 ( 内存模型,虚拟内存,MMU, TLB,页面置换算法,分段等)](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340906&idx=1&sn=2e6094bfcdec4a8ebd2b3ff573ef4c09&chksm=8b02bfb3bc7536a52d52b0436fef393f38dfb9870db7e9c2b09b1e11a8769895178230000eae&scene=21#wechat_redirect)  
    
-   [成为架构师系列: 怎样画系统架构图? 背后的本质是对问题的本质思考](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340844&idx=1&sn=643a09d018bf9cf4b99044875ff86084&chksm=8b02a075bc752963c557b6994d3619091cbbdc99cdeaec605ed9baa8e48dfdaaff09f6e3a63a&scene=21#wechat_redirect)  
    
-   [《编程的原则：改善代码质量的101个方法》读书笔记](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340541&idx=1&sn=8e4359ca7a0ca9bee065205cf30a45f4&chksm=8b02a124bc752832b924f09a586ca064a243d8067cea674dcef9e44f2f2590c6a45f86b9bf0a&scene=21#wechat_redirect)  
    
-   [UNIX 设计哲学：Do one thing and do it well](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340535&idx=1&sn=44afc0e8b932f8936c0414a485e4ee35&chksm=8b02a12ebc7528389c215b784e7f9c9fb60956342fbce466ab3f780720191235ceab263f2944&scene=21#wechat_redirect)  
    
-   [计算简史：什么是计算机？《禅与计算机程序设计艺术》](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340520&idx=1&sn=6ff7d47e7cbe836a29d4076f064b1ac7&chksm=8b02a131bc75282721a19d708db05f4b507e57f3c89ad96e8a05ce0828a65fbd836d729b148e&scene=21#wechat_redirect)  
    
-   [编程语言进化史《禅与计算机程序设计艺术》](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340520&idx=3&sn=5ad7ea763a5a2c4c6cd88cd6f9f51110&chksm=8b02a131bc7528277afa4a3c68a8317d205808fdde04998d6e3fbe5709a43d87cc0f6f0008b3&scene=21#wechat_redirect)  
    
-   [“风味人间”与计算机程序设计艺术《禅与计算机程序设计艺术》](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340469&idx=1&sn=0c21c12b45008026506d55fe0c4db071&chksm=8b02a1ecbc7528fa8d29432c78b843ddd1dbdb7f08ef16fdc29d58bd4749ef9241f34cb3ec97&scene=21#wechat_redirect)  
    
-   [编程为什么有趣？浅谈编程的快乐。](http://mp.weixin.qq.com/s?__biz=MzA5OTI2MTE3NA==&mid=2658340316&idx=1&sn=304159f71e578df58a93cff5283248eb&chksm=8b02a245bc752b5394ed262525041ce6a7fb2c5c09f86415cc8a17f33f230c36756be8801255&scene=21#wechat_redirect)  
    

___

附：MySQL中的锁思维导图：  

![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)