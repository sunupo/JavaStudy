# [Redis ---主从同步（replication）](https://redis.io/docs/management/replication/#how-redis-replication-works)



[(125条消息) redis源码阅读-主从复制增量复制细节\_redis增量复制\_5ycode的博客-CSDN博客](https://blog.csdn.net/f80407515/article/details/126539573)

是在master节点接收到psy[nc命令](https://so.csdn.net/so/search?q=nc命令&spm=1001.2101.3001.7020)的时候，这个命令是什么时候被调用的？是在slave周期性轮训的时候会调用（一般是连接以后，由状态控制）。

```c
//redis主从的关键是
replicaof masterIp masterPort
//这个可以启动一个redis执行，就变为了slave
```

[(125条消息) Redis replication\_buffer 与 replication\_backlog\_buffer 区别与联系 | 复制缓冲区、复制积压缓冲区\_replication buffer\_血煞长虹的博客-CSDN博客](https://blog.csdn.net/succing/article/details/121230604)

当master与所属slave首次建立连接后会进行全量rdb，后期（即使中间有断开连接，又重新恢复主从关系）优先使用增量同步数据，实在无法增量（比如：主的复制积压缓冲区爆满后，会重新落地rdb，然后主从就无法通过各自的offset偏移量计算出增量数据），才会走全量rdb。





## Redis 复制的工作原理

How Redis supports high availability and failover with replication  
Redis 如何通过复制支持高可用性和故障转移

At the base of Redis replication (excluding the high availability features provided as an additional layer by Redis Cluster or Redis Sentinel) there is a _leader follower_ (master-replica) replication that is simple to use and configure. It allows replica Redis instances to be exact copies of master instances. The replica will automatically reconnect to the master every time the link breaks, and will attempt to be an exact copy of it _regardless_ of what happens to the master.  
在 Redis 复制的基础（不包括 Redis 集群或 Redis Sentinel 作为附加层提供的高可用性功能）有一个易于使用和配置的领导者追随者（主副本）复制。它允许副本 Redis 实例是主实例的精确副本。每次链接断开时，副本都会自动重新连接到主副本，并且无论主节点发生什么情况，都会尝试成为它的精确副本。

This system works using three main mechanisms:  
该系统使用三种主要机制工作：

1.  When a master and a replica instances are well-connected, the master keeps the replica updated by sending a stream of commands to the replica to replicate the effects on the dataset happening in the master side due to: client writes, keys expired or evicted, any other action changing the master dataset.  
    当主实例和副本实例连接良好时，主实例通过向副本发送命令流来保持副本更新，以复制由于以下原因在主端对数据集产生的影响：客户端写入、密钥过期或逐出、任何其他更改主数据集的操作。
2.  When the link between the master and the replica breaks, for network issues or because a timeout is sensed in the master or the replica, the replica reconnects and attempts to proceed with a partial resynchronization: it means that it will try to just obtain the part of the stream of commands it missed during the disconnection.  
    当主副本和副本之间的链接中断时，由于网络问题或由于在主副本或副本中检测到超时，副本将重新连接并尝试继续部分重新同步：这意味着它将尝试仅获取在断开连接期间丢失的命令流的一部分。
3.  When a partial resynchronization is not possible, the replica will ask for a full resynchronization. This will involve a more complex process in which the master needs to create a snapshot of all its data, send it to the replica, and then continue sending the stream of commands as the dataset changes.  
    当无法进行部分重新同步时，副本将要求完全重新同步。这将涉及一个更复杂的过程，其中主节点需要创建其所有数据的快照，将其发送到副本，然后在数据集更改时继续发送命令流。

Redis uses by default asynchronous replication, which being low latency and high performance, is the natural replication mode for the vast majority of Redis use cases. However, Redis replicas asynchronously acknowledge the amount of data they received periodically with the master. So the master does not wait every time for a command to be processed by the replicas, however it knows, if needed, what replica already processed what command. This allows having optional synchronous replication.  
Redis 默认使用异步复制，即低延迟和高性能，是绝大多数 Redis 用例的自然复制模式。但是，Redis 副本会异步确认它们定期通过主数据库接收的数据量。因此，主节点不会每次等待副本处理命令，但是如果需要，它会知道哪个副本已经处理了什么命令。这允许具有可选的同步复制。

Synchronous replication of certain data can be requested by the clients using the [`WAIT`](https://redis.io/commands/wait) command. However [`WAIT`](https://redis.io/commands/wait) is only able to ensure there are the specified number of acknowledged copies in the other Redis instances, it does not turn a set of Redis instances into a CP system with strong consistency: acknowledged writes can still be lost during a failover, depending on the exact configuration of the Redis persistence. However with [`WAIT`](https://redis.io/commands/wait) the probability of losing a write after a failure event is greatly reduced to certain hard to trigger failure modes.  
客户端可以使用 `WAIT` 命令请求某些数据的同步复制。但是 `WAIT` 只能确保其他 Redis 实例中有指定数量的已确认副本，它不会将一组 Redis 实例转换为具有强一致性的 CP 系统：在故障转移期间，已确认的写入仍可能丢失，具体取决于 Redis 持久性的确切配置。但是，使用 `WAIT` 时，故障事件后丢失写入的可能性大大降低到某些难以触发的故障模式。

You can check the Redis Sentinel or Redis Cluster documentation for more information about high availability and failover. The rest of this document mainly describes the basic characteristics of Redis basic replication.  
您可以查看 Redis Sentinel 或 Redis 集群文档，了解有关高可用性和故障转移的更多信息。本文档的其余部分主要介绍 Redis 基本复制的基本特征。

### Important facts about Redis replication  
关于 Redis 复制的重要事实

-   Redis uses asynchronous replication, with asynchronous replica-to-master acknowledges of the amount of data processed.  
    Redis 使用异步复制，对处理的数据量进行异步副本到主数据库的确认。
-   A master can have multiple replicas.  
    一个主节点可以有多个副本。
-   Replicas are able to accept connections from other replicas. Aside from connecting a number of replicas to the same master, replicas can also be connected to other replicas in a cascading-like structure. Since Redis 4.0, all the sub-replicas will receive exactly the same replication stream from the master.  
    副本能够接受来自其他副本的连接。除了将多个副本连接到同一主节点外，副本还可以以类似级联的结构连接到其他副本。从 Redis 4.0 开始，所有子副本将从主副本接收完全相同的复制流。
-   Redis replication is non-blocking on the master side. This means that the master will continue to handle queries when one or more replicas perform the initial synchronization or a partial resynchronization.  
    Redis 复制在主端是非阻塞的。这意味着当一个或多个副本执行初始同步或部分重新同步时，主节点将继续处理查询。
-   Replication is also largely non-blocking on the replica side. While the replica is performing the initial synchronization, it can handle queries using the old version of the dataset, assuming you configured Redis to do so in redis.conf. Otherwise, you can configure Redis replicas to return an error to clients if the replication stream is down. However, after the initial sync, the old dataset must be deleted and the new one must be loaded. The replica will block incoming connections during this brief window (that can be as long as many seconds for very large datasets). Since Redis 4.0 you can configure Redis so that the deletion of the old data set happens in a different thread, however loading the new initial dataset will still happen in the main thread and block the replica.  
    复制在副本端也在很大程度上是非阻塞的。当副本执行初始同步时，它可以使用旧版本的数据集处理查询，假设您在 redis.conf 中将 Redis 配置为这样做。否则，您可以将 Redis 副本配置为在复制流关闭时向客户端返回错误。但是，在初始同步之后，必须删除旧数据集并加载新数据集。副本将在此短暂窗口期间阻止传入连接（对于非常大的数据集，可能长达几秒钟）。从 Redis 4.0 开始，您可以配置 Redis，以便在不同的线程中删除旧数据集，但是加载新的初始数据集仍将在主线程中发生并阻止副本。
-   Replication can be used both for scalability, to have multiple replicas for read-only queries (for example, slow O(N) operations can be offloaded to replicas), or simply for improving data safety and high availability.  
    复制既可用于可伸缩性，也可以用于为只读查询提供多个副本（例如，可以将慢速 O（N） 操作卸载到副本），或者仅用于提高数据安全性和高可用性。
-   You can use replication to avoid the cost of having the master writing the full dataset to disk: a typical technique involves configuring your master `redis.conf` to avoid persisting to disk at all, then connect a replica configured to save from time to time, or with AOF enabled. However, this setup must be handled with care, since a restarting master will start with an empty dataset: if the replica tries to sync with it, the replica will be emptied as well.  
    您可以使用复制来避免让主服务器将完整数据集写入磁盘的成本：典型的技术包括配置主 `redis.conf` 以避免完全保存到磁盘，然后连接配置为不时保存或启用 AOF 的副本。但是，必须小心处理此设置，因为重新启动主节点将从空数据集开始：如果副本尝试与其同步，副本也将清空。

## Safety of replication when master has persistence turned off  
关闭主服务器持久性时复制的安全性

In setups where Redis replication is used, it is strongly advised to have persistence turned on in the master and in the replicas. When this is not possible, for example because of latency concerns due to very slow disks, instances should be configured to **avoid restarting automatically** after a reboot.  
在使用 Redis 复制的设置中，强烈建议在主副本和副本中启用持久性。如果无法做到这一点，例如由于磁盘速度非常慢而导致的延迟问题，则应将实例配置为避免在重新启动后自动重新启动。

To better understand why masters with persistence turned off configured to auto restart are dangerous, check the following failure mode where data is wiped from the master and all its replicas:  
为了更好地理解为什么关闭持久性的主服务器配置为自动重新启动是危险的，请检查以下故障模式，在该模式下擦除数据：

1.  We have a setup with node A acting as master, with persistence turned down, and nodes B and C replicating from node A.  
    我们有一个设置，节点 A 充当主节点，持久性关闭，节点 B 和 C 从节点 A 复制。
2.  Node A crashes, however it has some auto-restart system, that restarts the process. However since persistence is turned off, the node restarts with an empty data set.  
    节点 A 崩溃，但它有一些自动重启系统，可以重新启动进程。但是，由于持久性已关闭，因此节点将使用空数据集重新启动。
3.  Nodes B and C will replicate from node A, which is empty, so they'll effectively destroy their copy of the data.  
    节点 B 和 C 将从节点 A 复制，节点 A 为空，因此它们将有效地销毁其数据副本。

When Redis Sentinel is used for high availability, also turning off persistence on the master, together with auto restart of the process, is dangerous. For example the master can restart fast enough for Sentinel to not detect a failure, so that the failure mode described above happens.  
当 Redis Sentinel 用于高可用性时，关闭主服务器上的持久性以及进程的自动重启也是危险的。例如，主站可以足够快地重新启动，以使 Sentinel 无法检测到故障，从而发生上述故障模式。

Every time data safety is important, and replication is used with master configured without persistence, auto restart of instances should be disabled.  
每当数据安全很重要，并且在没有持久性的情况下使用主配置的复制时，都应禁用实例的自动重启。

## How Redis replication works Redis 复制的工作原理

Every Redis master has a replication ID: it is a large pseudo random string that marks a given story of the dataset. Each master also takes an offset that increments for every byte of replication stream that it is produced to be sent to replicas, to update the state of the replicas with the new changes modifying the dataset. The replication offset is incremented even if no replica is actually connected, so basically every given pair of:  
每个 Redis 主节点都有一个复制 ID：它是一个大的伪随机字符串，用于标记数据集的给定故事。每个主节点还取一个偏移量，该偏移量为生成要发送到副本的每个复制流字节递增，以使用修改数据集的新更改更新副本的状态。即使没有实际连接副本，复制偏移量也会递增，因此基本上每个给定的复制对：

```
Replication ID, offset
```

Identifies an exact version of the dataset of a master.  
标识主数据集的确切版本。

When replicas connect to masters, they use the [`PSYNC`](https://redis.io/commands/psync) command to send their old master replication ID and the offsets they processed so far. This way the master can send just the incremental part needed. However if there is not enough _backlog_ in the master buffers, or if the replica is referring to an history (replication ID) which is no longer known, then a full resynchronization happens: in this case the replica will get a full copy of the dataset, from scratch.  
当副本连接到主副本时，它们使用 `PSYNC` 命令发送其旧的主复制 ID 以及到目前为止处理的偏移量。这样，主站可以只发送所需的增量部件。但是，如果主缓冲区中没有足够的积压工作，或者如果副本引用不再已知的历史记录（复制 ID），则会发生完全重新同步：在这种情况下，副本将从头开始获取数据集的完整副本。

This is how a full synchronization works in more details:  
以下是完全同步的更详细工作方式：

The master starts a background saving process to produce an RDB file. At the same time it starts to buffer all new write commands received from the clients. When the background saving is complete, the master transfers the database file to the replica, which saves it on disk, and then loads it into memory. The master will then send all buffered commands to the replica. This is done as a stream of commands and is in the same format of the Redis protocol itself.  
主服务器启动后台保存过程以生成 RDB 文件。同时，它开始缓冲从客户端接收的所有新写入命令。后台保存完成后，主服务器将数据库文件传输到副本，副本将其保存在磁盘上，然后将其加载到内存中。然后，主服务器会将所有缓冲的命令发送到副本。这是作为命令流完成的，并且格式与 Redis 协议本身相同。

You can try it yourself via telnet. Connect to the Redis port while the server is doing some work and issue the [`SYNC`](https://redis.io/commands/sync) command. You'll see a bulk transfer and then every command received by the master will be re-issued in the telnet session. Actually [`SYNC`](https://redis.io/commands/sync) is an old protocol no longer used by newer Redis instances, but is still there for backward compatibility: it does not allow partial resynchronizations, so now [`PSYNC`](https://redis.io/commands/psync) is used instead.  
您可以通过远程登录自行尝试。在服务器执行某些工作时连接到 Redis 端口并发出 `SYNC` 命令。您将看到批量传输，然后主服务器收到的每个命令都将在 telnet 会话中重新发出。实际上 `SYNC` 是一个不再被较新的 Redis 实例使用的旧协议，但仍然存在于向后兼容性：它不允许部分重新同步，所以现在使用 `PSYNC` 代替。

As already said, replicas are able to automatically reconnect when the master-replica link goes down for some reason. If the master receives multiple concurrent replica synchronization requests, it performs a single background save in to serve all of them.  
如前所述，当主-副本链接由于某种原因关闭时，副本能够自动重新连接。如果主服务器收到多个并发副本同步请求，它将执行单个后台保存以为所有请求提供服务。

## Replication ID explained 复制 ID 说明

In the previous section we said that if two instances have the same replication ID and replication offset, they have exactly the same data. However it is useful to understand what exactly is the replication ID, and why instances have actually two replication IDs: the main ID and the secondary ID.  
在上一节中，我们说过，如果两个实例具有相同的复制 ID 和复制偏移量，则它们具有完全相同的数据。但是，了解复制 ID 到底是什么以及为什么实例实际上有两个复制 ID：主 ID 和辅助 ID 很有用。

A replication ID basically marks a given _history_ of the data set. Every time an instance restarts from scratch as a master, or a replica is promoted to master, a new replication ID is generated for this instance. The replicas connected to a master will inherit its replication ID after the handshake. So two instances with the same ID are related by the fact that they hold the same data, but potentially at a different time. It is the offset that works as a logical time to understand, for a given history (replication ID), who holds the most updated data set.  
复制 ID 基本上标记数据集的给定历史记录。每次实例作为主实例从头开始重新启动，或者副本提升为主实例时，都会为此实例生成一个新的复制 ID。握手后，连接到主服务器的副本将继承其复制 ID。因此，具有相同 ID 的两个实例通过它们保存相同的数据（但可能位于不同的时间）而相关。对于给定的历史记录（复制 ID），偏移量可作为了解谁拥有最新数据集的逻辑时间。

For instance, if two instances A and B have the same replication ID, but one with offset 1000 and one with offset 1023, it means that the first lacks certain commands applied to the data set. It also means that A, by applying just a few commands, may reach exactly the same state of B.  
例如，如果两个实例 A 和 B 具有相同的复制 ID，但一个具有偏移量 1000，另一个具有偏移量 1023，则意味着第一个实例缺少应用于数据集的某些命令。这也意味着 A 只需应用几个命令，就可以达到与 B 完全相同的状态。

The reason why Redis instances have two replication IDs is because of replicas that are promoted to masters. After a failover, the promoted replica requires to still remember what was its past replication ID, because such replication ID was the one of the former master. In this way, when other replicas will sync with the new master, they will try to perform a partial resynchronization using the old master replication ID. This will work as expected, because when the replica is promoted to master it sets its secondary ID to its main ID, remembering what was the offset when this ID switch happened. Later it will select a new random replication ID, because a new history begins. When handling the new replicas connecting, the master will match their IDs and offsets both with the current ID and the secondary ID (up to a given offset, for safety). In short this means that after a failover, replicas connecting to the newly promoted master don't have to perform a full sync.  
Redis 实例具有两个复制 ID 的原因是副本已提升为主节点。故障转移后，提升的副本仍需要记住其过去的复制 ID 是什么，因为此类复制 ID 是以前的主复制 ID 之一。这样，当其他副本与新的主副本同步时，它们将尝试使用旧的主复制 ID 执行部分重新同步。这将按预期工作，因为当副本提升为主副本时，它会将其辅助 ID 设置为其主 ID，并记住此 ID 切换发生时的偏移量。稍后它将选择一个新的随机复制 ID，因为新的历史记录开始了。在处理连接的新副本时，主服务器会将其 ID 和偏移量与当前 ID 和辅助 ID 进行匹配（为了安全起见，最多为给定的偏移量）。简而言之，这意味着在故障转移后，连接到新提升的主节点的副本不必执行完全同步。

In case you wonder why a replica promoted to master needs to change its replication ID after a failover: it is possible that the old master is still working as a master because of some network partition: retaining the same replication ID would violate the fact that the same ID and same offset of any two random instances mean they have the same data set.  
如果您想知道为什么提升为主副本在故障转移后需要更改其复制 ID：由于某些网络分区，旧主副本可能仍作为主节点工作：保留相同的复制 ID 将违反以下事实：任何两个随机实例的相同 ID 和相同偏移量意味着它们具有相同的数据集。

## Diskless replication 无盘复制

Normally a full resynchronization requires creating an RDB file on disk, then reloading the same RDB from disk to feed the replicas with the data.  
通常，完全重新同步需要在磁盘上创建一个 RDB 文件，然后从磁盘重新加载相同的 RDB 以向副本提供数据。

With slow disks this can be a very stressing operation for the master. Redis version 2.8.18 is the first version to have support for diskless replication. In this setup the child process directly sends the RDB over the wire to replicas, without using the disk as intermediate storage.  
对于慢速磁盘，这对于主服务器来说可能是一个非常紧张的操作。Redis 版本 2.8.18 是第一个支持无盘复制的版本。在此设置中，子进程直接通过线路将 RDB 发送到副本，而不使用磁盘作为中间存储。

## Configuration 配置

To configure basic Redis replication is trivial: just add the following line to the replica configuration file:  
配置基本的 Redis 复制非常简单：只需将以下行添加到副本配置文件中：

```
replicaof 192.168.1.1 6379
```

Of course you need to replace 192.168.1.1 6379 with your master IP address (or hostname) and port. Alternatively, you can call the [`REPLICAOF`](https://redis.io/commands/replicaof) command and the master host will start a sync with the replica.

There are also a few parameters for tuning the replication backlog taken in memory by the master to perform the partial resynchronization. See the example `redis.conf` shipped with the Redis distribution for more information.

Diskless replication can be enabled using the `repl-diskless-sync` configuration parameter. The delay to start the transfer to wait for more replicas to arrive after the first one is controlled by the `repl-diskless-sync-delay` parameter. Please refer to the example `redis.conf` file in the Redis distribution for more details.

## Read-only replica

Since Redis 2.6, replicas support a read-only mode that is enabled by default. This behavior is controlled by the `replica-read-only` option in the redis.conf file, and can be enabled and disabled at runtime using [`CONFIG SET`](https://redis.io/commands/config-set).

Read-only replicas will reject all write commands, so that it is not possible to write to a replica because of a mistake. This does not mean that the feature is intended to expose a replica instance to the internet or more generally to a network where untrusted clients exist, because administrative commands like [`DEBUG`](https://redis.io/commands/debug) or [`CONFIG`](https://redis.io/commands/config) are still enabled. The [Security](https://redis.io/topics/security) page describes how to secure a Redis instance.

You may wonder why it is possible to revert the read-only setting and have replica instances that can be targeted by write operations. The answer is that writable replicas exist only for historical reasons. Using writable replicas can result in inconsistency between the master and the replica, so it is not recommended to use writable replicas. To understand in which situations this can be a problem, we need to understand how replication works. Changes on the master is replicated by propagating regular Redis commands to the replica. When a key expires on the master, this is propagated as a DEL command. If a key which exists on the master but is deleted, expired or has a different type on the replica compared to the master will react differently to commands like DEL, INCR or RPOP propagated from the master than intended. The propagated command may fail on the replica or result in a different outcome. To minimize the risks (if you insist on using writable replicas) we suggest you follow these recommendations:

-   Don't write to keys in a writable replica that are also used on the master. (This can be hard to guarantee if you don't have control over all the clients that write to the master.)
    
-   Don't configure an instance as a writable replica as an intermediary step when upgrading a set of instances in a running system. In general, don't configure an instance as a writable replica if it can ever be promoted to a master if you want to guarantee data consistency.
    

Historically, there were some use cases that were considered legitimate for writable replicas. As of version 7.0, these use cases are now all obsolete and the same can be achieved by other means. For example:

-   Computing slow Set or Sorted set operations and storing the result in temporary local keys using commands like [SUNIONSTORE](https://redis.io/commands/sunionstore) and [ZINTERSTORE](https://redis.io/commands/zinterstore). Instead, use commands that return the result without storing it, such as [SUNION](https://redis.io/commands/sunion) and [ZINTER](https://redis.io/commands/zinter).
    
-   Using the [SORT](https://redis.io/commands/sort) command (which is not considered a read-only command because of the optional STORE option and therefore cannot be used on a read-only replica). Instead, use [SORT\_RO](https://redis.io/commands/sort_ro), which is a read-only command.
    
-   Using [EVAL](https://redis.io/commands/eval) and [EVALSHA](https://redis.io/commands/evalsha) are also not considered read-only commands, because the Lua script may call write commands. Instead, use [EVAL\_RO](https://redis.io/commands/eval_ro) and [EVALSHA\_RO](https://redis.io/commands/evalsha_ro) where the Lua script can only call read-only commands.
    

While writes to a replica will be discarded if the replica and the master resync or if the replica is restarted, there is no guarantee that they will sync automatically.

Before version 4.0, writable replicas were incapable of expiring keys with a time to live set. This means that if you use [`EXPIRE`](https://redis.io/commands/expire) or other commands that set a maximum TTL for a key, the key will leak, and while you may no longer see it while accessing it with read commands, you will see it in the count of keys and it will still use memory. Redis 4.0 RC3 and greater versions are able to evict keys with TTL as masters do, with the exceptions of keys written in DB numbers greater than 63 (but by default Redis instances only have 16 databases). Note though that even in versions greater than 4.0, using [`EXPIRE`](https://redis.io/commands/expire) on a key that could ever exists on the master can cause inconsistency between the replica and the master.

Also note that since Redis 4.0 replica writes are only local, and are not propagated to sub-replicas attached to the instance. Sub-replicas instead will always receive the replication stream identical to the one sent by the top-level master to the intermediate replicas. So for example in the following setup:

```
A ---> B ---> C
```

Even if `B` is writable, C will not see `B` writes and will instead have identical dataset as the master instance `A`.

## Setting a replica to authenticate to a master

If your master has a password via `requirepass`, it's trivial to configure the replica to use that password in all sync operations.

To do it on a running instance, use `redis-cli` and type:

```
config set masterauth <password>
```

To set it permanently, add this to your config file:

```
masterauth <password>
```

## Allow writes only with N attached replicas

Starting with Redis 2.8, you can configure a Redis master to accept write queries only if at least N replicas are currently connected to the master.

However, because Redis uses asynchronous replication it is not possible to ensure the replica actually received a given write, so there is always a window for data loss.

This is how the feature works:

-   Redis replicas ping the master every second, acknowledging the amount of replication stream processed.
-   Redis masters will remember the last time it received a ping from every replica.
-   The user can configure a minimum number of replicas that have a lag not greater than a maximum number of seconds.

If there are at least N replicas, with a lag less than M seconds, then the write will be accepted.

You may think of it as a best effort data safety mechanism, where consistency is not ensured for a given write, but at least the time window for data loss is restricted to a given number of seconds. In general bound data loss is better than unbound one.

If the conditions are not met, the master will instead reply with an error and the write will not be accepted.

There are two configuration parameters for this feature:

-   min-replicas-to-write `<number of replicas>`
-   min-replicas-max-lag `<number of seconds>`

For more information, please check the example `redis.conf` file shipped with the Redis source distribution.

## How Redis replication deals with expires on keys

Redis expires allow keys to have a limited time to live (TTL). Such a feature depends on the ability of an instance to count the time, however Redis replicas correctly replicate keys with expires, even when such keys are altered using Lua scripts.

To implement such a feature Redis cannot rely on the ability of the master and replica to have synced clocks, since this is a problem that cannot be solved and would result in race conditions and diverging data sets, so Redis uses three main techniques to make the replication of expired keys able to work:

1.  Replicas don't expire keys, instead they wait for masters to expire the keys. When a master expires a key (or evict it because of LRU), it synthesizes a [`DEL`](https://redis.io/commands/del) command which is transmitted to all the replicas.
2.  However because of master-driven expire, sometimes replicas may still have in memory keys that are already logically expired, since the master was not able to provide the [`DEL`](https://redis.io/commands/del) command in time. To deal with that the replica uses its logical clock to report that a key does not exist **only for read operations** that don't violate the consistency of the data set (as new commands from the master will arrive). In this way replicas avoid reporting logically expired keys that are still existing. In practical terms, an HTML fragments cache that uses replicas to scale will avoid returning items that are already older than the desired time to live.
3.  During Lua scripts executions no key expiries are performed. As a Lua script runs, conceptually the time in the master is frozen, so that a given key will either exist or not for all the time the script runs. This prevents keys expiring in the middle of a script, and is needed to send the same script to the replica in a way that is guaranteed to have the same effects in the data set.

Once a replica is promoted to a master it will start to expire keys independently, and will not require any help from its old master.

## Configuring replication in Docker and NAT

When Docker, or other types of containers using port forwarding, or Network Address Translation is used, Redis replication needs some extra care, especially when using Redis Sentinel or other systems where the master [`INFO`](https://redis.io/commands/info) or [`ROLE`](https://redis.io/commands/role) commands output is scanned to discover replicas' addresses.

The problem is that the [`ROLE`](https://redis.io/commands/role) command, and the replication section of the [`INFO`](https://redis.io/commands/info) output, when issued into a master instance, will show replicas as having the IP address they use to connect to the master, which, in environments using NAT may be different compared to the logical address of the replica instance (the one that clients should use to connect to replicas).

Similarly the replicas will be listed with the listening port configured into `redis.conf`, that may be different from the forwarded port in case the port is remapped.

To fix both issues, it is possible, since Redis 3.2.2, to force a replica to announce an arbitrary pair of IP and port to the master. The two configurations directives to use are:

```
replica-announce-ip 5.5.5.5
replica-announce-port 1234
```

And are documented in the example `redis.conf` of recent Redis distributions.

## The INFO and ROLE command

There are two Redis commands that provide a lot of information on the current replication parameters of master and replica instances. One is [`INFO`](https://redis.io/commands/info). If the command is called with the `replication` argument as `INFO replication` only information relevant to the replication are displayed. Another more computer-friendly command is [`ROLE`](https://redis.io/commands/role), that provides the replication status of masters and replicas together with their replication offsets, list of connected replicas and so forth.

## Partial sync after restarts and failovers

Since Redis 4.0, when an instance is promoted to master after a failover, it will still be able to perform a partial resynchronization with the replicas of the old master. To do so, the replica remembers the old replication ID and offset of its former master, so can provide part of the backlog to the connecting replicas even if they ask for the old replication ID.

However the new replication ID of the promoted replica will be different, since it constitutes a different history of the data set. For example, the master can return available and can continue accepting writes for some time, so using the same replication ID in the promoted replica would violate the rule that a replication ID and offset pair identifies only a single data set.

Moreover, replicas - when powered off gently and restarted - are able to store in the `RDB` file the information needed to resync with their master. This is useful in case of upgrades. When this is needed, it is better to use the [`SHUTDOWN`](https://redis.io/commands/shutdown) command in order to perform a `save & quit` operation on the replica.

It is not possible to partially sync a replica that restarted via the AOF file. However the instance may be turned to RDB persistence before shutting down it, than can be restarted, and finally AOF can be enabled again.

## `Maxmemory` on replicas

By default, a replica will ignore `maxmemory` (unless it is promoted to master after a failover or manually). It means that the eviction of keys will be handled by the master, sending the DEL commands to the replica as keys evict in the master side.

This behavior ensures that masters and replicas stay consistent, which is usually what you want. However, if your replica is writable, or you want the replica to have a different memory setting, and you are sure all the writes performed to the replica are idempotent, then you may change this default (but be sure to understand what you are doing).

Note that since the replica by default does not evict, it may end up using more memory than what is set via `maxmemory` (since there are certain buffers that may be larger on the replica, or data structures may sometimes take more memory and so forth). Make sure you monitor your replicas, and make sure they have enough memory to never hit a real out-of-memory condition before the master hits the configured `maxmemory` setting.

To change this behavior, you can allow a replica to not ignore the `maxmemory`. The configuration directives to use is:

```
replica-ignore-maxmemory no
```