[(122条消息) Redis 集群中 分片 和 槽 的概念-CSDN博客](https://blog.csdn.net/sunqing0316/article/details/128080985?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-128080985-blog-116834531.pc_relevant_landingrelevant&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-128080985-blog-116834531.pc_relevant_landingrelevant&utm_relevant_index=1)

**分片**：[分片](https://so.csdn.net/so/search?q=%E5%88%86%E7%89%87&spm=1001.2101.3001.7020)数量就是集群中Redis节点的数量。理论上，单机可以通过设置不同端口实现集群，但在实际应用中，是一个物理机对应一个节点，以免物理机挂掉影响多个节点，造成损失扩大。当然了，如果将Redis集群部署到云上，那另当别论。

**槽**：slots是一个逻辑概念，属性是一个二进制位数组，数组长度为16384。2的14次幂，2048个字节。在没有明确指定到节点的时候（即 分配到不同的分片上），是没有具体的物理存储地址的。

关于分片，槽位既可以连续，也可以不连续，重要的是要保证数据分布均匀，请求命中均匀，避免数据倾斜。

重新分片，当新增或者缩减节点（分片）时，需要重新划分槽位，给槽位重新指派节点。


> [why redis-cluster use 16384 slots? · Issue #2576 · redis/redis · GitHub --- 为什么redis-cluster使用16384个插槽？·问题#2576 ·红宝石/红宝石· GitHub](https://github.com/redis/redis/issues/2576)
>
The reason is: 原因是：

1.  Normal heartbeat packets carry the full configuration of a node, that can be replaced in an idempotent way with the old in order to update an old config. This means they contain the slots configuration for a node, in raw form, that uses 2k of space with16k slots, but would use a prohibitive 8k of space using 65k slots.  
    正常的心跳包携带节点的完整配置，可以用旧配置以幂等的方式替换，以便更新旧配置。这意味着它们以原始形式包含节点的插槽配置，该配置使用2k空间和16k插槽，但使用65k插槽将使用禁止性的8k空间。
2.  At the same time it is unlikely that Redis Cluster would scale to more than 1000 mater nodes because of other design tradeoffs.  
    同时，由于其他设计上的权衡，Redis集群不太可能扩展到超过1000个主节点。

So 16k was in the right range to ensure enough slots per master with a max of 1000 maters, but a small enough number to propagate the slot configuration as a raw bitmap easily. Note that in small clusters the bitmap would be hard to compress because when N is small the bitmap would have slots/N bits set that is a large percentage of bits set.  
因此，16k在正确的范围内，以确保每个主机有足够的插槽，最多1000个主机，但这个数字足够小，可以轻松地将插槽配置作为原始位图传播。注意，在小集群中，位图将难以压缩，因为当N小时，位图将具有时隙/N比特集，这是比特集的大百分比。

总结： 
- 2KB 空间就可以表示 16k=16384 个槽。如果需要 65k=65536 个槽，那么需要 8KB空间。
- 同时，由于其他设计上的权衡，Redis集群不太可能扩展到超过1000个主节点
- 槽配置（slot configuration ）可以轻松通过bitmap传播（propagate）。当 N 小，slot/N比值大，导致小的集群（clusters）中位图（bitmap）难以压缩