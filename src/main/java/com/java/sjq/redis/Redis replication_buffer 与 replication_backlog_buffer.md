[(125条消息) Redis replication\_buffer 与 replication\_backlog\_buffer 区别与联系 | 复制缓冲区、复制积压缓冲区\_replication buffer\_血煞长虹的博客-CSDN博客](https://blog.csdn.net/succing/article/details/121230604)

## 前言

> 本文旨在用最少文字，清晰的表达出它们之间的关系，希望对你有所帮助！
>
> redis 版本 [5.0](http://download.redis.io/releases/redis-5.0.4.tar.gz "5.0")\+ [6.2.6](https://download.redis.io/releases/redis-6.2.6.tar.gz "6.2.6")，[中文官网（更新略慢）](https://www.redis.net.cn/download/ "中文官网（更新略慢）")/[英文官网(更新较快)](https://redis.io/download "英文官网(更新较快)")
>
> 附注：[Redis安装部署 | CentOS7.9+Redis6.2.6](https://blog.csdn.net/succing/article/details/121106990?spm=1001.2014.3001.5501 "Redis安装部署 | CentOS7.9+Redis6.2.6 ")   [Redis安装部署 | CentOS6.5+Redis5.0.4](https://blog.csdn.net/succing/article/details/120914749 "Redis安装部署 | CentOS6.5+Redis5.0.4")

## 一、概述

> replication 顾名思义，就是复制的意思；
>
> buffer是缓冲区的意思，两者合在一起replication\_buffer就是**复制缓冲区**；

> backlog英文释义，是积压的意思；
>
> 三者合在一起 replication\_backlog\_buffer，就是**复制****积压****缓冲区**

## 二、主从复制原理

## 1.关于Replication ID, offset

> master独有固定的Replication ID，据此与所属slave通信。
>
> offset，即为偏移量，每当有新的数据进来时，每进来1个字节offset就会自增+1，此处记做 master\_repl\_offset。

> maser通过全量rdb把数据同步给slave后，后面的通信便步入增量通信阶段。
>
> 每次slave接收到的增量数据后，都会自己标记一个offset，此处记做 slave\_repl\_offset

##  2.主从复制过程

当master与所属slave首次建立连接后会进行全量rdb，后期（即使中间有断开连接，又重新恢复主从关系）优先使用增量同步数据，实在无法增量（比如：主的复制积压缓冲区爆满后，会重新落地rdb，然后主从就无法通过各自的offset偏移量计算出增量数据），才会走全量rdb。

> 增量传输计算规则：  
> master\_repl\_offset 和 slave\_repl\_offset ，中间相差的这一部分，即为本次要增量传输的。
>
> 注：slave每次和master建立通信时，将会发送psync命令（包含复制的偏移量offset），请求partial resync（增量数据同步）。如果请求的offset不存在，那么执行全量的sync操作，相当于重新建立主从复制。

## 三、两者的使用场景

## 1.replication [buffer](https://so.csdn.net/so/search?q=buffer&spm=1001.2101.3001.7020)的使用场景

### 1.replication\_buffer的产生

> master不定时的后台触发bgsave命令，落成新的dump.rdb文件。
>
> bgsave的同时，如有新数据，会被暂时存放在replication buffer中。

注：如果主库传输 RDB 文件以及从库加载 RDB 文件耗时长，同时主库接收的写命令操作较多，就会导致复制缓冲区被写满而溢出。一旦溢出，主库就会关闭和从库的网络连接，重新开始全量同步。 

### 2.replication\_buffer的被首次使用

> 当且仅当slave与master首次或者出于某种原因，需要全量rdb传输数据后，然后会把replication\_buffer中的数据，再次全量传给slave。

注：此阶段称作主从复制的第一阶段，全量rdb+ replication\_buffer。

第二阶段(命令传播)，主要是增量传输，此时replication\_backlog\_buffer出场。

## 2.replication\_backlog\_buffer的使用场景

> 当主从全量rdb后，master会把rdb通信期间收到新的数据的操作命令，写入 replication buffer，同时也会把这些数据操作命令也写入 repl\_backlog\_buffer 这个缓冲区，它里面保存着最新传输的命令。

> 如果从节点和主节点间发生了网络断连，等从节点再次连接后，可以从repl\_backlog\_buffer中同步尚未复制的命令操作。
>
> 对主从同步的影响：如果从节点和主节点间的网络断连时间过长，复制积压缓冲区可能被新写入的命令覆盖。此时，从节点就没有办法和主节点进行增量复制了，而是只能进行全量复制。针对这个问题，应对的方法是调大复制积压缓冲区的大小

**总述：**复制缓冲区，主要用于首次rdb后的首次增量同步数据，复制积压缓冲区主要用于常规的增量数据同步。当无法完成增量同步时，就会全量同步，全量同步后的第一次增量，仍是使用复制缓冲区。 

## 四、两者的参数设定

## 1.replication\_buffer

> 通过client-output-buffer-limit slave 参数设置，当这个值太小会导致主从复制链接断开，从而引发严重问题。
>
> 问题解析：参数值设置太小，就会导致replication\_buffer不够用，新增的数据也就无法存入该缓冲区。反向会强迫master为了不让即将溢出的这部分丢失，后台自动的进行bgsave落成新的rdb。因此也会导致主从之间不得不再次全量同步，问题严重的话，maser会不停的bgsave,主从之间会不停的全量rdb同步数据，从而影响到整个服务的性能和质量。

> 语法：client-output-buffer-limit <class> <hard limit> <soft limit> <soft seconds>  
> class可选值有三个，Normal，Slaves和Pub/Sub；  
> hard limit: 缓冲区大小的硬性限制。  
> soft limit: 缓冲去大小的软性限制。  
> soft seconds: 缓冲区大小达到了（超过）soft limit值的持续时间。
>
> Normal: 普通的客户端。默认limit 是0，也就是不限制。  
> Pub/Sub: 发布与订阅的客户端的。默认hard limit 32M，soft limit 8M/60s。  
> Slaves: 从库的复制客户端。默认hard limit 256M，soft limit 64M/60s。

```
示例如下：client-output-buffer-limit normal 0 0 0client-output-buffer-limit slave 256mb 64mb 60client-output-buffer-limit pubsub 32mb 8mb 60
```

##  2.replication\_backlog\_buffer

> 通过repl-backlog-size参数设置，默认大小是1M。
>
> 大体算法如下：  
> 每秒产生的命令 乘以（（master执行rdb bgsave的时间）+ (master发送rdb到slave的时间) + (slave load rdb文件的时间) ) ，来估算积压缓冲区的大小，repl-backlog-size 值不小于这两者的乘积。 
>
> 例如，如果主服务器平均每秒产生1 MB的写数据，而从服务器断线之后平均要5秒才能重新连接上主服务器，那么复制积压缓冲区的大小就不能低于5MB。  
> 为了安全起见，可以将复制积压缓冲区的大小设为2\*5=10M，这样可以保证绝大部分断线情况都能用增量从而避免全量同步数据。