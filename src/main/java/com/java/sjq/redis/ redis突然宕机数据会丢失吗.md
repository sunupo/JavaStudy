[(122条消息) redis突然宕机数据会丢失吗\_redis崩溃了,数据库会怎么样\_march of Time的博客-CSDN博客](https://blog.csdn.net/qq_41358574/article/details/124080581)

### 文章目录

-   [断电为什么数据会丢失](https://blog.csdn.net/qq_41358574/article/details/124080581#_9)
-   [redis的持久化机制](https://blog.csdn.net/qq_41358574/article/details/124080581#redis_15)
-   [rdb机制](https://blog.csdn.net/qq_41358574/article/details/124080581#rdb_19)
-   -   [RDB 优缺点](https://blog.csdn.net/qq_41358574/article/details/124080581#RDB__55)
-   [在生成 RDB 期间，Redis 可以同时处理写请求么？](https://blog.csdn.net/qq_41358574/article/details/124080581#_RDB_Redis__74)
-   [aof机制](https://blog.csdn.net/qq_41358574/article/details/124080581#aof_83)
-   -   [aof的“写后日志”](https://blog.csdn.net/qq_41358574/article/details/124080581#aof_109)
-   [aof的重写机制](https://blog.csdn.net/qq_41358574/article/details/124080581#aof_136)
-   [混合持久化机制](https://blog.csdn.net/qq_41358574/article/details/124080581#_153)
-   [总结](https://blog.csdn.net/qq_41358574/article/details/124080581#_172)

___

之前看到有人面试时问了这个问题，这里记录一下自己的思考和总结，加整理之前记录的一些笔记。

## 断电为什么数据会丢失

首先要知道Redis是一个内存数据库，平时在读写Redis时比如set get命令都是直接对内存进行操作的，这也是redis为什么比mysql等关系型数据库快的原因，内存中的数据我们知道关机后都是会丢失的，比如平常笔记本电脑如果突然没电自动关机，再次开机的时候会发现之前正在运行的程序比如qq或者其他应用软件等自然都退出了，需要重新启动软件才行。  
（额外补充：一般电脑内存的Ram,RAM又叫运行内存,存放临时程序的,速度要远大于ROM，ram一般是指随机存取存储器,也叫主存,是与CPU直接交换数据的内部存储器。ROM即read-only memory,只读存储器，用来存放不可更改的电脑的出厂信息）

那么这里自然发现一个问题，redis既然是内存数据库为什么我们发现就算自己本地启动的redis在关闭后再次开启发现里面的数据都还在呢？这里可以想到Redis必然是有某种持久化机制，在我们每次停止然后又[启动Redis](https://so.csdn.net/so/search?q=%E5%90%AF%E5%8A%A8Redis&spm=1001.2101.3001.7020)服务以后，它会从可以持久化存储的硬盘中“取出”之前放在redis中的数据—这样才能做到数据不丢失，否则如果每次启动Redis它的数据都丢失的话，平常在redis中存储的缓存或者是一些token等数据岂不是都没了？

## redis的持久化机制

经过上面的讨论现在已经知道了redis的确有一套持久化机制，那么是不是可以认为redis的机器即时宕机数据也一定不会丢失呢？当然不是这样的。下面就讨论一下redis的持久化机制。

## [rdb](https://so.csdn.net/so/search?q=rdb&spm=1001.2101.3001.7020)机制

redis一开始最先的是rdb持久化。rdb持久化方式也叫快照持久化。**Redis可以通过创建快照来获得存储在内存里面的数据在某个时间点上的副本。Redis创建快照之后，可以对快照进行备份，可以将快照复制到其他服务器从而创建具有相同数据的服务器副本**（Redis主从结构，主要用来提高Redis性能），还可以将快照留在原地以便重启服务器的时候使用。  
图示：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/56904d2d71c84bbcbd9883f2a94f281f.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAbWFyY2ggb2YgVGltZQ==,size_18,color_FFFFFF,t_70,g_se,x_16)  
这种持久化有两种触发方式：  
⼀类是⼿动触发，另⼀类是⾃动触发。  
1）⼿动触发  
⼿动触发持久化的操作有两个： save 和 bgsave ，它们主要区别体现在：是否阻塞 Redis 主线程的执⾏。  
① save 命令  
在客⼾端中执⾏ save 命令，就会触发 Redis 的持久化，但同时也是使 Redis 处于阻塞状态，直到 RDB 持久化完成，才会响应其他客⼾端发来的命令，所以在⽣产环境⼀定要慎⽤。  
save 命令使⽤如下：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/2e2787df06384759b96a50f65e205cd9.png)  
从图⽚可以看出，当执⾏完 save 命令之后，持久化⽂件 dump.rdb 的修改时间就变了，这就表⽰ save 成功的触发了 RDB 持久化。save 命令执⾏流程，如下图所⽰：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/f8c993c0207d469c968546069611cd8b.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAbWFyY2ggb2YgVGltZQ==,size_10,color_FFFFFF,t_70,g_se,x_16)  
② bgsave 命令  
bgsave（background save）既后台保存的意思， 它和 save 命令最⼤的区别就是 bgsave 会 fork() ⼀个⼦进程来执⾏持久化，整个过程中只有在 fork() ⼦进程时有短暂的阻塞，当⼦进程被创建之后，Redis 的主进程就可以响应其他客⼾端的请求了，相对于整个流程都阻塞的 save 命令来说，显然 bgsave 命令更适合我们使⽤。bgsave 命令使⽤，如下图所⽰：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/c17baa25ece0467fadeec50ce685ba68.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAbWFyY2ggb2YgVGltZQ==,size_10,color_FFFFFF,t_70,g_se,x_16)

两种方式对比：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/9e64fa6a17234345a1c55f0b87d54959.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAbWFyY2ggb2YgVGltZQ==,size_20,color_FFFFFF,t_70,g_se,x_16)

2）⾃动触发  
说完了 RDB 的⼿动触发⽅式，下⾯来看如何⾃动触发 RDB 持久化？RDB ⾃动持久化主要来源于以下⼏种情况。  
① save m n  
save m  
n 是指在 m 秒内，如果有 n 个键发⽣改变，则⾃动触发持久化。参数 m 和 n 可以在 Redis 的配置⽂件中找到，例如，  
save601 则表明在 60 秒内，⾄少有⼀个键发⽣改变，就会触发 RDB 持久化。⾃动触发持久化，本质是 Redis 通过判断，如果满⾜设置的触发条件，⾃动执⾏⼀次 bgsave 命令。注意：当设置多个 save m n 命令时，满⾜任意⼀个条件都会触发持久化。例如，我们设置了以下两个 save m n 命令：  
save 60 10  
save 600 1  
当 60s 内如果有 10 次 Redis 键值发⽣改变，就会触发持久化；如果 60s 内 Redis 的键值改变次数少于 10 次，那么 Redis 就会判断600s 内，Redis 的键值是否⾄少被修改了⼀次，如果满⾜则会触发持久化。  
**配置自动生成rdb文件后台使用的是bgsave方式。**  
② flushall  
flushall 命令⽤于清空 Redis 数据库，在⽣产环境下⼀定慎⽤，当 Redis 执⾏了 flushall 命令之后，则会触发⾃动持久化，把 RDB⽂件清空.  
③ 主从同步触发  
在 Redis 主从复制中，当从节点执⾏全量复制操作时，主节点会执⾏ bgsave 命令，并将 RDB ⽂件发送给从节点，该过程会⾃动触发 Redis 持久化。

## RDB 优缺点

1）RDB 优点  
**RDB 的内容为⼆进制的数据，占⽤内存更⼩**，更紧凑，更适合做为备份⽂件；  
RDB 对灾难恢复⾮常有⽤，它是⼀个紧凑的⽂件，可以更快的传输到远程服务器进⾏ Redis 服务恢复；  
RDB 可以更⼤程度的提⾼ Redis 的运⾏速度，因为每次持久化时 Redis 主进程都会 fork() ⼀个⼦进程，进⾏数据持久化到磁盘，Redis 主进程并不会执⾏磁盘 I/O 等操作；**与 AOF 格式的⽂件相⽐，RDB ⽂件可以更快的重启。**

2）RDB 缺点  
因为 RDB 只能保存某个时间间隔的数据，如果中途 Redis 服务被意外终⽌了，则会丢失⼀段时间内的 Redis 数据；

RDB 需要经常 fork() 才能使⽤⼦进程将其持久化在磁盘上。**如果数据集很⼤，fork() 可能很耗时，并且如果数据集很⼤且CPU 性能不佳，则可能导致 Redis 停⽌为客⼾端服务⼏毫秒甚⾄⼀秒钟。**

禁⽤持久化  
禁⽤持久化可以提⾼ Redis 的执⾏效率，如果对数据丢失不敏感的情况下，可以在连接客⼾端的情况下，执⾏

```
config set save "" 
```

命令即可禁⽤ Redis 的持久化

## 在生成 RDB 期间，Redis 可以同时处理写请求么？

可以的，Redis 使用操作系统的多进程**写时复制技术 COW(Copy On Write)** 来实现快照持久化，保证数据一致性。

Redis 在持久化时会调用 glibc 的函数`fork`产生一个子进程，快照持久化完全交给子进程来处理，父进程继续处理客户端请求。

当主线程执行写指令修改数据的时候，这个数据就会复制一份副本， `bgsave` 子进程读取这个副本数据写到 RDB 文件。

这既保证了快照的完整性，也允许主线程同时对数据进行修改，避免了对正常业务的影响。

## [aof](https://so.csdn.net/so/search?q=aof&spm=1001.2101.3001.7020)机制

快照功能并不是非常耐久（durable）： 如果 Redis 因为某些原因而造成故障停机， 那么服务器将丢失  
最近写入、且仍未保存到快照中的那些数据。从 1.1 版本开始， Redis 增加了一种完全耐久的持久化方式： AOF 持久化，将修改的每一条指令记录进文件appendonly.aof中(先写入os cache，每隔一段时间fsync到磁盘)

比如执行命令“set name 666”，aof文件里会记录如下数据  
\*3  
$3  
set  
$5  
name  
$3  
666

这是一种resp协议格式数据，星号后面的数字代表命令有多少个参数，$号后面的数字代表这个参数有几个字符  
注意，如果执行带过期时间的set命令，aof文件里记录的是并不是执行的原始命令，而是记录key过期的时间戳  
可以通过修改配置文件来打开 AOF 功能：

```
# appendonly yes
```

从现在开始， 每当 Redis 执行一个改变数据集的命令时（比如 SET）， 这个命令就会被追加到 AOF 文件的末尾。这样的话， 当 Redis 重新启动时， 程序就可以通过重新执行 AOF 文件中的命令来达到重建数据集的目的。

## aof的“写后日志”

说到日志，我们比较熟悉的是数据库的写前日志（Write Ahead Log, WAL），也就是说，__在实际写数据前，先把修改的数据记到日志文件中，以便故障时进行恢复。**不过，AOF 日志正好相反，它是写后日志，**_\*“写后”的意思是 Redis 是先执行命令，把数据写入内存，然后才记录日志  
![在这里插入图片描述](https://img-blog.csdnimg.cn/ef16af1c8a964eacbf1253a2a9c3d693.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAbWFyY2ggb2YgVGltZQ==,size_18,color_FFFFFF,t_70,g_se,x_16)  
但是，为了避免额外的检查开销，Redis 在向 AOF 里面记录日志的时候，_**\*并不会先去对这些命令进行语法检查。所以，如果先记日志再执行命令的话，日志中就有可能记录了错误的命令，Redis 在使用日志恢复数据时，就可能会出错。\***\*

而写后日志这种方式，就是先让系统执行命令，只有命令能执行成功，才会被记录到日志中，否则，系统就会直接向客户端报错。所以，Redis 使用写后日志这一方式的一大好处是，可以避免出现记录错误命令的情况。

除此之外，AOF 还有一个好处：**\*它是在命令执行后才记录日志，所以不会阻塞当前的写操作。\***

不过，AOF 也有两个潜在的风险。

1.  首先，如果刚执行完一个命令，还没有来得及记日志就宕机了，那么这个命令和相应的数据就有丢失的风险。如果此时 Redis 是用作缓存，还可以从后端数据库重新读入数据进行恢复，但是，如果 Redis 是直接用作数据库的话，此时，因为命令没有记入日志，所以就无法用日志进行恢复了。
2.  其次，\*\*\*\*AOF 虽然避免了对当前命令的阻塞，但可能会给下一个操作带来阻塞风险。\*\*\*\*这是因为，AOF 日志也是在主线程中执行的，如果在把日志文件写入磁盘时，磁盘写压力大，就会导致写盘很慢，进而导致后续的操作也无法执行了。

仔细分析的话，你就会发现，这两个风险都是和 AOF 写回磁盘的时机相关的。这也就意味着，如果我们能够控制一个写命令执行完后 AOF 日志写回磁盘的时机，这两个风险就解除了。

这里你可以配置 Redis 多久才将数据 fsync 到磁盘一次。  
有三个选项：

```
1 appendfsync always：每次有新命令追加到 AOF 文件时就执行一次 fsync ，非常慢，也非常安全。
2 appendfsync everysec：每秒 fsync 一次，足够快，并且在故障时只会丢失 1 秒钟的数据。
3 appendfsync no：从不 fsync ，将数据交给操作系统来处理。更快，也更不安全的选择。
```

推荐（并且也是默认）的措施为每秒 fsync 一次， 这种 fsync 策略可以兼顾速度和安全性。

## aof的重写机制

AOF文件里可能有太多没用指令，所以AOF会定期根据内存的最新数据生成aof文件  
如下两个配置可以控制AOF自动重写频率

```
 # auto‐aof‐rewrite‐min‐size 64mb //aof文件至少要达到64M才会自动重写，文件太小恢复速度本来就
```

很快，重写的意义不大

```
# auto‐aof‐rewrite‐percentage 100 //aof文件自上一次重写后文件大小增长了100%则再次触发重写
```

当然AOF还可以手动重写，进入redis客户端执行命令bgrewriteaof重写AOF  
注意，AOF重写redis会fork出一个子进程去做(与bgsave命令类似)，不会对redis正常命令处理有太多影响

## 混合持久化机制

Redis 4.0 混合持久化  
重启 Redis 时，我们很少使用 RDB来恢复内存状态，因为会丢失大量数据。我们通常使用 AOF 日志重放，但是重放 AOF 日志性能相对 RDB来说要慢很多，这样在 Redis 实例很大的情况下，启动需要花费很长的时间。 Redis 4.0 为了解决这个问题，带来了一个新的持久化选项——混合持久化。

通过如下配置可以开启混合持久化(必须先开启aof)：

```
aof‐use‐rdb‐preamble yes
```

如果开启了混合持久化，AOF在重写时，不再是单纯将内存数据转换为RESP命令写入AOF文件，**而是将重写这一刻之前的内存做RDB快照处理**，并且将RDB快照内容和增量的AOF修改内存数据的命令存在一起，都写入新的AOF文件，新的文件一开始不叫appendonly.aof，等到重写完新的AOF文件才会进行改名，覆盖原有的AOF文件，完成新旧两个AOF文件的替换。  
于是在 Redis 重启的时候，可以先加载 RDB 的内容，然后再重放增量 AOF 日志就可以完全替代之前的AOF 全量文件重放，因此重启效率大幅得到提升。  
混合持久化后文件格式：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/dd0599c9f9d5417e8366e36b00f5558a.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAbWFyY2ggb2YgVGltZQ==,size_10,color_FFFFFF,t_70,g_se,x_16)  
Redis数据备份策略：

1.  写crontab定时调度脚本，每小时都copy一份rdb或aof的备份到一个目录中去，仅仅保留最近48  
    小时的备份
2.  每天都保留一份当日的数据备份到一个目录中去，可以保留最近1个月的备份
3.  每次copy备份的时候，都把太旧的备份给删了
4.  每天晚上将当前机器上的备份复制一份到其他机器上，以防机器损坏

## 总结

这里其实讨论了redis的持久化机制，关于标题的问题，我理解的结论就是：不一定，取决于Redis所采用的的持久化策略是什么，但是不管是哪种策略，其实都存在数据丢失的风险，但是丢失的数据有多少跟操作有关（如果恰好写完后还没来及持久化就掉电了，当然会丢失数据了），不过在一般情况下rdb模式丢失的数据可能更多。