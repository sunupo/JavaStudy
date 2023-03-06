[(122条消息) Redis集群 cluster-slot操作指令\_redis cluster slot\_梦想俱乐部的博客-CSDN博客](https://blog.csdn.net/weixin_49724150/article/details/115613564)

通过redis-cli 登录连接到redis-server，然后执行一下cluster命令进行操作  
一，redis cluster命令行

//集群(cluster)  
CLUSTER INFO 打印集群的信息  
CLUSTER NODES 列出集群当前已知的所有节点（[node](https://so.csdn.net/so/search?q=node&spm=1001.2101.3001.7020)），以及这些节点的相关信息。

//节点(node)  
CLUSTER MEET 将 ip 和 port 所指定的节点添加到集群当中，让它成为集群的一份子。  
CLUSTER FORGET <node\_id> 从集群中移除 node\_id 指定的节点。  
CLUSTER REPLICATE <node\_id> 将当前节点设置为 node\_id 指定的节点的从节点。  
CLUSTER SAVECONFIG 将节点的配置文件保存到硬盘里面。

//槽(slot)  
CLUSTER ADDSLOTS \[slot …\] 将一个或多个槽（slot）指派（assign）给当前节点。  
CLUSTER DELSLOTS \[slot …\] 移除一个或多个槽对当前节点的指派。  
CLUSTER FLUSHSLOTS 移除指派给当前节点的所有槽，让当前节点变成一个没有指派任何槽的节点。  
CLUSTER SETSLOT NODE <node\_id> 将槽 slot 指派给 node\_id 指定的节点，如果槽已经指派给另一个节点，那么先让另一个节点删除该槽>，然后再进行指派。  
CLUSTER SETSLOT MIGRATING <node\_id> 将本节点的槽 slot 迁移到 node\_id 指定的节点中。  
CLUSTER SETSLOT IMPORTING <node\_id> 从 node\_id 指定的节点中导入槽 slot 到本节点。  
CLUSTER SETSLOT STABLE 取消对槽 slot 的导入（import）或者迁移（migrate）。

//键 (key)  
CLUSTER KEYSLOT 计算键 key 应该被放置在哪个槽上。  
CLUSTER COUNTKEYSINSLOT 返回槽 slot 目前包含的键值对数量。  
CLUSTER GETKEYSINSLOT 返回 count 个 slot 槽中的键。  
这些命令是集群所独有的。执行上述命令要先登录

\[root@manage redis\]# redis-cli -c -p 6382 -h 192.168.10.220 //登录  
192.168.10.220:6382> cluster info //查看集群情况  
cluster\_state:ok  
cluster\_slots\_assigned:16384  
cluster\_slots\_ok:16384  
cluster\_slots\_pfail:0  
cluster\_slots\_fail:0  
cluster\_known\_nodes:6  
cluster\_size:3  
cluster\_current\_epoch:8  
cluster\_my\_epoch:4  
cluster\_stats\_messages\_sent:82753  
cluster\_stats\_messages\_received:82754

二，添加节点

1，新配置二个测试节点

## cd /etc/redis

//新增配置

## cp redis-6379.conf redis-6378.conf && sed -i “s/6379/6378/g” redis-6378.conf

## cp redis-6382.conf redis-6385.conf && sed -i “s/6382/6385/g” redis-6385.conf

//启动

## redis-server /etc/redis/redis-6385.conf > /var/log/redis/redis-6385.log 2>&1 &

## redis-server /etc/redis/redis-6378.conf > /var/log/redis/redis-6378.log 2>&1 &

2，添加主节点

## redis-trib.rb add-node 192.168.10.219:6378 192.168.10.219:6379

注释：  
192.168.10.219:6378是新增的节点  
192.168.10.219:6379集群任一个旧节点  
3，添加从节点

## redis-trib.rb add-node --slave --master-id 03ccad2ba5dd1e062464bc7590400441fafb63f2 192.168.10.220:6385 192.168.10.219:6379

注释：  
–slave，表示添加的是从节点  
–master-id 03ccad2ba5dd1e062464bc7590400441fafb63f2,主节点的node id，在这里是前面新添加的6378的node id  
192.168.10.220:6385,新节点  
192.168.10.219:6379集群任一个旧节点

4，重新分配slot

## redis-trib.rb reshard 192.168.10.219:6378 //下面是主要过程

How many slots do you want to move (from 1 to 16384)? 1000 //设置slot数1000  
What is the receiving node ID? 03ccad2ba5dd1e062464bc7590400441fafb63f2 //新节点node id  
Please enter all the source node IDs.  
Type ‘all’ to use all the nodes as source nodes for the hash slots.  
Type ‘done’ once you entered all the source nodes IDs.  
Source node #1:all //表示全部节点重新洗牌  
Do you want to proceed with the proposed reshard plan (yes/no)? yes //确认重新分

新增加的主节点，是没有slots的，

M: 03ccad2ba5dd1e062464bc7590400441fafb63f2 192.168.10.219:6378  
slots:0-332,5461-5794,10923-11255 (0 slots) master  
主节点如果没有slots的话，存取数据就都不会被选中。  
可以把分配的过程理解成打扑克牌，all表示大家重新洗牌；输入某个主节点的node id，然后在输入done的话，就好比从某个节点，抽牌。

5，查看一下，集群情况

\[root@slave2 redis\]# redis-trib.rb check 192.168.10.219:6379  
Connecting to node 192.168.10.219:6379: OK  
Connecting to node 192.168.10.220:6385: OK  
Connecting to node 192.168.10.219:6378: OK  
Connecting to node 192.168.10.220:6382: OK  
Connecting to node 192.168.10.220:6383: OK  
Connecting to node 192.168.10.219:6380: OK  
Connecting to node 192.168.10.219:6381: OK  
Connecting to node 192.168.10.220:6384: OK

> > > Performing Cluster Check (using node 192.168.10.219:6379)  
> > > M: 5d8ef5a7fbd72ac586bef04fa6de8a88c0671052 192.168.10.219:6379  
> > > slots:5795-10922 (5128 slots) master  
> > > 1 additional replica(s)  
> > > S: 9c240333476469e8e2c8e80b089c48f389827265 192.168.10.220:6385  
> > > slots: (0 slots) slave  
> > > replicates 03ccad2ba5dd1e062464bc7590400441fafb63f2  
> > > M: 03ccad2ba5dd1e062464bc7590400441fafb63f2 192.168.10.219:6378  
> > > slots:0-332,5461-5794,10923-11255 (1000 slots) master  
> > > 1 additional replica(s)  
> > > M: 19b042c17d2918fade18a4ad2efc75aa81fd2422 192.168.10.220:6382  
> > > slots:333-5460 (5128 slots) master  
> > > 1 additional replica(s)  
> > > M: b2c50113db7bd685e316a16b423c9b8abc3ba0b7 192.168.10.220:6383  
> > > slots:11256-16383 (5128 slots) master  
> > > 1 additional replica(s)  
> > > S: 6475e4c8b5e0c0ea27547ff7695d05e9af0c5ccb 192.168.10.219:6380  
> > > slots: (0 slots) slave  
> > > replicates 19b042c17d2918fade18a4ad2efc75aa81fd2422  
> > > S: 1ee01fe95bcfb688a50825d54248eea1e6133cdc 192.168.10.219:6381  
> > > slots: (0 slots) slave  
> > > replicates b2c50113db7bd685e316a16b423c9b8abc3ba0b7  
> > > S: 9a2a1d75b8eb47e05eee1198f81a9edd88db5aa1 192.168.10.220:6384  
> > > slots: (0 slots) slave  
> > > replicates 5d8ef5a7fbd72ac586bef04fa6de8a88c0671052  
> > > \[OK\] All nodes agree about slots configuration.
>
> > > Check for open slots…  
> > > Check slots coverage…  
> > > \[OK\] All 16384 slots covered.

三，改变从节点的master

//查看一下6378的从节点

## redis-cli -p 6378 cluster nodes | grep slave | grep 03ccad2ba5dd1e062464bc7590400441fafb63f2

//将6385加入到新的master

## redis-cli -c -p 6385 -h 192.168.10.220

192.168.10.220:6385> cluster replicate 5d8ef5a7fbd72ac586bef04fa6de8a88c0671052 //新master的node id  
OK  
192.168.10.220:6385> quit

//查看新master的slave

## redis-cli -p 6379 cluster nodes | grep slave | grep 5d8ef5a7fbd72ac586bef04fa6de8a88c0671052

四，删除节点

1,删除从节点

## redis-trib.rb del-node 192.168.10.220:6385 ‘9c240333476469e8e2c8e80b089c48f389827265’

2,删除主节点  
如果主节点有从节点，将从节点转移到其他主节点  
如果主节点有slot，去掉分配的slot，然后在删除主节点

## redis-trib.rb reshard 192.168.10.219:6378 //取消分配的slot,下面是主要过程

How many slots do you want to move (from 1 to 16384)? 1000 //被删除master的所有slot数量  
What is the receiving node ID? 5d8ef5a7fbd72ac586bef04fa6de8a88c0671052 //接收6378节点slot的master  
Please enter all the source node IDs.  
Type ‘all’ to use all the nodes as source nodes for the hash slots.  
Type ‘done’ once you entered all the source nodes IDs.  
Source node #1:03ccad2ba5dd1e062464bc7590400441fafb63f2 //被删除master的node-id  
Source node #2:done

Do you want to proceed with the proposed reshard plan (yes/no)? yes //取消slot后，reshard  
新增master节点后，也进行了这一步操作，当时是分配，现在去掉。反着的。

## redis-trib.rb del-node 192.168.10.219:6378 ‘03ccad2ba5dd1e062464bc7590400441fafb63f2’

新的master节点被删除了，这样就回到了，就是这篇文章开头，还没有添加节点的状态