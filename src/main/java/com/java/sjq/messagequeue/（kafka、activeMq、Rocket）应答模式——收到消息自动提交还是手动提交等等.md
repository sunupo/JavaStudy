## 消费者监听与生产者生产的先后顺序
### kafka
- consumer 先订阅，producer 再生产。
- consumer 后订阅，producer 先生产。

都能收到订阅前的消息
### rocket

### ActiveMQ
主题订阅模式下
- consumer 先订阅，producer 再生产。能收到所有生产消息
- consumer 后订阅，producer 先生产。不能收到订阅前的消息
点对点模式下
- consumer 先订阅，producer 再生产。
- consumer 后订阅，producer 先生产。

都能收到消息。但是一个 consumer 消费之后，另一个consumer 就不能消费了。

## 广播消费、集群消费
概念针对 consumergroup 来说的。
### kafka
默认集群消费。kafka的广播消费指的是单个consumergroup配置一个consumer来实现的。跟在一个consumergroup广播概念不一样，单结果可以一样。
kafka 广播消费和集群消费都是 broker 维护偏移量。因为广播消费的实现方式是通过每个consumergroup只设置一个consumer来实现的。

虽然 [为什么选择RocketMQ | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/#rocketmq-vs-activemq-vs-kafka) 写到 kafka 不能广播消费。指的应该是不能在一个 consumergroup 进行广播消费

### rocket
RocketMQ主要提供了两种消费模式：集群消费以及广播消费。我们只需要在定义消费者的时候通过`setMessageModel(MessageModel.XXX)`方法就可以指定是集群还是广播式消费，默认是集群消费模式，即每个Consumer Group中的Consumer均摊所有的消息。

### ActiveMQ
[为什么选择RocketMQ | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/#rocketmq-vs-activemq-vs-kafka)写到 ActiveMQ 支持广播消费。
应该指的就是 主题订阅模式下，每个consumer 都能消费到相同的消息。
