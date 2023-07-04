
# 消费失败重试

## kafka
-   Kafka消费失败不支持重试
```java
 while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("record:\t"+record);
                System.out.println(record.value());
                System.out.println(record.key());
            }
        }
```
> Spring-Kafka 封装了消费重试和死信队列， 将正常情况下无法被消费的消息称为死信消息（Dead-Letter Message），将存储死信消息的特殊队列称为死信队列（Dead-Letter Queue）。

我们在应用中可以对死信队列中的消息进行监控重发，来使得消费者实例再次进行消费，消费端需要做幂等性的处理。
[Kafka 消费端消费重试和死信队列 - Java小强技术博客](https://www.javacui.com/tool/686.html)


## rocket

-   RocketMQ消费失败支持定时重试，每次重试间隔时间顺延
    [Consumption Retry | RocketMQ --- 消耗重试|火箭MQ](https://rocketmq.apache.org/docs/featureBehavior/10consumerretrypolicy)

消费失败，可以返回 `ConsumeConcurrentlyStatus.CONSUME_LATER;`,就可以重试

```java
 consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 启动消费者实例
        consumer.start();
```

消息重试
若Consumer消费某条消息失败，则RocketMQ会在重试间隔时间后，将消息重新投递给Consumer消费，若达到最大重试次数后消息还没有成功被消费，则消息将被投递至死信队列

**消息重试只针对集群消费模式生效**；广播消费模式不提供失败重试特性，即消费失败后，失败消息不再重试，继续消费新的消息