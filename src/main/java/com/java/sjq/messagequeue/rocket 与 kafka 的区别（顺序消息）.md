# kafka
不支持分布式事务。支持单机事务：[Producer (kafka 3.4.0 API)](https://kafka.apache.org/34/javadoc/org/apache/kafka/clients/producer/Producer.html)

[(125条消息) Kafka事务消息详解\_wanger61的博客-CSDN博客](https://blog.csdn.net/wanger61/article/details/127205326)

## 一、[Kafka](https://so.csdn.net/so/search?q=Kafka&spm=1001.2101.3001.7020)的消息传输保障

一般而言，[消息中间件](https://so.csdn.net/so/search?q=%E6%B6%88%E6%81%AF%E4%B8%AD%E9%97%B4%E4%BB%B6&spm=1001.2101.3001.7020)的消息传输保障分为3个层级：

-   **at most once**：至多一次。消息可能丢失，但绝不会重复消费
-   **at least once**：最少一次。消息绝不丢失，但可能重复传输
-   **exactly once**：恰好一次。每条消息**肯定会被传输一次且仅传输一次**。

#### 1\. Kafka生产者消息保障

-   一旦消息被成功提交到日志文件，多副本机制会保障消息不丢失
-   对于网络问题，生产者也会通过重试机制来确保消息写入Kafka，但是重试过程中可能会导致消息的重复写入

因此，**Kafka生产者提供的消息保障为 at least once**

#### 2\. Kafka消费者消息保障

Kafka消费者消息保障主要**取决于消费者处理消息和提交消费位移的顺序**

如果消费者先处理消息后提交位移，那么如果在消息处理之后在位移提交之前消费者宕机了，那么重新上线后，会从上一次位移提交的位置拉取，这就导致了重复消息，对应 at least once

反过来，如果先提交位移后处理消息，就有可能会造成消息的丢失，对应 at most once

Kafka从0.11.0.0版本开始引入了**幂等**和**事务**这两个特性，以此来实现EOS（exactly once semantics）

## 二、Kafka[幂等](https://so.csdn.net/so/search?q=%E5%B9%82%E7%AD%89&spm=1001.2101.3001.7020)性

Kafka提供了幂等机制，只需显式地将生产者客户端参数 **enable.idempotence** 设置为 true即可（默认为false），开启后生产者就会幂等的发送消息

实现原理：

-   每个新的生产者实例在初始化时会被分配一个**PID（producer id）**
-   对于每个PID，消息发送到的每一个分区都有对应的**序列号**，序列号从0开始单调递增，生产者每发送一条消息就会**将<PID，分区>对应的序列号值加1**
-   broker端会在内存中为每一对<PID，分区>维护一个序列号，对于收到的每一条消息，**只有当它的序列号的值（SN\_new）正好比broker端中维护的对应序列号的值（SN\_old）大1，broker才会接收该消息**。如果 SN\_new < SN\_old + 1，说明消息被重复写入，broker会将该消息丢弃。否则，说明中间有数据尚未写入，暗示可能有消息丢失，对应生产者会抛出 OutOfOrderSequenceException 异常

注意：序列号实现幂等只是针对每一对<PID，分区>，即**Kafka的幂等性只能保证单个生产者会话（session）中单分区的幂等！！！！**

## 三、Kafka事务

通过事务可以弥补幂等性不能跨多个分区的缺陷，且可以**保证对多个分区写入操作的原子性**

在使用Kafka事务前，需要开启幂等特性，将 enable.idempotence 设置为 true

事务消息发送的示例如下：

```java
Properties properties = new Properties();
properties.put(org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionId);

KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

// 初始化事务
producer.initTransactions();
// 开启事务
producer.beginTransaction();

try {
     // 处理业务逻辑
     ProducerRecord<String, String> record1 = new ProducerRecord<String, String>(topic, "msg1");
     producer.send(record1);
     ProducerRecord<String, String> record2 = new ProducerRecord<String, String>(topic, "msg2");
     producer.send(record2);
     ProducerRecord<String, String> record3 = new ProducerRecord<String, String>(topic, "msg3");
     producer.send(record3);
     // 处理其他业务逻辑
     // 提交事务
     producer.commitTransaction();
} catch (ProducerFencedException e) {
 // 中止事务，类似于事务回滚
     producer.abortTransaction();
}
producer.close();
```

通过事务，在生产者角度，Kafka可以保证：

1.  **跨生产者会话的消息幂等发送**  
    transactionId与PID一一对应，如果新的生产者启动，**具有相同transactionId的旧生产者会立即失效**。（每个生产者通过 transactionId获取PID的同时，还会获取一个单调递增的 **producer epoch**）
2.  **跨生产者会话的事务恢复**  
    当某个生产者实例宕机后，新的生产者实例可以保证任何未完成的旧事物要么被提交（Commit），要么被中止（Abort），如此可以使新的生产者实例从一个正常的状态开始工作（通过 **producer epoch**判断）

在消费者角度，事务能保证的语义相对偏弱，对于一些特殊的情况，**Kafka并不能保证已提交的事务中的所有消息都能被消费**：

-   对采用日志压缩策略的主题，事务中的某些消息可能被清理（相同key的消息，后写入的消息会覆盖前面写入的消息）
-   事务中消息可能分布在同一个分区的多个日志分段（LogSegment）中，当老的日志分段被删除时，对应的消息可能会丢失
-   …

##### 事务隔离性

事务的隔离性通过设置消费端的参数 isolation.level 确定，默认值为 read\_uncommitted，即消费者可以消费到未提交的事务。该参数可以设置为 read\_commited，表示消费者不能消费到还未提交的事务

##### 事务手动提交

在一个事务中如果需要手动提交消息，需要先将 enable.auto.commit 参数设置为 false，然后调用 **sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId)** 方法进行手动提交，该方式特别适用于 **消费-转换-生产模式**的状况

示例代码如下:

```
producer.initTransactions();
        while (true){
            org.apache.kafka.clients.consumer.ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            if (!records.isEmpty()){
                Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                producer.beginTransaction();
                try {
                    for (TopicPartition partition: records.partitions()){
                        List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                        for (ConsumerRecord<String, String> record : partitionRecords) {
                            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("topic-sink", record.key(), record.value());
                            producer.send(producerRecord);
                        }
                        long lastConsumedOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                        offsets.put(partition, new OffsetAndMetadata(lastConsumedOffset + 1));
                    }
                    // 手动提交事务
                    producer.sendOffsetsToTransaction(offsets, "groupId");
                    producer.commitTransaction();
                }catch (ProducerFencedException e){
                    // log the exception
                    producer.abortTransaction();
                }
            }
       }
```

## 四、Kafka事务实现原理

为了实现事务，Kafka引入了**事务协调器（TransactionCoodinator）负责事务的处理**，**所有的事务逻辑包括分派PID等都是由TransactionCoodinator 负责实施的**。

### 内部主题 \_\_transaction\_state

broker节点有一个专门管理事务的**内部主题 \_\_transaction\_state**，TransactionCoodinator 会将事务状态持久化到该主题中

事务的整体流程如下：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/5b8e0f3f92254497a164ebf3fdb95541.jpeg)

1.  **查找 TransactionCoordinator**：生产者会先向某个broker发送 FindCoordinator 请求，找到 TransactionCoordinator 所在的 broker节点
2.  **获取PID**：生产者会向 TransactionCoordinator 申请获取 PID，TransactionCoordinator 收到请求后，会把 transactionalId 和对应的 PID 以消息的形式保存到主题 **\_\_transaction\_state** 中，保证 <transaction\_Id，PID>的对应关系被持久化，即使宕机该对应关系也不会丢失
3.  **开启事务**：调用 beginTransaction()后，生产者本地会标记开启了一个新事务
4.  **发送消息**：生产者向用户主题发送消息，过程跟普通消息相同，但第一次发送请求前会先发送请求给TransactionCoordinator 将 transactionalId 和 TopicPartition 的对应关系存储在 \_\_transaction\_state 中
5.  **提交或中止事务**：Kafka除了普通消息，还有专门的控制消息（ControlBatch）来标志一个事务的结束，控制消息有两种类型，分别用来表征事务的提交和中止  
    该阶段本质就是一个**两阶段**提交过程：
    1.  **将 PREPARE\_COMMIT 或 PREPARE\_ABORT 消息写入主题 \_\_transaction\_state**
    2.  **将COMMIT 或 ABORT 信息写入用户所使用的普通主题和 \_\_consumer\_offsets**
    3.  **将 COMPLETE\_COMMIT 或 COMPLETE\_COMMIT\_ABORT 消息写入主题 \_\_transaction\_state**

如此一来，表面当前事务已经结束，此时就可以删除主题 \_\_transaction\_state 中所有关于该事务的消息

# rocket

[事务消息发送 | RocketMQ](https://rocketmq.apache.org/zh/docs/4.x/producer/06message5)

> 事务消息的生产组名称 ProducerGroupName不能随意设置。事务消息有回查机制，回查时Broker端如果发现原始生产者已经崩溃，则会联系同一生产者组的其他生产者实例回查本地事务执行情况以Commit或Rollback半事务消息。

## 事务消息介绍

基于 RocketMQ 的**分布式事务消息**功能，在普通消息基础上，支持二阶段的提交能力。将二阶段提交和本地事务绑定，实现全局提交结果的一致性。

![事务消息1](assets/%E4%BA%8B%E5%8A%A1%E6%B6%88%E6%81%AF1-15b51f54e4cb4280459be1df277c288e.png)

事务消息发送分为两个阶段。

- 第一阶段会发送一个**半事务消息**，半事务消息是指暂不能投递的消息，生产者已经成功地将消息发送到了 Broker，但是Broker 未收到生产者对该消息的二次确认，此时该消息被标记成“暂不能投递”状态.
- 如果发送成功则执行本地事务，并根据本地事务执行成功与否，向 Broker 半事务消息状态（commit或者rollback），半事务消息只有 commit 状态才会真正向下游投递。
- 如果由于网络闪断、生产者应用重启等原因，导致某条事务消息的<u>二次确认丢失</u>，<u>Broker 端会通过扫描发现某条消息长期处于“半事务消息”时，需要主动向消息生产者询问该消息的最终状态（Commit或是Rollback）。</u>这样最终保证了本地事务执行成功，下游就能收到消息，本地事务执行失败，下游就收不到消息。总而保证了上下游数据的一致性。
- ![事务消息2](assets/%E4%BA%8B%E5%8A%A1%E6%B6%88%E6%81%AF2-2673a99678f13a471b8fc0bd4ab3bf3a-1678614831867-5.png)

## [事务消息步骤](https://rocketmq.apache.org/zh/docs/4.x/producer/06message5#事务消息步骤)

事务消息**发送**步骤如下：

1. 生产者将半事务消息发送至 `RocketMQ Broker`。
2. `RocketMQ Broker` 将消息持久化成功之后，向生产者返回 Ack 确认消息已经发送成功，此时消息暂不能投递，为半事务消息。
3. 生产者开始执行本地事务逻辑。
4. 生产者根据本地事务执行结果向服务端提交二次确认结果（Commit或是Rollback），服务端收到确认结果后处理逻辑如下：

- 二次确认结果为Commit：服务端将半事务消息标记为可投递，并投递给消费者。
- 二次确认结果为Rollback：服务端将回滚事务，不会将半事务消息投递给消费者。

1. 在断网或者是生产者应用重启的特殊情况下，若服务端未收到发送者提交的二次确认结果，或服务端收到的二次确认结果为Unknown未知状态，经过固定时间后，服务端将对消息生产者即生产者集群中任一生产者实例发起消息回查。
2. :::note 需要注意的是，服务端仅仅会按照参数尝试指定次数，超过次数后事务会强制回滚，因此未决事务的回查时效性非常关键，需要按照业务的实际风险来设置 :::

事务消息**回查**步骤如下： 7. 生产者收到消息回查后，需要检查对应消息的本地事务执行的最终结果。 8. 生产者根据检查得到的本地事务的最终状态再次提交二次确认，服务端仍按照步骤4对半事务消息进行处理

## 示例代码

```java
public class TransactionProducer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        TransactionListener transactionListener = new TransactionListenerImpl();
        TransactionMQProducer producer = new TransactionMQProducer("please_rename_unique_group_name");
        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("client-transaction-msg-check-thread");
                return thread;
            }
        });

        producer.setExecutorService(executorService);
        producer.setTransactionListener(transactionListener);
        producer.start();

        String[] tags = new String[] {"TagA", "TagB", "TagC", "TagD", "TagE"};
        for (int i = 0; i < 10; i++) {
            try {
                Message msg =
                    new Message("TopicTest", tags[i % tags.length], "KEY" + i,
                        ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                SendResult sendResult = producer.sendMessageInTransaction(msg, null);
                System.out.printf("%s%n", sendResult);

                Thread.sleep(10);
            } catch (MQClientException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 100000; i++) {
            Thread.sleep(1000);
        }
        producer.shutdown();
    }

    static class TransactionListenerImpl implements TransactionListener {
        private AtomicInteger transactionIndex = new AtomicInteger(0);

        private ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();

        @Override
        public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            int value = transactionIndex.getAndIncrement();
            int status = value % 3;
            localTrans.put(msg.getTransactionId(), status);
            return LocalTransactionState.UNKNOW;
        }

        @Override
        public LocalTransactionState checkLocalTransaction(MessageExt msg) {
            Integer status = localTrans.get(msg.getTransactionId());
            if (null != status) {
                switch (status) {
                    case 0:
                        return LocalTransactionState.UNKNOW;
                    case 1:
                        return LocalTransactionState.COMMIT_MESSAGE;
                    case 2:
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    default:
                        return LocalTransactionState.COMMIT_MESSAGE;
                }
            }
            return LocalTransactionState.COMMIT_MESSAGE;
        }
    }
}
```

事务消息的发送不再使用 DefaultMQProducer，而是使用 `TransactionMQProducer` 进行发送，上述的例子中设置了事务回查的线程池，如果不设置也会默认生成一个，最重要的是需要实现 `TransactionListener` 接口，并传入 `TransactionMQProducer`。

`executeLocalTransaction` 是半事务消息发送成功后，执行本地事务的方法，具体执行完本地事务后，可以在该方法中返回以下三种状态：

- `LocalTransactionState.COMMIT_MESSAGE`：提交事务，允许消费者消费该消息
- `LocalTransactionState.ROLLBACK_MESSAGE`：回滚事务，**消息将被丢弃不允许消费**。
- `LocalTransactionState.UNKNOW`：暂时无法判断状态，等待固定时间以后Broker端根据回查规则向生产者进行消息回查。

`checkLocalTransaction`是由于二次确认消息没有收到，Broker端回查事务状态的方法。回查规则：本地事务执行完成后，若Broker端收到的本地事务返回状态为LocalTransactionState.UNKNOW，或生产者应用退出导致本地事务未提交任何状态。则Broker端会向消息生产者发起事务回查，第一次回查后仍未获取到事务状态，则之后每隔一段时间会再次回查。

# rocket 与 kafka 幂等

[RocketMQ的消息幂等](https://www.ngui.cc/zz/2220631.html)

## rocket

### 以前：

官方开源的RocketMQ无法保证exactly once ,只能保证at least once。所以，使用RocketMQ时，需要由业务系统自行保证消息的幂等性。
处理方式
从上面的分析中，我们知道，在RocketMQ中，是无法保证每个消息只被投递一次的，所以要在业务上自行来保证消息消费的幂等性。

所以在一些对幂等性要求严格的场景，最好是使用业务上唯一的一个标识比较靠谱。例如订单ID。而这个业务标识可以使用Message的Key来进行传递。

不要使用MessageId来作为判断幂等的关键依据,这个MessageId是无法保证全局唯一的，也会有冲突的情况,另外在消息失败重试的时候,第二次发过来的消息和第一次发过来的消息的MessageId是不一样的.

### 现在

RocketMQ虽然之前也支持分布式事务，但并没有开源，等到RocketMQ 4.3才正式开源。