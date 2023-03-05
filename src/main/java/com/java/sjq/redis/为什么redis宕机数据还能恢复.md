Redis有两种持久化机制。
RDB：将内存中的数据库定时dump到磁盘上
AOF：将redis的命令以日志追加的方式写入到文件

### RDB

RDB的持久化是指子啊指定时间内将内存中的数据快照写入磁盘里面，定义一个二进制的格式。
为了避免频繁备份产生不必要的消耗，redis用过过参数配置来确定生成快照评率。

```redis
save 60 1000 # 如果 60 秒内有 1000 个写入
```

RDB 通过  fork 子进程来进行的备份。
```redis
bgsave
```

### AOF

RDB周期性备份若没达到备份设置条件，数据就丢失了。

于是 AOF 每次过滤读操作，将写操作记录放到缓冲区，最后刷到磁盘。

刷磁盘参数：

```
always
everysec
no
```

为了解决 aof 记录的写命令太多的问题，aof 会 fork 子进程的触发重写，每次触发的时候，会将新的写入命令也放进重写缓冲区，防止数据不一致。
```redis
bgrewriteaof
```


[【转】Redis数据备份和重启恢复 - 撸码识途 - 博客园](https://www.cnblogs.com/tinyj/p/9874094.html)

Redis的RDB文件不会坏掉，因为其写操作是在一个新进程中进行的。当生成一个新的RDB文件时，Redis生成的子进程会先将数据写到一个临时文件中，然后通过原子性rename系统调用将临时文件重命名为RDB文件。
这样在任何时候出现故障，Redis的RDB文件都总是可用的。同时，Redis的RDB文件也是Redis主从同步内部实现中的一环。

第一次Slave向Master同步的实现是：

Slave向Master发出同步请求，Master先dump出rdb文件，然后将rdb文件全量传输给slave，然后Master把缓存的命令转发给Slave，初次同步完成。
第二次以及以后的同步实现是：
Master将变量的快照直接实时依次发送给各个Slave。
但不管什么原因导致Slave和Master断开重连都会重复以上两个步骤的过程。
Redis的主从复制是建立在内存快照的持久化基础上的，只要有Slave就一定会有内存快照发生。

