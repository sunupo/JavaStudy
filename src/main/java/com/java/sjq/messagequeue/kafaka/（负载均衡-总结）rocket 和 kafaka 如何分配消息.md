# 集群模式下

[rocket 集群消费和广播消费](https://www.alibabacloud.com/help/zh/message-queue-for-apache-rocketmq/latest/clustering-consumption-and-broadcasting-consumption#section-ajk-82y-37z)

## rocket
> [订阅关系一致 | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/bestPractice/07subscribe)

### 分配策略

1. `queue个数大于Consumer个数， 那么Consumer会平均分配queue`。
2. `queue个数小于Consumer个数，那么会有Consumer闲置，就是浪费掉了，其余Consumer平均分配到queue上`。



### 如何消费

1. 一个queue只会被同一个group下的一个consumer消费
2. 一个queue 可以被其他组的 consumer消费。（不同组的consumerGroup的consumer端，可以消费相同的topic消息，且消费消息的顺序、msgId、offset、body等参数完全一致)
3. 每一个消费者都可以消费多个queue。

> - <s>消费者组只能消费一个Topic的消息，不能同时消费多个Topic消息。(我的理解：在某个组上，可以取消订阅上一组订阅topic1消费者，然后在消费者组上重新关联一组订阅topic2的消费者。这是不是就实现了一个消费者组分时订阅多个topic)</s>
> - 一个消费者组中的消费者必须订阅完全相同的 Topic
> - 答案来了,consumer可以订阅多个topic [订阅关系一致 | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/bestPractice/07subscribe#13-%E8%AE%A2%E9%98%85%E5%A4%9A%E4%B8%AAtopic%E4%B8%94%E8%AE%A2%E9%98%85%E5%A4%9A%E4%B8%AAtag)
>   - 如下图所示，同一Group ID下的三个Consumer实例C1、C2和C3分别都订阅了TopicA和TopicB，且订阅的TopicA都未指定Tag，即订阅TopicA中的所有消息，订阅的TopicB的Tag都是Tag2和Tag3，表示订阅TopicB中所有Tag为Tag2或Tag3的消息，且顺序一致都是Tag2||Tag3，符合订阅关系一致原则。
> ![1658454292557-c07fa0ac-81be-4aac-9c5b-342821c554a6](https://rocketmq.apache.org/zh/assets/images/4.x订阅关系一致-3-085b3104a879045cd04b876911351e21.jpeg)


## kafaka

### 分配策略

`1. partition 多，多个partition 分给同一个 consumer `
`2. partition  少， consumer 闲置`

### 如何消费

kafka 为了保证同一类型的消息顺序性（FIFO）

1. 一个partition只能被同一组的一个consumer消费，（组内多个consumer不能利用同一份partition）

2. 一个partition可以被不同组的consumer消费

3. 一个consumer可以消费多个partition

## 总结

上面这些情况 Kafaka 和 rocketMQ 一致

> [基础概念 | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/consumer/01concept2)

