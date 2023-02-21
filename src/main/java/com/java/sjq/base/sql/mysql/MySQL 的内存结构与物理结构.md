[MySQL的内存结构与物理结构 - 腾讯云开发者社区-腾讯云](https://cloud.tencent.com/developer/article/1496453)





**“**从[MySQL](https://cloud.tencent.com/product/cdb?from=10680)的物理结构和内存结构开始了解MySQL的运行机制**”**

![](https://ask.qcloudimg.com/http-save/5426480/cch724f0dq.jpeg?imageView2/2/w/1620)

MySQL的数据存储结构主要分两个方面：物理存储结构与内存存储结构，作为[数据库](https://cloud.tencent.com/solution/database?from=10680)，所有的数据最后一定要落到磁盘上，才能完成持久化的存储。内存结构为了实现提升数据库整体性能，主要用于存储临时数据和日志的缓冲。本文主要讲MySQL的物理结构，以及MySQL的内存结构，对于存储引擎也主要以InnoDB为主。

![](https://ask.qcloudimg.com/http-save/5426480/1l2cevv4oj.jpeg?imageView2/2/w/1620)

01

—

MySQL的物理结构

上图的 On-Disk Structures 主要是InnoDB存储引擎的磁盘结构，对于[MySQL数据库](https://cloud.tencent.com/product/cdb?from=10680)来说，还包括一些文件、日志、表结构存储结构等。

文件主要包括参数文件、日志文件、表结构文件、存储引擎文件等，存储引擎文件主要包括表空间文件、redo log等。

**参数文件**指的是MySQL实例启动时，会先去读取的参数配置文件，配置内容包含各种文件的位置，一些初始化参数，这些参数定义了某种内存结构的大小设置，还包括一些其他配置，如：主从配置等。

**日志文件**记录了MySQL数据库的各种类型活动，这些日志都是在Server层实现的，是各种存储引擎都会有的日志文件。包括错误日志、binlog、慢查询日志、查询日志：

**错误日志**主要用于查看MySQL出现错误时，用来排查问题使用，是DBA出问题时，首要关注的日志。

**慢查询日志**是用来记录低于阈值的SQL语句，这个阈值通过long\_query\_time设置，默认是10秒，通过查询慢查询日志，也可以得到一些关于数据库需要优化的信息，比如需要某个语句执行扫描了全表，没有走到索引。开发人员可以结合场景去优化SQL语句或者优化索引的设置等。

**查询日志**记录了所有对MySQL数据库请求的信息，不论这些请求是否得到了正确的执行。

**binlog**是server层维护的一种二进制日志，与后面要说的InnoDB存储引擎层的redo log不同，主要用来记录对MySQL数据更新或潜在发生更新的SQL语句，不包括Select和Show这类操作。binlog默认是不开启的，测试表明开启确实会影响MySQL的性能。不过通过binlog可以实现数据的备份同步和数据恢复，同这么强大的作用比起来，损失这点性能也是值得的，所以建议开启。

当使用支持事务的存储引擎时，未提交事务的binlog会存储到binlog\_cache中，而提交的事务，要根据参数来确定从缓冲刷到磁盘的时间。

根据binlog\_cache\_size设置缓冲池大小：

```
show variables like 'binlog_cache_size';
```

![](https://ask.qcloudimg.com/http-save/5426480/9flozhfnwe.png?imageView2/2/w/1620)

根据sync\_binlog设置刷盘的时间：

```
show variables like 'sync_binlog';
```

-   如果设置为0，表示MySQL不控制binlog的刷盘，由操作系统的文件系统来控制它的属性；
-   如果设置不为0的值，表示每【设置值】次事务，刷新binlog缓冲池到磁盘；

![](https://ask.qcloudimg.com/http-save/5426480/eq1fad00bf.png?imageView2/2/w/1620)

如果设置为1，当发生断电、系统崩溃时，最多丢失一个事务的更新。如果设置大于1，在系统故障时，则可能会有一部分已提交的但还未来得及刷盘的数据丢失。因此，sync\_binlog 设置为1需要每次都刷盘，对性能有一定影响，同时也是最安全。现在高版本的MySQL默认设置 sync\_binlog 为1，不过可以设置成100以内的值，牺牲一定一致性来获取更多的性能。

binlog记录的事件支持三种格式：

-   STATEMENT：基于SQL语句的复制（statement-based replication, SBR）
-   ROW：基于行的复制（row-based replication, RBR）
-   MIXED：混合模式复制（mixed-based replication, MBR）

InnoDB存储引擎将所有数据逻辑地存放在一个空间，我们称之为**表空间**（tablespace）。表空间由段（segment）、区（extent）、页（page）组成，大致存储结构如下图所示：

![](https://ask.qcloudimg.com/http-save/5426480/qhdhcat2w.jpeg?imageView2/2/w/1620)

-   段：常见的段有数据段（B+树页节点）、索引段（B+树非页节点，索引节点）、回滚段等。
-   区：区是由64个连续的页组成的，每个页大小为16KB，即每个区大约为1MB。
-   页：页是innodb磁盘管理最小的单位，innodb每个页的大小是16K，且不可更改。常见的页有数据页、Undo页、事务数据页、系统页等

![](https://ask.qcloudimg.com/http-save/5426480/83z1jtk5oa.png?imageView2/2/w/1620)

InnoDB页类型为B-tree node类型，存放的实际就是行数据了，File Header用于记录Page的头信息，其中比较重要的就是Fil\_PAGE\_PREV和FIL\_PAGE\_NEXT字段，通过这两个字段可以找到该页的上一页和下一页，实际上通过这两个字段将形成一个双向链表。Page Header用来记录Page状态信息。接下来Infimum和Supremum是两个伪行记录，Infimum（下确界）记录比该页中任何主键值都要小的值，Supremum （上确界）记录比该页中任何主键值都要大的值，这个伪记录分别构成了页中记录的边界。UserRecords中存放的就是数据行记录。Free Space存放的是空闲空间，被删除的行会被记录成空闲空间。Page Directory记录着与二叉查找相关的信息。File Trailer存储用于检测数据完整性的校验。

![](https://ask.qcloudimg.com/http-save/5426480/4xbrxhczwi.jpeg?imageView2/2/w/1620)

InnoDB存储引擎是按照行来存储数据的，目前版本的MySQL默认行记录格式为Compact，还包括Redundant。下图为Compact行记录格式，每行的数据除了下图的定义的列外，还包括两个隐藏的列，事务ID列和回滚指针列，如果没有定义主键，还会有一个默认的Rowid列。

![](https://ask.qcloudimg.com/http-save/5426480/kq12wiwtyc.png?imageView2/2/w/1620)

表空间可以看成是InnoDB存储引擎逻辑结构的最高层，如果不开启 innodb\_file\_per\_table 参数设置的话，存储引擎会有一个共享表空间，ibdata1，所有数据都会存在这个表空间内，如果开启了 innodb\_file\_per\_table 参数配置，则会为每张表单独放到一个表空间内。这里需要注意的是，每个独立的表空间内存放的只是数据、索引、插入缓冲等，其他类的数据回滚信息、插入缓冲索引、double write buffer还是存放在共享表空间内。

查看 innodb\_file\_per\_table 参数配置，是否启用每个表为单独放在一个表空间内：

```
show variables like '%per_table%';
```

![](https://ask.qcloudimg.com/http-save/5426480/q6a2d0vmgt.png?imageView2/2/w/1620)

通常MySQL默认为不启用，也就是使用共享表空间ibdata1文件，共享表空间的特点是可以指定多个文件来作为共享表空间文件。可以设置为autoextend，当表空间不足时，自动扩张。

```
show variables like 'innodb_data%'
```

共享表空间还有一个问题是，无法通过删除数据文件方式，来减少表空间大小，只能通过 mysqldump 工具备份所有数据，再删除所有表空间数据，再重新导入备份数据，这样的方式重构表空间来减少。独立表空间则没有这个问题，可以实现单表在不同数据库中移动，性能也会更高一些。File-Per-Table tablespace 每个表独立的表空间，一般会在MySQL根目录上，以库名为目录的文件夹内，包括表结构定义文件（.frm）和表数据文件（.ibd）。

**重做日志文件**也叫redo log，不同于binlog是MySQL server层实现的，redo log是InnoDB存储引擎实现的。redo log以日志文件组的形式存在，InnoDB以循环的方式先写ib-logfile1，写完再写ib-logfile2，一组文件都写完了，再回到ib-logfile1，也就会覆盖之前写的数据。

![](https://ask.qcloudimg.com/http-save/5426480/ihiv24y1zn.png?imageView2/2/w/1620)

redo log是记录的都是关于每个页（Page）的更改的物理情况，InnoDB要读取或修改数据是从磁盘读取到内存中进行的，然后再通过一套完整的策略来刷回磁盘，这其中并不是每次都要刷回磁盘的，因为会产生大量的随机I/O，InnoDB会优化随机I/O为顺序I/O。不过存在一种情况，就是当刷盘之前，数据库出现问题，那数据页还没来得及刷盘，数据会丢失，redo log就是解决这个问题的。在数据库重启恢复时，用来恢复还未刷到磁盘的数据页。

redo log包含两个部分，一个是日志缓冲，一个是磁盘上的redo log文件。redo log都是先写到日志缓冲内（redo log buffer），然后在写入日志文件，根据 innodb\_flush\_log\_at\_trx\_commit 参数可以设定缓冲写到磁盘的机制。

-   设置为0时，每次事务提交都把redo log留在redo log buffer中，等待master thread每秒定时刷到redo log中；
-   设置为1时，每次事务提交都将redo log持久化到磁盘；
-   设置为2时，每次事务提交只是把redo log写到磁盘的page上，等待数据刷盘；

![](https://ask.qcloudimg.com/http-save/5426480/5def7qsy4w.png?imageView2/2/w/1620)

redo log文件组是环形的结构，设置其大小要综合参考脏页刷新与每次重启恢复数据的时长，设置过小，事务高峰期，可能会使部分没落盘的数据页的redo log被覆盖；设置过大，可能会导致重启之后，数据恢复时间过长，也浪费磁盘空间。对于redo log的介绍，后续文章将会继续深入讨论其机制和原理。

单独依靠redo log并不一定能恢复到故障之前的状况，当一个事务还未提交时，可能redo log已经写完了，但其实这部分数据应该需要回滚。所以，这就需要一种回滚机制，也就是**undo log**。undo log和redo log记录物理日志不一样，它是逻辑日志。可以认为当delete一条记录时，undo log中会记录一条对应的insert记录，反之亦然，当update一条记录时，它记录一条对应相反的update记录。

在InnoDB存储引擎中，undo log用于实现回滚和多版本控制-MVCC，其原理大致为，当执行rollback时，就可以从undo log中的逻辑记录读取到相应的内容并进行回滚。当用到行版本控制的时候，当读取的某一行被其他事务锁定时，它可以从undo log中分析出该行记录以前的数据是什么，从而提供该行版本信息，让用户实现非锁定一致性读取。

02

—

MySQL的内存结构

InnoDB存储引擎使用**Buffer Pool**在内存中缓存表数据和索引，处理数据时可以直接操作缓冲池的数据，提升InnoDB的处理速度。缓冲池的数据一般按照页格式，每个页包含多行数据，缓冲池可以看成是页面链表，并且使用LRU（last recent used）算法，来管理缓冲池的数据列表。当需要新空间将新页面加到缓冲池时，将会淘汰最近最少使用的数据。

![](https://ask.qcloudimg.com/http-save/5426480/k361xlo1wa.jpeg?imageView2/2/w/1620)

MySQL提供了多个关于缓冲池的配置参数，

-   innodb\_buffer\_pool\_instances 与 innodb\_buffer\_pool\_size 配置缓冲池的实例和缓冲池大小：通过配置多个缓冲池可以减少不同线程的竞争，提升并发度。通常在专用服务器上，80%的物理内存会分配给Buffer Pool。
-   innodb\_buffer\_pool\_chunk\_size 配置缓冲池的块大小：当增加或减少innodb\_buffer\_pool\_size时，操作以块形式执行，块大小由此参数决定，默认为128M。
-   innodb\_max\_dirty\_pages\_pct 配置脏页比例：根据设置的缓冲池中脏页比例，来触发将脏页刷盘的时机。另外，InnoDB也根据redo log的生成速度和刷新频率，来触发刷盘时机。
-   innodb\_read\_ahead\_threshold 与 innodb\_random\_read\_ahead 预读参数配置：预读是指一次I/O请求磁盘中某页中的数据时，会同时同步取出相邻页面的数据，缓存到缓冲池。因为，InnoDB认为这些页面的数据大概率也将会被读取，从而来提升I/O性能。包括线性预读和随机预读。

**Change Buffer**用来缓存不在缓冲池中的辅助索引页(非唯一索引)的变更。这些缓存的的变更，可能由INSERT、UPDATE或DELETE操作产生，当读操作将这些变更的页从磁盘载入缓冲池时，InnoDB引擎会将change buffer中缓存的变更跟载入的辅助索引页合并。

不像聚簇索引，辅助索引通常不是唯一的，并且辅助索引的插入顺序是相对随机的。若不用change buffer，那么每有一个页产生变更，都要进行I/O操作来合并变更。使用change buffer可以先将辅助索引页的变更缓存起来，当这些变更的页被其他操作载入缓冲池时再执行merge操作，这样可以减少大量的随机I/O。change buffer可能缓存了一个页内的多条记录的变更，这样可以将多次I/O操作减少至一次。

在内存中，change buffer占据缓冲池的一部分。在磁盘上，change buffer是系统表空间的一部分，以便数据库重启后缓存的索引变更可以继续被缓存

**自适应哈希索引**是InnoDB表通过在内存中构造一个哈希索引来加速查询的优化技术，此优化只针对使用 '=' 和 'IN' 运算符的查询。MySQL会监视InnoDB表的索引查找，若能通过构造哈希索引来提高效率，那么InnoDB会自动为经常访问的辅助索引页建立哈希索引。

这个哈希索引总是基于辅助索引(B+树结构)来构造。MySQL通过索引键的任意长度的前缀和索引的访问模式来构造哈希索引。InnoDB只为某些热点页构建哈希索引。

**Log Buffer**用来缓存要写入磁盘日志文件的内存缓冲区域，该区域大小由 innodb\_log\_buffer\_size 参数定义，默认16MB。

**Doublewrite Buffer**是位于系统表空间中的存储区域，其工作原理是：在将缓冲池中的页写入磁盘上对应位置之前，先将缓冲池中的页copy到内存中的doublewrite buffer，之后再分两次，每次1M，顺序地将内存中doublewrite buffer中的页写入系统表空间中的doublewrite区域，然后立即调用系统fsync函数，同步数据到磁盘文件中，避免缓冲写带来的问题。在完成doublewrite页的写入之后，再将内存上doublewrite buffer中的页写入到自己的表空间文件。如果当页面写入磁盘时，发生了数据库宕机，会导致“写失效”，重启之后，可以通过Doublewrite Buffer来恢复故障前要写的Page数据。

![](https://ask.qcloudimg.com/http-save/5426480/mk9ty20ky0.jpeg?imageView2/2/w/1620)

InnoDB存储引擎支持事务、MVCC、故障恢复等特性，要理解InnoDB的存储结构，我总结我的理解关键点如下：

磁盘I/O，磁盘的读写是操作系统实现的，结合磁盘存取数据的过程，来看InnoDB的逻辑存储结构，在磁盘和内存中都以页（Page）为操作的基本单元，页内元素才是基本的行结构数据；

基于B+ Tree结构来组织数据的存储，叶节点和非叶节点存储数据和索引，并且通过主键构建整个树结构，便于通过主键索引遍历数据；

二阶段提交、undo log等机制完善InnoDB事务的特性；

缓冲池、索引等机制的引入提升MySQL性能；

由redo log、binlog，错误日志来实现数据库故障恢复、备份和异常情况的记录。

___

1.  https://dev.mysql.com/doc/refman/5.5/en/innodb-on-disk-structures.html
2.  https://dev.mysql.com/doc/refman/5.7/en/innodb-in-memory-structures.html
3.  https://dev.mysql.com/doc/refman/5.7/en/innodb-doublewrite-buffer.html
4.  《MySQL技术内幕：InnoDB存储引擎》
5.  https://www.cnblogs.com/liuhao/p/3714012.html
6.  https://my.oschina.net/u/553773/blog/792144
7.  https://github.com/mysql/mysql-server/tree/5.7

文章分享自微信公众号：

![](https://open.weixin.qq.com/qr/code?username=gh_8f1f321811fd)

本文参与 [腾讯云自媒体分享计划](https://cloud.tencent.com/developer/support-plan) ，欢迎热爱写作的你一起参与！

如有侵权，请联系

cloudcommunity@tencent.com

删除。