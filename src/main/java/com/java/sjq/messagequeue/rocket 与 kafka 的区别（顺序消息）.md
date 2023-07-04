## kafka
>### kafka的顺序消费
>     对于kafka来说，从整体架构上来讲，一般上是不支持顺序消费的。生产者可以选择不同的partition分区发送消息，消费者组可以消费不同partition分区的数据，这样就无法保证顺序性。如果必须要有一定的顺序，可以通过以下两种方式来解决！以订单消息为例
>
> - 订单topic下只设置一个partition分区，在创建一个消费者组，里边一个消费者去消费订单的partition分区，这样只有一个消费者消费一个分区，可以保证顺序性。缺点是效率低下，不符合kafka高效率的初衷。
> - 脱离kafka来实现消息顺序。因为kafka不怎么支持顺序消费，我们可以脱离kafka，在外部日志中根据订单的下单时间来做顺序，这也是常用的方式！


在发送一条消息时，可以通过指定producer的【ProducerConfig.PARTITIONER_CLASS_CONFIG】这一参数来指定，该class必须实现kafka.producer.Partitioner接口。如果key可以被解析为整数，则将对应的整数与partition总数取余，该消息会被发送到该数对应的partition。

### 生产者partition
```java

properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,"com.java.sjq.messagequeue.kafaka.multiConsumerGroup.MyPartitioner");


class MyPartitioner implements Partitioner{

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        return 0;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}

```
### 消费者

一次拉一条
```java
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

```

或者在代码中保证按照顺序消息集合的顺序消费。

## rocketmq
> [✅RocketMQ如何保证消息的顺序性？](https://www.yuque.com/hollis666/bfrl8w/nt1ishhbunfo0g86)
> 
### 生产者

和Kafka只支持同一个Partition内消息的顺序性一样，RocketMQ中也提供了基于队列(分区)的顺序消费。即同一个队列内的消息可以做到有序，但是不同队列内的消息是无序的！

当我们作为MQ的生产者需要发送顺序消息时，需要在send方法中，传入一个MessageQueueSelector。

MessageQueueSelector中需要实现一个select方法，这个方法就是用来定义要把消息发送到哪个MessageQueue的，通常可以使用取模法进行路由：

```java
SendResult sendResult = producer.send(msg, new MessageQueueSelector() {

@Override

//mqs：该Topic下所有可选的MessageQueue

//msg：待发送的消息

//arg：发送消息时传递的参数

public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {

Integer id = (Integer) arg;

//根据参数，计算出一个要接收消息的MessageQueue的下标

int index = id % mqs.size();

//返回这个MessageQueue

return mqs.get(index);

}

}, orderId);
```

通过以上形式就可以将需要有序的消息发送到同一个队列中。需要注意的时候，这里需要使用同步发送的方式！

### 消费者
消息按照顺序发送的消息队列中之后，那么，消费者如何按照发送顺序进行消费呢？

RockerMQ的MessageListener回调函数提供了两种消费模式，有序消费模式MessageListenerOrderly和并发消费模式MessageListenerConcurrently。所以，想要实现顺序消费，需要使用MessageListenerOrderly模式接收消息：

```java
consumer.registerMessageListener(new MessageListenerOrderly() {

Override

public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs ,ConsumeOrderlyContext context) {

System.out.printf("Receive order msg:" + new String(msgs.get(0).getBody()));

return ConsumeOrderlyStatus.SUCCESS ;

}

});
```

