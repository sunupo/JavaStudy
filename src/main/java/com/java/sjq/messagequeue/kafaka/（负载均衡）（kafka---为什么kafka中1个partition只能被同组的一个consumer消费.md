# [（知识短文）kafka中partition和消费者对应关系 - 腾讯云开发者社区-腾讯云](https://cloud.tencent.com/developer/article/1513288)

> kafka 为了保证同一类型的消息顺序性（FIFO），
> - 一个partition只能被同一组的一个consumer消费，（组内多个consumer不能利用同一份partition）
> - 一个partition可以被不同组的consumer消费
> - 一个consumer可以消费多个partition
>简单来说  partition 可以被多组利用。 一个组内 partition 只能被一个 consumer 利用



## **消费者多于partition**

Topic： T1只有1个partition

Group: G1组中启动2个consumer

消费者数量为2大于partition数量1，此时partition和消费者进程对应关系如下：

![](https://ask.qcloudimg.com/http-save/yehe-1261315/3wg5qcsash.png?imageView2/2/w/2560/h/7000)

只有C1能接收到消息，C2则不能接收到消息，即同一个partition内的消息只能被同一个组中的一个consumer消费。当消费者数量多于partition的数量时，多余的消费者空闲。

也就是说如果只有一个partition你在同一组启动多少个consumer都没用，partition的数量决定了此topic在同一组中被可被均衡的程度，例如partition=4，则可在同一组中被最多4个consumer均衡消费。

## **消费者少于partition**

Topic：T2包含3个partition

Group: G2组中启动2个consumer

消费者数量为2小于partition数量3，此时partition和消费者进程对应关系如下：

![](https://ask.qcloudimg.com/http-save/yehe-1261315/b79fylokxx.png?imageView2/2/w/2560/h/7000)

此时P1、P2对应C1，即多个partition对应一个消费者，C1接收到消息量是C2的两倍

## **消费者等于partition**

Topic：T3包含3个partition

Group: G3组中启动3个consumer

消费者数量为3等于partition数量3，此时partition和消费者进程对应关系如下：

![](https://ask.qcloudimg.com/http-save/yehe-1261315/urkkrcj4ml.png?imageView2/2/w/2560/h/7000)

C1，C2，C3均分了T3的所有消息，即消息在同一个组之间的消费者之间均分了。

## **多个消费者组**

Topic：T3包含3个partition

Group: G3组中启动3个consumer，G4组中启动1个consumer

此时partition和消费者进程对应关系如下：

![](https://ask.qcloudimg.com/http-save/yehe-1261315/zu7chjl8ao.png?imageView2/2/w/2560/h/7000)

消息被G3组的消费者均分，G4组的消费者在接收到了所有的消息。启动多个组，则会使同一个消息被消费多次。

如果想要了解更多kafka内容，请参考：

[Kafka 架构-图文讲解](https://mp.weixin.qq.com/s?__biz=MzU5OTMyODAyNg==&mid=2247485393&idx=1&sn=de301a4991170555b2345aa8ffa9d121&chksm=feb7d3a4c9c05ab290ba84c14990932bfe716965788bb40c57e2e7c7956ee9bd4888671d7b0a&mpshare=1&scene=21&srcid=&sharer_sharetime=1569253741032&sharer_shareid=8c6df54816fbf82ec2ad71ea6388aad3&key=c9b773f393aeb8edc0ca0effc97f6942031ca3a4e4c82cfffd367ffd64c237501603774c42595d89542f4a51791d0778af1ce18e83f511c9a4d1d8ea6bc760683f070728019caa97edcd23ca09ac1392&ascene=1&uin=MTU0NTg1MjQw&devicetype=Windows%2010&version=62060841&lang=zh_CN&pass_ticket=2daIIS4eyDEOVn88dPy8mZL/Xv3EUD0/Vh%20r7JwNoCk=#wechat_redirect)

[Kafka 高性能吞吐揭秘](https://mp.weixin.qq.com/s?__biz=MzU5OTMyODAyNg==&mid=2247485381&idx=1&sn=f755be629ec92ffd9f0f71abec1e4d95&chksm=feb7d3b0c9c05aa6bb8e01aee1d40abddc9ad8a57dee93930b09e9e748450ad6d80dcd5935c8&mpshare=1&scene=21&srcid=&sharer_sharetime=1569253749355&sharer_shareid=8c6df54816fbf82ec2ad71ea6388aad3&key=f2f51bf817f177032e17d54316eb129e529a22150a6845b0da8648876199ef65b9066053b6ee9bdf36362fc59623f3c17d21f9bb238cb10bb2d6b80e611b4bb7f60a8c8c72dc3677f28d2cbe3c3ae682&ascene=1&uin=MTU0NTg1MjQw&devicetype=Windows%2010&version=62060841&lang=zh_CN&pass_ticket=2daIIS4eyDEOVn88dPy8mZL/Xv3EUD0/Vh%20r7JwNoCk=#wechat_redirect)

[Kafka最新面试题](https://mp.weixin.qq.com/s?__biz=MzU5OTMyODAyNg==&mid=2247485470&idx=1&sn=a0386d11f39acec5869d228623849905&chksm=feb7dc6bc9c0557d230ad30d58ba1881cbc7bd4c3ca5c80adcfda34f0b44472dcdbe0d2bca48&mpshare=1&scene=21&srcid=&sharer_sharetime=1569253729724&sharer_shareid=8c6df54816fbf82ec2ad71ea6388aad3&key=f05e901d119aa1e253365a01d504492cb5d8b26d2e65b79db558a6054acca23fcd3f9f90b3c5c3537299150492ca5d2cfe7d4e48dc577bbc0e53de9fd2b7cc34bb195142b9b2c98f846a845bae46d583&ascene=1&uin=MTU0NTg1MjQw&devicetype=Windows%2010&version=62060841&lang=zh_CN&pass_ticket=2daIIS4eyDEOVn88dPy8mZL/Xv3EUD0/Vh%20r7JwNoCk=#wechat_redirect)

# [(125条消息) kafka---为什么kafka中1个partition只能被同组的一个consumer消费?\_kafka多个消费者消费一个partition\_这代码有点上头的博客-CSDN博客](https://blog.csdn.net/weixin_47993432/article/details/128204985)

[Kafka](https://so.csdn.net/so/search?q=Kafka&spm=1001.2101.3001.7020)中每一个客户端的offset是由自己进行维护的，kafka并没有对同一个消费组中每个消费者的offset做中心化处理，所以如果他们消费同一个partition 都分别用自己的offset 会出现重复消费的问题。

## **offset是什么？**

**offset**

partition中的每条消息都被标记了一个序号，每个序号都是连续的，这个序号表示消息在partition中的偏移量，称为offset，每一条消息在partition都有唯一的offset。

offset从语义上来看有两种：Current offset 和 committed offset

### **Current offset**

-   Cuttent offset 保存在客户端中由客户端自己维护，它表示消费者希望收到下一条消息的序号，它仅仅在poll()方法中使用，例如：消费者第一次调用poll()方法收到了20条消息，那么 cuttent offset的值将被设置成20 下一次poll时，kafka就知道要从序号为21的消息开始读取，这样能保证消费者每次poll消息时，收到的消息不重复

### **Committed offset**

-   Committed Offset保存在Broker上 (V0.9之后的版本)，它表示**Consumer已经确认消费过的消息的序号**。主要通过`commitSync()`来操作。举例:Consumer通过poll()方法收到20条消息后，此时Current Offset就是20，经过一系列的逻辑处理后，并没有调用commitSync()来提交
-   Committed Offset，那么此时Committed Offset依旧是0。
-   Committed Offset主要用于Consumer Rebalance(再平衡)。在Consumer Rebalance的过程中，一个Partition被分配给了一个Consumer，那么这个Consumer该从什么位置开始消费消息呢?答案就是Committed Offset。另外，如果一个Consumer消费了5条消息.(poll并且成功commitSync)之后宕机了，重新启动之后，它仍然能够从第6条消息开始消费，因为Committed Offset已经被Kafka记录为5。
-   Committed Offset是为了每一个消费组进行记录的 不同的消费者组分别记录

## 小结：

- Current offset 是针对消费者 poll过程为了保证每次poll都返回不重复的消息

- Committed offset 是为了 Consumer Rebalance(再平衡) 的你过程，它能够保证同一个消费者组中新的消费者在正确的位置开始消费，避免重复消费。

同一消费组内多个消费者同时消费会出现重复消费。例如A1 1-10 POS 10

A2 10-20 POS 20

下次消费的时候 A1是 11-20重复消费

解决方案

-   假设broker对index进行维护。但是consumer 是进行pull操作的，拉取操作一般来说都是由拉取方提供index，数据方根据index 返回数据。如果由数据方维护index，会增加获取index 的通信开销。
-   假设consumer端进行多人的index维护，那么就得引入中心的概念，大家都在中心去获得当前的index，这会增加复杂性。
-   在consumer端维护单人index，可以方便的pull数据。牺牲了分区的分布式消费。由于可以有多个分区，权衡之下是较为合理的方案。
