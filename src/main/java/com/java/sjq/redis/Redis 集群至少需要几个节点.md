> [Redis replication | Redis --- Redis复制|雷迪斯](https://redis.io/docs/management/replication/)
> 

## redis集群为什么最少需要6个节点组成?
 - Redis集群至少需要3个master节点 1master对应1slave ,所以redis集群需要6个节点


## 为什么哨兵节点至少要有 3 个？
- 如果哨兵集群中只有 2 个哨兵节点，此时如果一个哨兵想要成功成为 Leader，必须获得 2 票，而不是 1 票。

- 所以，如果哨兵集群中有个哨兵挂掉了，那么就只剩一个哨兵了，如果这个哨兵想要成为 Leader，这时票数就没办法达到 2 票，就无法成功成为 Leader，这时是无法进行主从节点切换的。

## 问题来了，由哨兵集群中的哪个节点进行主从故障转移呢？

- 所以这时候，还需要在哨兵集群中选出一个 leeder，让 leeder 来执行主从切换。

- 选举 leeder 的过程其实是一个投票的过程，在投票开始前，肯定得有个「候选者」。

## 集群是如何判断是否有某个节点挂掉

- 首先要说的是，每一个节点都存有这个集群所有主节点以及从节点的信息。它们之间通过互相的ping-pong判断是否节点可以连接上。如果有一半以上的节点去ping一个节点的时候没有回应，集群就认为这个节点宕机了，然后去连接它的备用节点。



## 集群进入fail状态的必要条件

- A、某个主节点和所有从节点全部挂掉，我们集群就进入faill状态。

- B、如果集群超过半数以上master挂掉，无论是否有slave，集群进入fail状态.

- C、如果集群任意master挂掉,且当前master没有slave.集群进入fail状态

## redis的投票机制

具体原理如下图所示：

![redis集群节点宕机](https://www.likecs.com/default/index/img?u=L2RlZmF1bHQvaW5kZXgvaW1nP3U9YUhSMGNITTZMeTlwYldGblpYTXlNREUxTG1OdVlteHZaM011WTI5dEwySnNiMmN2TVRFd05EQTRNaTh5TURFM01ESXZNVEV3TkRBNE1pMHlNREUzTURJeE1USXlNak13TmpBM01pMHhPRGN6TVRFek1UazVMbkJ1Wnc9PQ== "redis集群节点宕机")

投票过程是集群中所有master参与,如果半数以上master节点与master节点通信超时(cluster-node-timeout),认为当前master节点挂掉。

选举的依据依次是：**网络连接正常->5秒内回复过INFO命令->10\*down-after-milliseconds内与主连接过的->从服务器优先级->复制偏移量->运行id较小的**。选出之后通过`slaveif no ont`将该从服务器升为新主服务器。

通过slaveof ip port命令让其他从服务器复制该信主服务器。

最后当旧主重新连接后将其变为新主的从服务器。注意如果客户端与旧主服务器分隔在一起，写入的数据在恢复后由于旧主会复制新主的数据会造成数据丢失。

## 集群中的主从复制
集群中的每个节点都有1个至N个复制品，其中一个为主节点，其余的为从节点，如果主节点下线了，集群就会把这个主节点的一个从节点设置为新的主节点继续工作，这样集群就不会因为一个主节点的下线而无法正常工作。
注意：

- 1、如果某一个主节点和他所有的从节点都下线的话，redis集群就会停止工作了。redis集群不保证数据的强一致性，在特定的情况下，redis集群会丢失已经被执行过的写命令。
- 2、使用`异步复制`（asynchronous replication）是redis 集群可能会丢失写命令的其中一个原因，有时候由于网络原因，如果网络断开时间太长，redis集群就会启用新的主节点，之前发给主节点的数据就会丢失。

## 哨兵节点之间是通过 Redis 的发布者/订阅者机制来相互发现的。
配置哨兵的信息:
```redis
sentinel monitor <master-name> <ip> <redis-port> <quorum>
```
正式通过 Redis 的发布者/订阅者机制，哨兵之间可以相互感知，然后组成集群，同时，哨兵又通过 INFO 命令，在主节点里获得了所有从节点连接信息，于是就能和从节点建立连接，并进行监控了。

在主从集群中，主节点上有一个名为__sentinel__:hello的频道，不同哨兵就是通过它来相互发现，实现互相通信的。

## 一主多从与分布式集群
一主多从也是集群，这种的就相当于备份。 还有一种叫分布式集群，比如：一份数据分为两个半份或者多份存在了不同的机器上，这就是分布式集群。

## 设置从节点和查看集群情况
设置从节点 `slaveof 192.168.1.102 6379`

全部从节点配置好后，使用 `info replication ` 可以看到 当前role: master 主节点，多条slave 是它的从节点