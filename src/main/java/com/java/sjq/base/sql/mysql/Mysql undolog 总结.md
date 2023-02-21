[MySQL回滚日志（undo log）总结](https://blog.csdn.net/Weixiaohuai/article/details/117867353)

___

### 一、undo log的概念

undo log是mysql中比较重要的事务日志之一，顾名思义，undo log是一种用于撤销回退的日志，在事务没提交之前，MySQL会先记录更新前的数据到 undo log日志文件里面，当[事务回滚](https://so.csdn.net/so/search?q=%E4%BA%8B%E5%8A%A1%E5%9B%9E%E6%BB%9A&spm=1001.2101.3001.7020)时或者数据库崩溃时，可以利用 undo log来进行回退。

### 二、undo log的作用

在MySQL中，undo log日志的作用主要有两个：

-   ### 1、提供回滚操作【undo log实现事务的原子性】

我们在进行数据更新操作的时候，不仅会记录redo log，还会记录undo log，如果因为某些原因导致事务回滚，那么这个时候MySQL就要执行回滚（rollback）操作，利用undo log将数据恢复到事务开始之前的状态。

如我们执行下面一条删除语句：

```
delete from user where id = 1;
```

那么此时undo log会记录一条对应的insert 语句【反向操作的语句】，以保证在事务回滚时，将数据还原回去。

再比如我们执行一条update语句：

```
update user set name = "李四" where id = 1;   ---修改之前name=张三
```

此时undo log会记录一条相反的update语句，如下：

```
update user set name = "张三" where id = 1;
```

如果这个修改出现异常，可以使用undo log日志来实现回滚操作，以保证事务的一致性。

-   2、提供多版本控制(MVCC)【undo log实现多版本并发控制（MVCC）】

MVCC，即多版本控制。在MySQL数据库InnoDB存储引擎中，用undo Log来实现多版本并发控制(MVCC)。当读取的某一行被其他事务锁定时，它可以从undo log中分析出该行记录以前的数据版本是怎样的，从而让用户能够读取到当前事务操作之前的数据【快照读】。

下面解释一下什么是快照读，与之对应的还有一个是---当前读。

快照读：

SQL读取的数据是快照版本【可见版本】，也就是历史版本，不用加锁，普通的SELECT就是快照读。

当前读：

SQL读取的数据是最新版本。通过锁机制来保证读取的数据无法通过其他事务进行修改UPDATE、DELETE、INSERT、SELECT … LOCK IN SHARE MODE、SELECT … FOR UPDATE都是当前读。

### 三、undo log的存储机制

undo log的存储由InnoDB存储引擎实现，数据保存在InnoDB的数据文件中。在InnoDB存储引擎中，undo log是采用分段(segment)的方式进行存储的。rollback segment称为回滚段，每个回滚段中有1024个undo log segment。在MySQL5.5之前，只支持1个rollback segment，也就是只能记录1024个undo操作。在MySQL5.5之后，可以支持128个rollback segment，分别从resg slot0 - resg slot127，每一个resg slot，也就是每一个回滚段，内部由1024个undo segment 组成，即总共可以记录128 \* 1024个undo操作。

下面以一张图来说明undo log日志里面到底存了哪些信息？

![](https://img-blog.csdnimg.cn/20210613084841967.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dlaXhpYW9odWFp,size_16,color_FFFFFF,t_70)

如上图，可以看到，undo log日志里面不仅存放着数据更新前的记录，还记录着RowID、事务ID、回滚指针。其中事务ID每次递增，回滚指针第一次如果是insert语句的话，回滚指针为NULL，第二次update之后的undo log的回滚指针就会指向刚刚那一条undo log日志，依次类推，就会形成一条undo log的回滚链，方便找到该条记录的历史版本。

### 四、undo log的工作原理

在更新数据之前，MySQL会提前生成undo log日志，当事务提交的时候，并不会立即删除undo log，因为后面可能需要进行回滚操作，要执行回滚（rollback）操作时，从缓存中读取数据。undo log日志的删除是通过通过后台purge线程进行回收处理的。

同样，通过一张图来理解undo log的工作原理。

![](https://img-blog.csdnimg.cn/20210613084909540.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dlaXhpYW9odWFp,size_16,color_FFFFFF,t_70)

如上图：

-   1、事务A执行update操作，此时事务还没提交，会将数据进行备份到对应的undo buffer，然后由undo buffer持久化到磁盘中的undo log文件中，此时undo log保存了未提交之前的操作日志，接着将操作的数据，也就是Teacher表的数据持久保存到InnoDB的数据文件IBD。
-   2、此时事务B进行查询操作，直接从undo buffer缓存中进行读取，这时事务A还没提交事务，如果要回滚（rollback）事务，是不读磁盘的，先直接从undo buffer缓存读取。

用undo log实现原子性和持久化的事务的简化过程：

假设有A、B两个数据，值分别为1,2。

-   A. 事务开始
-   B. 记录A=1到undo log中
-   C. 修改A=3
-   D. 记录B=2到undo log中
-   E. 修改B=4
-   F. 将undo log写到磁盘 -------undo log持久化
-   G. 将数据写到磁盘 -------数据持久化
-   H. 事务提交 -------提交事务

之所以能同时保证原子性和持久化，是因为以下特点：

1.  更新数据前记录undo log。
2.  为了保证持久性，必须将数据在事务提交前写到磁盘，只要事务成功提交，数据必然已经持久化到磁盘。
3.  undo log必须先于数据持久化到磁盘。如果在G,H之间发生系统崩溃，undo log是完整的，可以用来回滚。
4.  如果在A - F之间发生系统崩溃，因为数据没有持久化到磁盘，所以磁盘上的数据还是保持在事务开始前的状态。

缺陷：每个事务提交前将数据和undo log写入磁盘，这样会导致大量的磁盘IO，因此性能较差。 如果能够将数据缓存一段时间，就能减少IO提高性能，但是这样就会失去事务的持久性。

> undo日志属于逻辑日志，redo是物理日志，所谓逻辑日志是undo log是记录一个操作过程，不会物理删除undo log，sql执行delete或者update操作都会记录一条undo日志。

### 五、undo log的相关参数

-   innodb\_undo\_directory

指定undo log日志的存储目录，默认值为./。

-    innodb\_undo\_logs 

在MySQL5.6版本之后，可以通过此参数自定义多少个rollback segment，默认值为128。

-    innodb\_undo\_tablespaces 

指定undo log平均分配到多少个表空间文件中，默认值为0，即全部写入一个文件中。不建议修改为非0值，我们直接使用默认值即可。

> 在InnoDB存储引擎中，在启动日志中也会提示：不建议修改 innodb\_undo\_tablespaces为非0的值。

### 附录：参考资料

[https://www.cnblogs.com/f-ck-need-u/p/9010872.html#auto\_id\_11](https://www.cnblogs.com/f-ck-need-u/p/9010872.html#auto_id_11)

[https://www.cnblogs.com/wyy123/p/7880077.html](https://www.cnblogs.com/wyy123/p/7880077.html)

《MySQL技术内幕（第5版）》