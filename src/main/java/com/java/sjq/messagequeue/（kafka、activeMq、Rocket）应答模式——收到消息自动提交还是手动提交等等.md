### kafka

> [kafka消费者的三种模式（最多/最少/恰好消费一次）](https://baijiahao.baidu.com/s?id=1647192820875012954&wfr=spider&for=pc)

#### 关键配置及含义

- enable.auto.commit 是否自动提交自己的offset值；默认值时true

- auto.commit.interval.ms 自动提交时长间隔；默认值时5000 ms

- consumer.commitSync(); offset提交命令；

#### 默认配置

采用默认配置情况下，既不能完全保证At-least-once 也不能完全保证at-most-once；

比如：

- 在自动提交之后，数据消费流程失败，这样就会有丢失，不能保证at-least-once；

- 数据消费成功，但是自动提交失败，可能会导致重复消费，这样也不能保证at-most-once；

- 但是将自动提交时长设置得足够小，则可以最大限度地保证at-most-once；

#### at most once模式

基本思想是保证每一条消息commit成功之后，再进行消费处理；

设置自动提交为false，接收到消息之后，首先commit，然后再进行消费

#### at least onece模式

基本思想是保证每一条消息处理成功之后，再进行commit；

设置自动提交为false；消息处理成功之后，手动进行commit；

采用这种模式时，最好保证消费操作的“幂等性”，防止重复消费；

#### exactly onece模式

核心思想是将offset作为唯一id与消息同时处理，并且保证处理的原子性；

设置自动提交为false；消息处理成功之后再提交；

**比如对于关系型数据库来说，可以将id设置为消息处理结果的唯一索引，再次处理时，如果发现该索引已经存在，那么就不处理；**

--------

> [kafka消费的三种模式](https://worktile.com/blog/know-681/)

kafka消费的三种模式主要是：1. 自动提交offset；2. 手动提交offset；3. 手动提交partition的offset。

**(应该是指三种提交模式，还有三种 once模式，。提交模式 和 once 模式有关联)**

### 一、 自动提交offset

Properties的实例props中存放的key意义：

1）  bootstrap.servers表示要连接的Kafka集群中的节点，其中9092表示端口号；

2）  enable.auto.commit为true，表示在auto.commit.interval.ms时间后会自动提交topic的offset，其中auto.commit.interval.ms默认值为5000ms；

3）  其中foo和bar为要消费的topic名称，由group.id为test作为consumer group统一进行管理；

4）  key.deserializer和value.deserializer表示指定将字节序列化为对象。

这种方式让消费者来管理位移，应用本身不需要显式操作。当我们将enable.auto.commit设置为true，那么消费者会在poll方法调用后每隔5秒（由auto.commit.interval.ms指定）提交一次位移。和很多其他操作一样，自动提交也是由poll()方法来驱动的；在调用poll()时，消费者判断是否到达提交时间，如果是则提交上一次poll返回的最大位移。

### 二、  手动提交offset

生产环境中，需要在数据消费完全后再提交offset，也就是说在数据从kafka的topic取出来后并被逻辑处理后，才算是数据被消费掉，此时需要手动去提交topic的offset。

本方案的缺点是必须保证所有数据被处理后，才提交topic的offset。为避免数据的重复消费，可以用第三种方案，根据每个partition的数据消费情况进行提交，称之为“at-least-once”。

为了减少消息重复消费或者避免消息丢失，很多应用选择自己主动提交位移。设置auto.commit.offset为false，那么应用需要自己通过调用commitSync()来主动提交位移，该方法会提交poll返回的最后位移。

> Kafka 提供了异步提交（commitAsync）及同步提交（commitSync）两种手动提交的方式。[两者的主要区别在于](https://blog.csdn.net/qq_35349490/article/details/79790625)：
>
> - 同步模式下提交失败时一直尝试提交，直到遇到无法重试的情况下才会结束，同时，同步方式下消费者线程在拉取消息时会被阻塞，直到偏移量提交操作成功或者在提交过程中发生错误。
> - 而异步方式下消费者线程不会被阻塞，可能在提交偏移量操作的结果还未返回时就开始进行下一次的拉取操作，在提交失败时也不会尝试提交。

### 三、  手动提交partition的offset

手动提交有一个缺点，那就是当发起提交调用时应用会阻塞。当然我们可以减少手动提交的频率，但这个会增加消息重复的概率（和自动提交一样）。另外一个解决办法是，使用异步提交的API。以下为使用异步提交的方式，应用发了一个提交请求然后立即返回：但是异步提交也有个缺点，那就是如果服务器返回提交失败，异步提交不会进行重试。

--------

### rocket


### activeMQ
```java
    static final int AUTO_ACKNOWLEDGE = 1;
    static final int CLIENT_ACKNOWLEDGE = 2;
    static final int DUPS_OK_ACKNOWLEDGE = 3;
    static final int SESSION_TRANSACTED = 0;
```
```java
    public static final int INDIVIDUAL_ACKNOWLEDGE = 4;
```

#### Auto_ack
异常或重试指定次数。

#### Client_ack
- 手动回复ack，相当于确认前面所有的消息都正常收到了（如果前面有一条消息处理异常了，但是在接下的消息处理中还是回复了ack，那么这个错误的消息activemq服务器也不会再次下发了。因为业务端易用已经手动确认过了，业务自己背锅。）。 
- 相当于 TCP 的延迟确认合并ACK（接收端不必每个包都回应ACK，只需要回复一个ack就表示这个ack号以前的都被正确接受了）。
- 业务异常后就不要提交ACK了。针对后面消息的业务需要主动卡主。

#### dups
自动批量，类似于 
#### session
业务成功需要session.commit 提交。业务失败需要session.rollback回滚。
#### INDIVIDUAL_ACKNOWLEDGE
ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE 
针对单条消息，失败自动重试，不会马上继续执行下一条消息。等到达最大重试次数就扔到死信队列，然后继续执行下一条消息。