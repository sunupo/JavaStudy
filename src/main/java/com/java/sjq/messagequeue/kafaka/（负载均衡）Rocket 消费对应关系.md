




> [(125条消息) RocketMQ-核心篇(2)-核心概念\_rocketmq一个消费者组只能消费一个topic吗\_TianXinCoord的博客-CSDN博客](https://blog.csdn.net/sinat_34104446/article/details/124784850)
> 
一个Topic类型的消息可以被多个消费者组同时消费
- 消费者组只能消费一个Topic的消息，不能同时消费多个Topic消息。(我的理解：在某个组上，可以取消订阅上一组订阅topic1消费者，然后在消费者组上重新关联一组订阅topic2的消费者。这是不是就实现了一个消费者组分时订阅多个topic)
- 一个消费者组中的消费者必须订阅完全相同的 Topic

一个消费者只对某种特定的topic感兴趣，即只可以订阅和消费一种topic。
一个分区（queue），只能由一个consumer消费；一个 consumer 可以消费多个分区

每个Message属于某个topic，message 使用唯一的 messageID 进行表示，同时消息可以带有标签 Tag 和 Key。
一个消费者组可以订阅多个Topic(对，官网有例子)