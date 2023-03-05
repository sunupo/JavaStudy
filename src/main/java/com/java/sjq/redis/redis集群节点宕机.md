[redis集群节点宕机 - 爱码网](https://www.likecs.com/show-305747638.html)

redis集群是有很多个redis一起工作，那么就需要这个集群不是那么容易挂掉，所以呢，理论上就应该给集群中的每个节点至少一个备用的redis服务。这个备用的redis称为从节点（slave）。

**1、集群是如何判断是否有某个节点挂掉**

首先要说的是，每一个节点都存有这个集群所有主节点以及从节点的信息。它们之间通过互相的ping-pong判断是否节点可以连接上。如果有一半以上的节点去ping一个节点的时候没有回应，集群就认为这个节点宕机了，然后去连接它的备用节点。

**2、集群进入fail状态的必要条件**

A、某个主节点和所有从节点全部挂掉，我们集群就进入faill状态。

B、如果集群超过半数以上master挂掉，无论是否有slave，集群进入fail状态.

C、如果集群任意master挂掉,且当前master没有slave.集群进入fail状态

**3、redis的投票机制**

具体原理如下图所示：

![redis集群节点宕机](https://www.likecs.com/default/index/img?u=L2RlZmF1bHQvaW5kZXgvaW1nP3U9YUhSMGNITTZMeTlwYldGblpYTXlNREUxTG1OdVlteHZaM011WTI5dEwySnNiMmN2TVRFd05EQTRNaTh5TURFM01ESXZNVEV3TkRBNE1pMHlNREUzTURJeE1USXlNak13TmpBM01pMHhPRGN6TVRFek1UazVMbkJ1Wnc9PQ== "redis集群节点宕机")

投票过程是集群中所有master参与,如果半数以上master节点与master节点通信超时(cluster-node-timeout),认为当前master节点挂掉。

选举的依据依次是：网络连接正常->5秒内回复过INFO命令->10\*down-after-milliseconds内与主连接过的->从服务器优先级->复制偏移量->运行id较小的。选出之后通过slaveif no ont将该从服务器升为新主服务器。

通过slaveof ip port命令让其他从服务器复制该信主服务器。

最后当旧主重新连接后将其变为新主的从服务器。注意如果客户端与旧主服务器分隔在一起，写入的数据在恢复后由于旧主会复制新主的数据会造成数据丢失。

**4、集群中的主从复制**  
      集群中的每个节点都有1个至N个复制品，其中一个为主节点，其余的为从节点，如果主节点下线了，集群就会把这个主节点的一个从节点设置为新的主节点继续工作，这样集群就不会因为一个主节点的下线而无法正常工作。  
注意：  
1、如果某一个主节点和他所有的从节点都下线的话，redis集群就会停止工作了。redis集群不保证数据的强一致性，在特定的情况下，redis集群会丢失已经被执行过的写命令。  
2、使用异步复制（asynchronous replication）是redis 集群可能会丢失写命令的其中一个原因，有时候由于网络原因，如果网络断开时间太长，redis集群就会启用新的主节点，之前发给主节点的数据就会丢失。

原文链接：https://www.cnblogs.com/dadonggg/p/8628735.html