[ActiveMQ queue和topic，持久订阅和非持久订阅](https://www.shuzhiduo.com/A/MAzA7q3o59/)


- 消息的 destination 分为 queue 和 topic，而消费者称为 subscriber（订阅者）。
- queue 中的消息只会发送给一个订阅者，而 topic 的消息，会发送给每一个订阅者。
- 在 broker 中，处理 queue 消息和 topic 消息的逻辑是不同的。
  - queue 先存储消息，然后把消息分发给消费者.
  - topic 收到消息的同时，就会分发。
  - 不论持久订阅还是非持久订阅，订阅者只能收到它订阅以后 生产者发送的消息。

- Queue 中有 doMessageSend 和 iterate 方法。
  - doMessageSend 负责接收生产者的消息
  - iterate 负责分发消息给消费者。
- Topic 中也有 doMessageSend 和 iterate 方法。
  - doMessageSend 负责接收生产者的消息，并且分发给消费者。
  - ```java
    // Topic.java
    synchronized void doMessageSend(final ProducerBrokerExchange producerExchange, final Message message){}
    public boolean iterate() {
        synchronized (messagesWaitingForSpace) {
            while (!memoryUsage.isFull() && !messagesWaitingForSpace.isEmpty()) {
                Runnable op = messagesWaitingForSpace.removeFirst();
                op.run();
            }

            if (!messagesWaitingForSpace.isEmpty()) {
                registerCallbackForNotFullNotification();
            }
        }
        return false;
    }
    ```

- queue 有持久和临时2种类型（topic相同）：  
  - 队列默认为持久队列，一旦创建，一直存在于broker中。
  - 而临时队列被创建后，在connection关闭后，broker就会删除它。

- topic 订阅有持久和非持久2种类型：  
  - broker 会把消息全部推送给持久订阅，即便该订阅者中途offline了.
  - 如果是非持久订阅，一旦它下线，broker 不会为它保留消息，直到它上线后，开始继续发送消息。

> 需要注意：假定有一个 topic，生产者向该 topic 发送一条消息，但此时该 topic 没有任何订阅者，则该消息不会保存，它会被删除。


- （创建持久订阅）代码示例：
  - 创建durable subscriber，需要指定唯一clientID

    ```java
    public static void main(String[] args) {
        //该连接上会创建durable subscriber，需要指定唯一clientID
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory("tcp://localhost:61616?jms.clientID=10086");
     
        ActiveMQConnection connection = (ActiveMQConnection)connectionFactory.createConnection();
        connection.start();
        ActiveMQSession session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建topic
        ActiveMQTopic destination = (ActiveMQTopic) session.createTopic("topic_zhang");
        //创建持久订阅者
        TopicSubscriber consumer = session.createDurableSubscriber(destination, "subscriber_zhang");
        //普通消费者，即非持久订阅者
        ActiveMQMessageConsumer consumer2 = (ActiveMQMessageConsumer) session.createConsumer(destination);
    }
    ```
