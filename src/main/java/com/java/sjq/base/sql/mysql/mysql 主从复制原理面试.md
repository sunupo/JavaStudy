[(110条消息) mysql主从复制原理面试\_面试题之----主从复制作用及原理\_weixin\_42355421的博客-CSDN博客](https://blog.csdn.net/weixin_42355421/article/details/113293648?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-113293648-blog-100428618.pc_relevant_multi_platform_whitelistv3&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-113293648-blog-100428618.pc_relevant_multi_platform_whitelistv3&utm_relevant_index=2)

1、什么是主从复制

1、主从的作用

2、主从的原理

3、从数据库的读的延迟问题了解吗？如何解决？

4、做主从后主服务器挂了怎么办？

# 一、什么是主从复制?

主从复制，是用来建立一个和主数据库完全一样的数据库环境，称为从数据库；主数据库一般是准实时的业务数据库。

# 二、主从复制的作用(好处，或者说为什么要做主从)重点!

1、[架构](https://so.csdn.net/so/search?q=%E6%9E%B6%E6%9E%84&spm=1001.2101.3001.7020)的扩展。业务量越来越大，I/O访问频率过高，单机无法满足，此时做多库的存储，物理服务器增加，负荷增加。

2、读写分离，使数据库能支撑更大的并发。主从只负责各自的写和读，极大程度的缓解X锁和S锁争用。在报表中尤其重要。由于部分报表sql语句非常的慢，导致锁表，影响前台服务。如果前台使用master，报表使用slave，那么报表sql将不会造成前台锁，保证了前台速度。

3、做数据的热备，作为后备数据库，主数据库服务器故障后，可切换到从数据库继续工作，避免数据丢失。

# 三、主从复制的原理(重中之重)：

1.数据库有个bin-log二进制文件，记录了所有sql语句。

2.我们的目标就是把主数据库的bin-log文件的sql语句复制过来。

3.让其在从数据的relay-log重做日志文件中再执行一次这些sql语句即可。

4.下面的主从配置就是围绕这个原理配置

5.具体需要三个线程来操作：

1. **binlog输出线程**:每当有从库连接到主库的时候，主库都会创建一个线程然后发送binlog内容到从库。

在从库里，当复制开始的时候，从库就会创建两个线程进行处理：

2. **从库I/O线程**:当START SLAVE语句在从库开始执行之后，从库创建一个I/O线程，该线程连接到主库并请求主库发送binlog里面的更新记录到从库上。从库I/O线程读取主库的binlog输出线程发送的更新并拷贝这些更新到本地文件，其中包括relay log文件。

3. **从库的SQL线程:**从库创建一个SQL线程，这个线程读取从库I/O线程写到relay log的更新事件并执行。

可以知道，对于每一个主从复制的连接，都有三个线程。拥有多个从库的主库为每一个连接到主库的从库创建一个binlog输出线程，每一个从库都有它自己的I/O线程和SQL线程。

主从复制如图：

![20b8b8329f5e313c520e3d67c8551dfb.png](https://img-blog.csdnimg.cn/img_convert/20b8b8329f5e313c520e3d67c8551dfb.png)

原理图2,帮助理解!

![f348ea99c25118f053cc50a9ed0d77a2.png](https://img-blog.csdnimg.cn/img_convert/f348ea99c25118f053cc50a9ed0d77a2.png)

步骤一：主库db的更新事件(update、insert、delete)被写到binlog

步骤二：从库发起连接，连接到主库

步骤三：此时主库创建一个binlog dump thread线程，把binlog的内容发送到从库

步骤四：从库启动之后，创建一个I/O线程，读取主库传过来的binlog内容并写入到relay log.

步骤五：还会创建一个SQL线程，从relay log里面读取内容，从Exec\_Master\_Log\_Pos位置开始执行读取到的更新事件，将更新内容写入到slave的db.

# 四、从数据库的读的延迟问题了解吗？如何解决？

原因：当主库的TPS并发较高时，产生的DDL数量超过slave一个sql线程所能承受的范围，那么延时就产生了，当然还有就是可能与slave的大型query语句产生了锁等待，还有网络延迟。(谈到MySQL数据库主从同步延迟原理，得从mysql的数据库主从复制原理说起，mysql的主从复制都是单线程的操作，主库对所有DDL和DML产生binlog，binlog是顺序写，所以效率很高；slave的Slave\_IO\_Running线程会到主库取日志，效率会比较高，slave的Slave\_SQL\_Running线程将主库的DDL和DML操作都在slave实施。DML和DDL的IO操作是随机的，不是顺序的，因此成本会很高，还可能是slave上的其他查询产生lock争用，由于Slave\_SQL\_Running也是单线程的，所以一个DDL卡主了，需要执行10分钟，那么所有之后的DDL会等待这个DDL执行完才会继续执行，这就导致了延时。有朋友会问：“主库上那个相同的DDL也需要执行10分，为什么slave会延时？”，答案是master可以并发，Slave\_SQL\_Running线程却不可以。)

解决方法一：最简单的减少slave同步延时的方案就是在架构上做优化，尽量让主库的DDL快速执行。还有就是主库是写，对数据安全性较高，比如sync\_binlog=1，innodb\_flush\_log\_at\_trx\_commit = 1 之类的设置，而slave则不需要这么高的数据安全，完全可以讲sync\_binlog设置为0或者关闭binlog，innodb\_flushlog也可以设置为0来提高sql的执行效率。另外就是使用比主库更好的硬件设备作为slave。

解决方法二：数据放入缓存中，更新数据库后，在预期可能马上用到的情况下，主动刷新缓存。

解决办法三：对于比较重要且必须实时的数据，比如用户刚换密码(密码写入 Master)，然后用新密码登录(从 Slaves 读取密码)，会造成密码不一致，导致用户短时间内登录出错。所以在这种需要读取实时数据的时候最好从 Master 直接读取，避免 Slaves 数据滞后现象发生。

# 五：做主从后主服务器挂了怎么办？

假设发生了突发事件，master宕机，现在的需求是要将192.168.1.102提升为主库，另外一个为从库

步骤：

1.**确保所有的relay log全部更新完毕**，在每个从库上执行stop slave io\_thread; show processlist;直到看到Has read all relay log,则表

示从库更新都执行完毕了

2.**登陆所有从库**，查看master.info文件，对比**选择pos最大的作为新的主库**，这里我们选择192.168.1.102为新的主库

3.登陆192.168.1.102，**执行stop slave**; 并进入数据库目录，删除master.info和relay-log.info文件, **配置my.cnf文件**，**开启log-bin**,如果有

log-slaves-updates和read-only则要注释掉，执行reset master

4.创建用于同步的用户并授权slave，同第五大步骤

5.登录另外一台从库，执行stop slave停止同步

6.根据第七大步骤连接到新的主库

7.执行start slave;

8.修改新的master数据，测试slave是否同步更新

读写分离实现方法：

为了减轻数据库的压力，一般会进行数据库的读写分离，实现方法一是通过分析sql语句是insert/select/update/delete中的哪一种，从而对应选择主从，二是通过拦截方法名称的方式来决定主从的，如：save\*()、insert\*() 形式的方法使用master库，select()开头的使用slave库。

虽然大多数都是从程序里直接实现读写分离的，但对于分布式的部署和水平和垂直分割，一些代理的类似中间件的软件还是挺实用的，如 MySQL Proxy比较。mysql proxy根本没有配置文件， lua脚本就是它的全部，当然lua是相当方便的。

六：innodb\_flush\_log\_at\_trx\_commit 和 sync\_binlog

innodb\_flush\_log\_at\_trx\_commit 和 sync\_binlog 是 MySQL 的两个配置参数。它们的配置对于 MySQL 的性能有很大影响(一般为了保证数据的不丢失，会设置为双1，该情形下数据库的性能也是最低的)。

1、innodb\_flush\_log\_at\_trx\_commit

innodb\_flush\_log\_at\_trx\_commit：是 InnoDB 引擎特有的，ib\_logfile的刷新方式( ib\_logfile：记录的是redo log和undo log的信息)

取值:0/1/2

innodb\_flush\_log\_at\_trx\_commit=0，表示每隔一秒把log buffer刷到文件系统中(os buffer)去，并且调用文件系统的“flush”操作将缓存刷新到磁盘上去。也就是说一秒之前的日志都保存在日志缓冲区，也就是内存上，如果机器宕掉，可能丢失1秒的事务数据。

innodb\_flush\_log\_at\_trx\_commit=1，表示在每次事务提交的时候，都把log buffer刷到文件系统中(os buffer)去，并且调用文件系统的“flush”操作将缓存刷新到磁盘上去。这样的话，数据库对IO的要求就非常高了，如果底层的硬件提供的IOPS比较差，那么MySQL数据库的并发很快就会由于硬件IO的问题而无法提升。

innodb\_flush\_log\_at\_trx\_commit=2，表示在每次事务提交的时候会把log buffer刷到文件系统中去，但并不会立即刷写到磁盘。如果只是MySQL数据库挂掉了，由于文件系统没有问题，那么对应的事务数据并没有丢失。只有在数据库所在的主机操作系统损坏或者突然掉电的情况下，数据库的事务数据可能丢失1秒之类的事务数据。这样的好处，减少了事务数据丢失的概率，而对底层硬件的IO要求也没有那么高(log buffer写到文件系统中，一般只是从log buffer的内存转移的文件系统的内存缓存中，对底层IO没有压力)。

2、sync\_binlog

sync\_binlog：是MySQL 的二进制日志(binary log)同步到磁盘的频率。

取值：0-N

sync\_binlog=0，当事务提交之后，MySQL不做fsync之类的磁盘同步指令刷新binlog\_cache中的信息到磁盘，而让Filesystem自行决定什么时候来做同步，或者cache满了之后才同步到磁盘。这个是性能最好的。

sync\_binlog=1，当每进行1次事务提交之后，MySQL将进行一次fsync之类的磁盘同步指令来将binlog\_cache中的数据强制写入磁盘。

sync\_binlog=n，当每进行n次事务提交之后，MySQL将进行一次fsync之类的磁盘同步指令来将binlog\_cache中的数据强制写入磁盘。

注：

大多数情况下，对数据的一致性并没有很严格的要求，所以并不会把 sync\_binlog 配置成 1. 为了追求高并发，提升性能，可以设置为 100 或直接用 0.

而和 innodb\_flush\_log\_at\_trx\_commit 一样，对于支付服务这样的应用，还是比较推荐 sync\_binlog = 1.