# 4.x
[Push消费 | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/consumer/02push)
[Pull消费 | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/consumer/03pull)

## push： consumer 监听 
```java
public class Consumer {
  public static void main(String[] args) throws InterruptedException, MQClientException {
    // 初始化consumer，并设置consumer group name
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name");
   
    // 设置NameServer地址 
    consumer.setNamesrvAddr("localhost:9876");
    //订阅一个或多个topic，并指定tag过滤条件，这里指定*表示接收所有tag的消息
    consumer.subscribe("TopicTest", "*");
    //注册回调接口来处理从Broker中收到的消息
    consumer.registerMessageListener(new MessageListenerConcurrently() {
      @Override
      public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
        // 返回消息消费状态，ConsumeConcurrentlyStatus.CONSUME_SUCCESS为消费成功
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
      }
    });
    // 启动Consumer
    consumer.start();
    System.out.printf("Consumer Started.%n");
  }
}
``` 

## pull：主动拉消息
```java
public class PullConsumerTest {
  public static void main(String[] args) throws MQClientException {
    DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("please_rename_unique_group_name_5");
    consumer.setNamesrvAddr("127.0.0.1:9876");
    consumer.start();
    try {
      MessageQueue mq = new MessageQueue();
      mq.setQueueId(0);
      mq.setTopic("TopicTest");
      mq.setBrokerName("jinrongtong-MacBook-Pro.local");
      long offset = 26;
      PullResult pullResult = consumer.pull(mq, "*", offset, 32);
      if (pullResult.getPullStatus().equals(PullStatus.FOUND)) {
        System.out.printf("%s%n", pullResult.getMsgFoundList());
        consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    consumer.shutdown();
  }
}
```

## Lite Pull Consumer
Lite Pull Consumer是`RocketMQ 4.6.0`推出的Pull Consumer，相比于原始的Pull Consumer更加简单易用，它提供了Subscribe和Assign两种模式，Subscribe模式示例如下

```java
public class LitePullConsumerSubscribe {
    public static volatile boolean running = true;
    public static void main(String[] args) throws Exception {
        DefaultLitePullConsumer litePullConsumer = new DefaultLitePullConsumer("lite_pull_consumer_test");
        litePullConsumer.subscribe("TopicTest", "*");
        litePullConsumer.setPullBatchSize(20);
        litePullConsumer.start();
        try {
            while (running) {
                List<MessageExt> messageExts = litePullConsumer.poll();
                System.out.printf("%s%n", messageExts);
            }
        } finally {
            litePullConsumer.shutdown();
        }
    }
}
```

首先还是初始化`DefaultLitePullConsumer`并设置`ConsumerGroupName`，调用subscribe方法订阅topic并启动。与Push Consumer不同的是，`LitePullConsumer`拉取消息调用的是轮询poll接口，如果能拉取到消息则返回对应的消息列表，否则返回null。通过`setPullBatchSize`可以设置每一次拉取的最大消息数量，此外如果不额外设置，`LitePullConsumer`默认是自动提交位点。在subscribe模式下，同一个消费组下的多个`LitePullConsumer`会负载均衡消费，与PushConsumer一致。

如下是Assign模式的示例

``` java
public class LitePullConsumerAssign {
    public static volatile boolean running = true;
    public static void main(String[] args) throws Exception {
        DefaultLitePullConsumer litePullConsumer = new DefaultLitePullConsumer("please_rename_unique_group_name");
        litePullConsumer.setAutoCommit(false);
        litePullConsumer.start();
        Collection<MessageQueue> mqSet = litePullConsumer.fetchMessageQueues("TopicTest");
        List<MessageQueue> list = new ArrayList<>(mqSet);
        List<MessageQueue> assignList = new ArrayList<>();
        for (int i = 0; i < list.size() / 2; i++) {
            assignList.add(list.get(i));
        }
        litePullConsumer.assign(assignList);
        litePullConsumer.seek(assignList.get(0), 10);
        try {
            while (running) {
                List<MessageExt> messageExts = litePullConsumer.poll();
                System.out.printf("%s %n", messageExts);
                litePullConsumer.commitSync();
            }
        } finally {
            litePullConsumer.shutdown();
        }
    }
}
```

Assign模式一开始仍然是初始化`DefaultLitePullConsumer`，这里我们采用手动提交位点的方式，因此设置AutoCommit为false，然后启动consumer。与Subscribe模式不同的是，Assign模式下没有自动的负载均衡机制，需要用户自行指定需要拉取的队列，因此在例子中，先用fetchMessageQueues获取了Topic下的队列，再取前面的一半队列进行拉取，示例中还调用了seek方法，将第一个队列拉取的位点设置从10开始。紧接着进入循环不停地调用poll方法拉取消息，拉取到消息后调用commitSync方法手动提交位点。


# 5.x
## 相比 4.x 使用方式不太一样
Apache RocketMQ 支持 PushConsumer 、 SimpleConsumer 以及 PullConsumer 这三种类型。 


[消费者分类 | RocketMQ](https://rocketmq.apache.org/zh/docs/featureBehavior/06consumertype#pushconsumer)

