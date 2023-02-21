[【图文详解】MySQL系列之redo log、undo log和binlog详解 - 腾讯云开发者社区-腾讯云](https://cloud.tencent.com/developer/article/1801920)

这篇文章主要介绍了[MySQL](https://cloud.tencent.com/product/cdb?from=10680)系列之redo log、undo log和binlog详解,本文给大家介绍的非常详细，对大家的学习或工作具有一定的参考借鉴价值，需要的朋友可以参考下。

## 事务的实现

redo log保证事务的持久性，undo log用来帮助事务回滚及MVCC的功能。

## InnoDB存储引擎体系结构

![](https://ask.qcloudimg.com/http-save/8352137/8xyl6uoqp1.png?imageView2/2/w/1620)

![](https://ask.qcloudimg.com/http-save/8352137/uptm9qsvap.png?imageView2/2/w/1620)

## redo log：Write Ahead Log策略

事务提交时，先写重做日志再修改页；当由于发生宕机而导致数据丢失时，就可以通过重做日志来完成数据的恢复。

InnoDB首先将重做日志信息先放到重做日志缓存；

然后，按一定频率刷新到重做日志文件。

重做日志文件： 在默认情况，InnoDB存储引擎的数据目录下会有两个名为ib\_logfile1和ib\_logfile2的文件。每个InnoDB存储引擎至少有1个重做日志文件组(group)，每个文件组下至少有2个重做日志文件。

下面图一，很好说明重做日志组以循环写入方式运行，InnoDB存储引擎先写ib\_logfile1，当达到文件最后时，会切换至重做日志文件ib\_logfile2.

而图2，增加一个OS Buffer，有助于理解fsync过程。

![](https://ask.qcloudimg.com/http-save/8352137/yafz6czi5d.png?imageView2/2/w/1620)

![](https://ask.qcloudimg.com/http-save/8352137/1bgdsp0g3e.png?imageView2/2/w/1620)

关于log group，称为重做日志组，是一个逻辑上的概念。InnoDB存储引擎实际只有一个log group。

![](https://ask.qcloudimg.com/http-save/8352137/qpjxwbnle3.png?imageView2/2/w/1620)

log group中第一个redo log file，其前2KB部分保存4个512字节大小块：

![](https://ask.qcloudimg.com/http-save/8352137/9iiivqtmcn.png?imageView2/2/w/1620)

## 重做日志缓冲刷新到磁盘

下面三种情况刷新：

> Master Thread每一秒将重做日志缓冲刷新到重做日志文件 每个事务提交时会将重做日志缓冲刷新到重做日志文件 当重做日志缓冲池剩余空间小于1/2时，重做日志刷新到重做日志文件

补充上述三种情况第二种，触发写磁盘过程由参数innodb\_flush\_log\_at\_trx\_commit控制，表示提交(commit)操作时，处理重做日志的方式。

参数 innodb\_flush\_log\_at\_trx\_commit 有效值有0、1、2

0表示当提交事务时，并不将事务的重做日志写入磁盘上日志文件，而是等待主线程每秒刷新。

1表示在执行commit时将重做日志缓冲同步写到磁盘，即伴有fsync的调用

2表示将重做日志异步写到磁盘，即写到文件系统的缓存中。不保证commit时肯定会写入重做日志文件。

0，当[数据库](https://cloud.tencent.com/solution/database?from=10680)发生宕机时，部分日志未刷新到磁盘，因此会丢失最后一段时间的事务。

2，当操作系统宕机时，重启数据库后会丢失未从文件系统缓存刷新到重做日志文件那部分事务。

下图有助于理解

![](https://ask.qcloudimg.com/http-save/8352137/gq7c70q44y.png?imageView2/2/w/1620)

## 重做日志块

在InnoDB存储引擎中，重做日志都是以512字节进行存储的。意味着重做日志缓存、重做日志文件都是以块(block)的方式进行保存的，每块512字节。

重做日志头12字节，重做日志尾8字节，故每个重做日志块实际可以存储492字节。

![](https://ask.qcloudimg.com/http-save/8352137/eknyklpze1.png?imageView2/2/w/1620)

## 重做日志格式

redo log是基于页的格式来记录的。默认情况下，innodb的页大小是16KB(由 innodb\_page\_size变量控制)，一个页内可以存放非常多的log block(每个512字节)，而log block中记录的又是数据页的变化。

log body的格式分为4部分：

redo\_log\_type：占用1个字节，表示redo log的日志类型。

space：表示表空间的ID，采用压缩的方式后，占用的空间可能小于4字节。

page\_no：表示页的偏移量，同样是压缩过的。

redo\_log\_body表示每个重做日志的数据部分，恢复时会调用相应的函数进行解析。例如insert语句和delete语句写入redo log的内容是不一样的。

![](https://ask.qcloudimg.com/http-save/8352137/ehhgj2l6ke.png?imageView2/2/w/1620)

如下图，分别是insert和delete大致的记录方式。

![](https://ask.qcloudimg.com/http-save/8352137/gmromu53d3.png?imageView2/2/w/1620)

## redo日志恢复

下面 LSN (Log Sequence Number) 代表 checkpoint，当数据库在 LSN 为10000时发生宕机，恢复操作仅恢复LSN10000-LSN13000范围内日志

![](https://ask.qcloudimg.com/http-save/8352137/s65sst15dc.png?imageView2/2/w/1620)

## undo log

undo log的作用：

undo是逻辑日志，只是将数据库逻辑地恢复到原来的样子；所有修改都被逻辑地取消了，但是数据结构和页本身在回滚之后可能不大相同。

undo log有两个作用：提供回滚和多个行版本控制(MVCC)。

InnoDB存储引擎回滚时，对于每个INSERT，会完成一个DELETE；对于每个DELETE，会执行一个INSERT；对于每个UPDATE，会执行一个相反的UPDATE，将修改前的行放回去。

MVCC： 当用户读取一行记录时，若该记录已经被其他事务占用，当前事务可以通过undo读取之前的行版本信息，以此实现非锁定读取。

undo log的存储方式：

innodb存储引擎对undo的管理采用段的方式。rollback segment称为回滚段，每个回滚段中有1024个undo log segment。

在以前老版本，只支持1个rollback segment，这样就只能记录1024个undo log segment。后来MySQL5.5可以支持128个rollback segment，即支持128\*1024个undo操作，还可以通过变量 innodb\_undo\_logs (5.6版本以前该变量是 innodb\_rollback\_segments )自定义多少个rollback segment，默认值为128。

undo log默认存放在共享表空间中。

![](https://ask.qcloudimg.com/http-save/8352137/v5oa1etyaw.png?imageView2/2/w/1620)

## 事务提交undo log处理过程

当事务提交时，InnoDB存储引擎会做以下两件事：

1、将undo log放入一个列表中，以供之后的purge使用，是否可以最终删除undo log及所在页由purge线程来判断

2、判断undo log 所在的页是否可以重用，若可以，分配给下个事务使用

当事务提交时，首先将undo log放入链表中，然后判断undo页的使用空间是否小于3/4，若是，则表示该undo页可以被重用，之后新的undo log记录在当前undo log的后面

undo log分为：

> insert undo log update undo log

因为事务隔离性，insert undo log对其他事务不可见，所以该undo log可以在事务提交后直接删除，不需要进行purge操作。

update undo log记录的是对delete和update操作产生的undo log。该undo log可能需要提供MVCC机制，因此不能提交时就进行删除

update分为两种情况：

date的列如果不是主键列，在undo log中直接反向记录是如何update的。即update是直接进行的。

update主键的操作可以分为两步：

首先将原主键记录标记为已删除，因此需要产生一个类型为TRX\_UNDO\_DEL\_MARK\_REC的undo log

之后插入一条新的记录，产生一个类型为TRX\_UNDO\_INSERT\_MARK\_REC的undo log

![](https://ask.qcloudimg.com/http-save/8352137/y2nwaot6gf.png?imageView2/2/w/1620)

InnoDB purge时，会先从history列表找undo log，然后再从undo page中找undo log；可以避免大量随机读取操作，从而提高purge效率。

![](https://ask.qcloudimg.com/http-save/8352137/63g2e7adfj.png?imageView2/2/w/1620)

## MVCC(多版本并发控制)

MVCC其实就是在每一行记录后面增加两个隐藏列，记录创建版本号和删除版本号，而每一个事务在启动的时候，都有一个唯一的递增的版本号。

MVCC只在REPEATABLE READ 和READ COMMITTED两个隔离级别下工作。读未提交不存在版本问题，序列化则对所有读取行加锁。

示例：

插入操作：记录的创建版本号就是事务版本号

如插入一条记录，事务id假设是1，则创建版本号也是1

idnamecreate versiondelete version

1test1

更新操作：先标记旧版本号为已删除，版本号就是当前版本号，再插入一条新的记录

如事务2把name字段更新

update table set name = 'new test' where id = 1;

原来的记录被标记删除，删除版本号为2，并插入新记录，创建版本号为2

idnamecreate versiondelete version

1test12

1new test2

删除操作：把事务版本作为删除版本号

如事务3把记录删除

delete from table where id = 1;

idnamecreate versiondelete version

1test23

查询操作

需满足以下两个条件的记录才能被事务查询出来：

InnoDB只查找版本早于当前事务版本的数据行

行的删除版本要么未定义，要么大于当前版本号，这可以确保事务读取到的行，在事务未开始之前未被删除

MVCC好处：减少锁的争用，提升性能

## binlog

二进制文件概念及作用

二进制文件(binary log)记录了对[MySQL数据库](https://cloud.tencent.com/product/cdb?from=10680)执行更改的所有操作(不包含SELECT、SHOW等，因为对数据没有修改)

二进制文件主要几种作用：

> 恢复：某些数据的恢复需要二进制日志 复制： 通过复制和执行二进制日志使一台远程的MySQL(slave)与另一台MySQL数据库(master)进行实时同步 审计： 用户可以通过二进制日志中信息来进行审计，判断是否有对数据库进行注入的攻击

二进制文件三个格式

MySQL 5.1开始引入binlog\_format参数，该参数可设值有STATEMENT、ROW和MIX

STATEMENT： 二进制文件记录的是日志的逻辑SQL语句

ROW：记录表的行更改情况。如果设置了ROW模式，可以将InnoDB事务隔离级别设为READ\_COMMITTED，以获得更好的并发性

MIX：MySQL默认采用STATEMENT格式进行二进制文件的记录，但在一些情况下会使用ROW，可能的情况有：

表的存储引擎为NDB，这时对表DML操作都以ROW格式进行

使用了UUID()、USER()、CURRENT\_USER()、FOUND\_ROWS()、ROW\_COUNT()等不确定函数

使用了INSERT DELAY语句

使用了用户定义函数

使用了临时表

redo log和二进制文件区别

(二进制文件用来进行POINT-IN-TIME(PIT))的恢复及主从复制环境的建立。

二进制文件会记录所有与MySQL数据库有关的日志记录，包括InnoDB、MyISAM等其他存储引擎的日志。而InnoDB存储引擎的重做日志只记录有关该存储引擎本身的事务日志。

记录的内容不同，无论用户将二进制日志文件记录的格式设为STATEMENT、ROW或MIXED，其记录的都是关于一个事务的具体操作内容，即该日志是逻辑日志。而InnoDB存储引擎的重做日志文件记录的是关于每个页的更改的物理情况。

此外，写入的时间页不同，二进制日志文件仅再事务提交前进行提交，即只写磁盘一次，不论这时该事务多大。而在事务进行的过程中，却不断有重做日志条目(reod entry)被写入到重做日志文件中。

## group commit

若事务为非只读事务，则每次事务提交时需要进行一次fsync操作，以此保证重做日志都已经写入磁盘。但磁盘fsync性能有限，为提高磁盘fsync效率，当前数据库都提供group commit功能，即一次可以刷新确保多个事务日志被写入文件。

对InnoDB group commit，进行两阶段操作：

1、修改内存中事务对应的信息，并且将日志写入重做日志缓冲

2、调用fsync将确保日志都从重做日志缓冲写入磁盘

InnoDB1.2前，开启二进制文件，group commit功能失效问题：

开启二进制文件后，其步骤如下：

1）当事务提交时，InnoDB存储引擎进行prepare操作

2）MySQL数据库上层写入二进制文件

3）InnoDB将日志写入重做日志文件

a）修改内存中事务对应的信息，并将日志写入重做日志缓冲b）调用fsync将确保日志都从重做日志缓冲写入磁盘

其中在保证MySQL数据库上层二进制文件的写入顺序，和InnoDB事务提交顺序一致，MySQL内部使用了prepare\_commit\_mutex锁，从而步骤3）中a）步不可以在其他事务执行步骤b）时进行，从而导致roup commit功能失效。

解决方案便是 BLGC (Binary Log Group Commit)

MySQL 5.6 BLGC实现方式分为三个阶段：

Flush阶段：将每个事务的二进制文件写入内存

Sync阶段：将内存中的二进制刷新到磁盘，若队列有多个事务，那么仅一次fsync操作就完成了二进制日志的写入，这就是BLGC

Commit阶段：leader根据顺序调用存储引擎层事务提交，由于innodb本就支持group commit，所以解决了因为锁 prepare\_commit\_mutex 而导致的group commit失效问题。

https://www.jb51.net/article/202911.htm

___

## [必须了解的mysql三大日志-binlog、redo log和undo log](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.cnblogs.com%2Fchentianming%2Fp%2F13517020.html)

日志是mysql数据库的重要组成部分，记录着数据库运行期间各种状态信息。mysql日志主要包括错误日志、查询日志、慢查询日志、事务日志、二进制日志几大类。作为开发，我们重点需要关注的是二进制日志(binlog)和事务日志(包括redo log和undo log)，本文接下来会详细介绍这三种日志。

## binlog

binlog用于记录数据库执行的写入性操作(不包括查询)信息，以二进制的形式保存在磁盘中。binlog是mysql的逻辑日志，并且由Server层进行记录，使用任何存储引擎的mysql数据库都会记录binlog日志。

> 逻辑日志：**可以简单理解为记录的就是sql语句**。 物理日志：**因为mysql数据最终是保存在数据页中的，物理日志记录的就是数据页变更**。

binlog是通过追加的方式进行写入的，可以通过max\_binlog\_size参数设置每个binlog文件的大小，当文件大小达到给定值之后，会生成新的文件来保存日志。

### binlog使用场景

在实际应用中，binlog的主要使用场景有两个，分别是**主从复制**和**数据恢复**。

**主从复制**：在Master端开启binlog，然后将binlog发送到各个Slave端，Slave端重放binlog从而达到主从数据一致。

**数据恢复**：通过使用mysqlbinlog工具来恢复数据。

### binlog刷盘时机

对于InnoDB存储引擎而言，只有在事务提交时才会记录biglog，此时记录还在内存中，那么biglog是什么时候刷到磁盘中的呢？mysql通过sync\_binlog参数控制biglog的刷盘时机，取值范围是0-N：

0：不去强制要求，由系统自行判断何时写入磁盘；

1：每次commit的时候都要将binlog写入磁盘；

N：每N个事务，才会将binlog写入磁盘。

从上面可以看出，sync\_binlog最安全的是设置是1，这也是MySQL 5.7.7之后版本的默认值。但是设置一个大一些的值可以提升数据库性能，因此实际情况下也可以将值适当调大，牺牲一定的一致性来获取更好的性能。

### binlog日志格式

binlog日志有三种格式，分别为STATMENT、ROW和MIXED。

在 MySQL 5.7.7之前，默认的格式是STATEMENT，MySQL 5.7.7之后，默认值是ROW。日志格式通过binlog-format指定。

#### STATMENT

**基于SQL语句的复制(statement-based replication, SBR)，每一条会修改数据的sql语句会记录到binlog中**。

优点：**不需要记录每一行的变化，减少了binlog日志量，节约了IO, 从而提高了性能**；

缺点：**在某些情况下会导致主从数据不一致，比如执行sysdate()、slepp()等**。

#### ROW

**基于行的复制(row-based replication, RBR)，不记录每条sql语句的上下文信息，仅需记录哪条数据被修改了**。

优点：**不会出现某些特定情况下的存储过程、或function、或trigger的调用和触发无法被正确复制的问题**；

缺点：**会产生大量的日志，尤其是alter table的时候会让日志暴涨**

#### MIXED

**基于STATMENT和ROW两种模式的混合复制(mixed-based replication, MBR)，一般的复制使用STATEMENT模式保存binlog，对于STATEMENT模式无法复制的操作使用ROW模式保存binlog**

## redo log

## 为什么需要redo log

我们都知道，事务的四大特性里面有一个是**持久性**，具体来说就是**只要事务提交成功，那么对数据库做的修改就被永久保存下来了，不可能因为任何原因再回到原来的状态**。那么mysql是如何保证一致性的呢？最简单的做法是在每次事务提交的时候，将该事务涉及修改的数据页全部刷新到磁盘中。但是这么做会有严重的性能问题，主要体现在两个方面：

因为Innodb是以页为单位进行磁盘交互的，而一个事务很可能只修改一个数据页里面的几个字节，这个时候将完整的数据页刷到磁盘的话，太浪费资源了！

一个事务可能涉及修改多个数据页，并且这些数据页在物理上并不连续，使用随机IO写入性能太差！

因此mysql设计了redo log，**具体来说就是只记录事务对数据页做了哪些修改**，这样就能完美地解决性能问题了(相对而言文件更小并且是顺序IO)。

## redo log基本概念

redo log包括两部分：

一个是内存中的日志缓冲(redo log buffer)；

另一个是磁盘上的日志文件(redo log file)。

mysql每执行一条DML语句，先将记录写入redo log buffer，后续某个时间点再一次性将多个操作记录写到redo log file。

> 这种**先写日志，再写磁盘**的技术就是MySQL里经常说到的WAL(Write-Ahead Logging) 技术。

在计算机操作系统中，用户空间(user space)下的缓冲区数据一般情况下是无法直接写入磁盘的，中间必须经过操作系统内核空间(kernel space)缓冲区(OS Buffer)。因此，redo log buffer写入redo log file实际上是先写入OS Buffer，然后再通过系统调用fsync()将其刷到redo log file中，过程如下：

mysql支持三种将redo log buffer写入redo log file的时机，可以通过innodb\_flush\_log\_at\_trx\_commit参数配置，各参数值含义如下：

0（延迟写）事务提交时不会将redo log buffer中日志写入到os buffer，而是每秒写入os buffer并调用fsync()写入到redo log file中。也就是说设置为0时是(大约)每秒刷新写入到磁盘中的，当系统崩溃，会丢失1秒钟的数据。

1（实时写，实时刷）事务每次提交都会将redo log buffer中的日志写入os buffer并调用fsync()刷到redo log file中。这种方式即使系统崩溃也不会丢失任何数据，但是因为每次提交都写入磁盘，IO的性能较差。

2（实时写，延迟刷）每次提交都仅写入到os buffer，然后是每秒调用fsync()将os buffer中的日志写入到redo log file。

### redo log记录形式

前面说过，redo log实际上记录数据页的变更，而这种变更记录是没必要全部保存，因此redo log实现上采用了大小固定，循环写入的方式，当写到结尾时，会回到开头循环写日志。如下图：

在上图中，write pos表示redo log当前记录的LSN(逻辑序列号)位置，check point表示**数据页更改记录**刷盘后对应redo log所处的LSN(逻辑序列号)位置。write pos到check point之间的部分是redo log空着的部分，用于记录新的记录；check point到write pos之间是redo log待落盘的数据页更改记录。当write pos追上check point时，会先推动check point向前移动，空出位置再记录新的日志。

同时我们很容易得知，**在innodb中，既有redo log需要刷盘，还有数据页也需要刷盘，redo log存在的意义主要就是降低对数据页刷盘的要求**。

启动innodb的时候，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。因为redo log记录的是数据页的物理变化，因此恢复的时候速度比逻辑日志(如binlog)要快很多。

重启innodb时，首先会检查磁盘中数据页的LSN，如果数据页的LSN小于日志中的LSN，则会从checkpoint开始恢复。

还有一种情况，在宕机前正处于checkpoint的刷盘过程，且数据页的刷盘进度超过了日志页的刷盘进度，此时会出现数据页中记录的LSN大于日志中的LSN，这时超出日志进度的部分将不会重做，因为这本身就表示已经做过的事情，无需再重做。

## redo log与binlog区别

文件大小redo log的大小是固定的。

binlog可通过配置参数max\_binlog\_size设置每个binlog文件的大小。

实现方式redo log是InnoDB引擎层实现的，并不是所有引擎都有。

binlog是Server层实现的，所有引擎都可以使用 binlog日志。

记录方式redo log 采用循环写的方式记录，当写到结尾时，会回到开头循环写日志。binlog 通过追加的方式记录，当文件大小大于给定值后，后续的日志会记录到新的文件上

适用场景redo log适用于崩溃恢复(crash-safe)binlog适用于主从复制和数据恢复

由binlog和redo log的区别可知：binlog日志只用于归档，只依靠binlog是没有crash-safe能力的。但只有redo log也不行，因为redo log是InnoDB特有的，且日志上的记录落盘后会被覆盖掉。因此需要binlog和redo log二者同时记录，才能保证当数据库发生宕机重启时，数据不会丢失。

## undo log

数据库事务四大特性中有一个是**原子性**，具体来说就是 **原子性是指对数据库的一系列操作，要么全部成功，要么全部失败，不可能出现部分成功的情况**。实际上，**原子性**底层就是通过undo log实现的。undo log主要记录了数据的逻辑变化，比如一条INSERT语句，对应一条DELETE的undo log，对于每个UPDATE语句，对应一条相反的UPDATE的undo log，这样在发生错误时，就能回滚到事务之前的数据状态。同时，undo log也是MVCC(多版本并发控制)实现的关键，这部分内容在[面试中的老大难-mysql事务和锁，一次性讲清楚！](https://links.jianshu.com/go?to=https%3A%2F%2Fjuejin.im%2Fpost%2F6855129007336521741)中有介绍，不再赘述。

参考：

[https://juejin.im/post/6844903794073960455](https://links.jianshu.com/go?to=https%3A%2F%2Fjuejin.im%2Fpost%2F6844903794073960455)

[https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.cnblogs.com%2Ff-ck-need-u%2Farchive%2F2018%2F05%2F08%2F9010872.html)

[https://www.cnblogs.com/ivy-zheng/p/11094528.html](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.cnblogs.com%2Fivy-zheng%2Fp%2F11094528.html)

[https://yq.aliyun.com/articles/592937](https://links.jianshu.com/go?to=https%3A%2F%2Fyq.aliyun.com%2Farticles%2F592937)

[https://www.jianshu.com/p/5af73b203f2a](https://www.jianshu.com/p/5af73b203f2a)

[https://www.jianshu.com/p/20e10ed721d0](https://www.jianshu.com/p/20e10ed721d0)

本文分享自作者个人站点/博客：https://www.jianshu.com/u/c55c7a9c8de6复制

如有侵权，请联系

cloudcommunity@tencent.com

删除。