## kafka
[Kafka Producer 批量大小 - IT屋-程序员软件开发技术分享社区](https://www.it1352.com/2549053.html)
### 问题描述
- 当 `batch.size` 达到并且生产者应用线程发送更多数据时会发生什么?
- 线程是否会阻塞直到包含批处理的缓冲区中有可用空间?

### 推荐答案

- `batch.size` 以总字节数而不是消息数来衡量批大小.它控制在向 Kafka 代理发送消息之前要收集多少字节的数据.在不超过可用内存的情况下，将其设置得尽可能高.默认值为 16384.
- 当您使用 `Producer.send()` 时，您会填满生产者的缓冲区.当缓冲区已满时，生产者将缓冲区发送给 Kafka 代理并开始重新填充缓冲区.



## rocket

[批量消息发送 | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/producer/05message4)

这里调用非常简单，将消息打包成 Collection<Message> msgs 传入方法中即可，需要注意的是批量消息的大小不能超过 1MiB（否则需要自行分割），其次同一批 batch 中 topic 必须相同。

[(125条消息) RocketMQ批量发送消息4M限制解决方案-CSDN博客](https://blog.csdn.net/jy03133639/article/details/123608110)
超出大小业务方需要分割消息。

## 为什么kafka比RocketMQ吞吐量更高
> [rocketMq和kafka对比 - minch - 博客园](https://www.cnblogs.com/minch/p/17110619.html)

kafka性吞吐量更高主要是由于Producer端将多个小消息合并，批量发向Broker。kafka采用异步发送的机制，当发送一条消息时，消息并没有发送到broker而是缓存起来，然后直接向业务返回成功，当缓存的消息达到一定数量时再批量发送。

此时减少了网络io，从而提高了消息发送的性能，但是如果消息发送者宕机，会导致消息丢失，业务出错，所以理论上kafka利用此机制提高了io性能却降低了可靠性。