[(122条消息) Redis集群与分区\_redis分区和集群\_FYHannnnnn的博客-CSDN博客](https://blog.csdn.net/qq_42773863/article/details/121439984)

分区是将数据分布在多个Redis实例（Redis主机）上，以至于每个实例只包含一部分数据。

分区的意义：

1，**性能的提升，**单机Redis的网络I/O能力和计算资源是有限的，将请求分散到多台机器，充分利用多台机器的计算能力可网络带宽，有助于提高Redis总体的服务能力。

2，**存储能力的横向扩展，**即使Redis的服务能力能够满足应用需求，但是随着存储数据的增加，单台机器受限于机器本身的存储 容量，将数据分散到多台机器上存储使得Redis服务可以横向扩展。

## 一，范围分区

根据id数字的范围比如1--10000、100001--20000.....90001-100000，每个范围分到不同的Redis实例中。

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16.png)

-   好处： 实现简单，方便迁移和扩展
-   缺陷： 热点数据分布不均，性能损失 

## 二，hash分区 

Redis实例=hash(key)%N

-   好处： 支持任何类型的key 热点分布较均匀，性能较好
-   缺陷： 迁移复杂，需要重新计算，扩展较差（利用一致性hash环）

## **三，client端分区** 

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280431-1.png)

 客户端选择算法：普通hash算法、一致性hash、hash（服务器的IP地址） % 2^32

## 四，proxy端分区

在客户端和服务器端引入一个代理或代理集群，客户端将命令发送到代理上，由代理根据算法，将命令 路由到相应的服务器上。常见的代理有Codis（豌豆荚）和TwemProxy（Twitter）。

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280431-2.png)

##  五，cluster分区

Redis3.0之后，Redis官方提供了完整的集群解决方案。 方案采用去中心化的方式，包括：s**harding（分区）、replication（复制）、failover（故障转移）**。 称为RedisCluster。

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280431-3.png)

###  1，Gossip协议（病毒传播协议）

一个节点周期性(每秒)随机选择一些节点，并把信息传递给这些节点。 这些收到信息的节点接下来会做同样的事情，即把这些信息传递给其他一些随机选择的节点。 信息会周期性的传递给N个目标节点。这个N被称为fanout（扇出） gossip协议包含多种消息，包括meet、ping、pong、fail、publish等等。

【通过gossip协议，cluster可以提供集群间状态同步更新、选举自助failover等重要的集群功能。】

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280431-4.png)

###  **二，slot**

redis-cluster把所有的物理节点映射到\[0-16383\]个slot上,基本上采用平均分配和连续分配的方式。

**\*slot槽必须在节点上连续分配，如果出现不连续的情况，则RedisCluster不能工作**

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280431-5.png)

 **RedisCluster的优势：**

-   **高性能** Redis Cluster 的性能与单节点部署是同级别的。 多主节点、负载均衡、读写分离
-   **高可用** Redis Cluster 支持标准的 主从复制配置来保障高可用和高可靠。 failover Redis Cluster 也实现了一个类似 Raft 的共识方式，来保障整个集群的可用性。
-   **易扩展** 向 Redis Cluster 中添加新节点，或者移除节点，都是透明的，不需要停机。 水平、垂直方向都非常容易扩展。 数据分区，海量数据，数据存储
-   **原生** 部署 Redis Cluster 不需要其他的代理或者工具，而且 Redis Cluster 和单机 Redis 几乎完全兼容。

### 三，分片

不同节点分组服务于相互无交集的分片（sharding），Redis Cluster 不存在单独的proxy或配置服务 器，所以需要将客户端路由到目标的分片。

**客户端路由** Redis Cluster的客户端相比单机Redis 需要具备路由语义的识别能力，**且具备一定的路由缓存能力。**

**moved重定向（如果是在node1节点上插入数据，但是通过计算slot应该插入到node3，则会触发重定向）**

-   1.每个节点通过通信都会共享Redis Cluster中槽和集群中对应节点的关系
-   2.客户端向Redis Cluster的任意节点发送命令，接收命令的节点会根据CRC16规则进行hash运算与 16384取余，计算自己的槽和对应节点
-   3.如果保存数据的槽被分配给当前节点，则去槽中执行命令，并把命令执行结果返回给客户端
-   4.如果保存数据的槽不在当前节点的管理范围内，则向客户端返回moved重定向异常
-   5.客户端接收到节点返回的结果，如果是moved异常，则从moved异常中获取目标节点的信息
-   6.客户端向目标节点发送命令，获取命令执行结果

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280432-6.png)

 **ask重定向**

在对集群进行扩容和缩容时，需要对槽及槽中数据进行迁移 当客户端向某个节点发送命令，节点向客户端返回moved异常，告诉客户端数据对应的槽的节点信息 **如果此时正在进行集群扩展或者缩空操作，当客户端向正确的节点发送命令时，槽及槽中数据已经被迁 移到别的节点了，就会返回ask，这就是ask重定向机制**

-   1.客户端向目标节点发送命令，目标节点中的槽已经迁移支别的节点上了，此时目标节点会返回ask转 向给客户端
-   2.客户端向新的节点发送Asking命令给新的节点，然后再次向新节点发送命令
-   3.新节点执行命令，把命令执行结果返回给客户端

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280432-7.png)

 **Smart智能客户端-JedisCluster**

JedisCluster是Jedis根据RedisCluster的特性提供的集群智能客户端 JedisCluster为每个节点创建连接池，并跟节点建立映射关系缓存（Cluster slots） JedisCluster将每个主节点负责的槽位一一与主节点连接池建立映射缓存 JedisCluster启动时，已经知道key,slot和node之间的关系，可以找到目标节点 JedisCluster对目标节点发送命令，目标节点直接响应给JedisCluster 如果JedisCluster与目标节点连接出错，则JedisCluster会知道连接的节点是一个错误的节点 此时节点返回moved异常给JedisCluster JedisCluster会重新初始化slot与node节点的缓存关系，然后向新的目标节点发送命令，目标命令执行 命令并向JedisCluster响应 如果命令发送次数超过5次，则抛出异常"Too many cluster redirection!

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280432-8.png)

###  四，容灾（failover）

**故障检测**

-   集群中的每个节点都会定期地（每秒）向集群中的其他节点发送PING消息
-   如果在一定时间内(cluster-node-timeout)，发送ping的节点A没有收到某节点B的pong回应，则A将B 标识为pfail。
-   A在后续发送ping时，会带上B的pfail信息， 通知给其他节点。 如果B被标记为pfail的个数大于集群主节点个数的一半（N/2 + 1）时，B会被标记为fail，A向整个集群 广播，该节点已经下线。 其他节点收到广播，标记B为fail。

**从节点选举 （自动主从切换）**

1.  raft，每个从节点，都根据自己对master复制数据的offset，来设置一个选举时间，offset越大（复制数 据越多）的从节点，选举时间越靠前，优先进行选举。
2.  slave 通过向其他master发送FAILVOER\_AUTH\_REQUEST 消息发起竞选，master 收到后回复FAILOVER\_AUTH\_ACK 消息告知是否同意。
3.  slave 发送FAILOVER\_AUTH\_REQUEST 前会将currentEpoch 自增，并将最新的Epoch 带入到 FAILOVER\_AUTH\_REQUEST 消息中，如果自己未投过票，则回复同意，否则回复拒绝。
4.  所有的Master开始slave选举投票，给要进行选举的slave进行投票，如果大部分master node（N/2 + 1）都投票给了某个从节点，那么选举通过，那个从节点可以切换成master。
5.  **变更通知：**当slave 收到过半的master 同意时，会成为新的master。此时会以最新的Epoch 通过PONG 消息广播 自己成为master，让Cluster 的其他节点尽快的更新拓扑结构(node.conf)。

**RedisCluster失效的判定：**

-   1、集群中半数以上的主节点都宕机（无法投票）
-   2、宕机的主节点的从节点也宕机了（slot槽分配不连续） 

**从节点选举（手动切换）** 

人工故障切换是预期的操作，而非发生了真正的故障，目的是以一种安全的方式(数据无丢失)将当前 master节点和其中一个slave节点(执行cluster-failover的节点)交换角色

1、向从节点发送cluster failover 命令（slaveof no one）

2、从节点告知其主节点要进行手动切换（CLUSTERMSG\_TYPE\_MFSTART）

3、主节点会阻塞所有客户端命令的执行（10s）

4、从节点从主节点的ping包中获得主节点的复制偏移量

5、从节点复制达到偏移量，发起选举、统计选票、赢得选举、升级为主节点并更新配置

6、切换完成后，原主节点向所有客户端发送moved指令重定向到新的主节点

**以上是在主节点在线情况下。 如果主节点下线了，则采用cluster failover force或cluster failover takeover 进行强制切换。**

**副本漂移**

在一主一从的情况下，如果主从同时挂了，那整个集群就挂了。 为了避免这种情况我们可以做一主多从，但这样成本就增加了。 Redis提供了一种方法叫副本漂移，这种方法既能提高集群的可靠性又不用增加太多的从机。

如图所示，也就是说如果那个主节点下的slave比较多，少的那个快要达到峰值了，会去将其他主节点的Slave节点转变成自己的。

![](Redis%E9%9B%86%E7%BE%A4%E4%B8%8E%E5%88%86%E5%8C%BA.assets/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBARllIYW5ubm5ubg==,size_20,color_FFFFFF,t_70,g_se,x_16-1677947280432-9.png)

Master1宕机，则Slaver11提升为新的Master1 集群检测到新的Master1是单点的（无从机） 集群从拥有最多的从机的节点组（Master3）中，选择节点名称字母顺序最小的从机（Slaver31）漂移 到单点的主从节点组(Master1)。

具体流程如下（以上图为例）：

1、将Slaver31的从机记录从Master3中删除

2、将Slaver31的的主机改为Master1

3、在Master1中添加Slaver31为从节点

4、将Slaver31的复制源改为Master1

5、通过ping包将信息同步到集群的其他节点

###