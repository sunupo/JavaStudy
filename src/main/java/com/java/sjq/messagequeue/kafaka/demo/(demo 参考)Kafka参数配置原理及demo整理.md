[(125条消息) Kafka参数配置原理及demo整理\_kafkademo\_海底吐泡泡的鱼的博客-CSDN博客](https://blog.csdn.net/qq_45505510/article/details/120880311)


提示：文章写完后，目录可以自动生成，如何生成可参考右边的帮助文

**目录**

[文章目录](https://blog.csdn.net/qq_45505510/article/details/120880311#t0)

[前言](https://blog.csdn.net/qq_45505510/article/details/120880311#t1)

[一、kafka](https://blog.csdn.net/qq_45505510/article/details/120880311#t2)定义

[二、kafka安装和配置](https://blog.csdn.net/qq_45505510/article/details/120880311#t3)

[1.jdk环境](https://blog.csdn.net/qq_45505510/article/details/120880311#t4)

[2.zookeeper安装](https://blog.csdn.net/qq_45505510/article/details/120880311#t5)

[3.kafka安装](https://blog.csdn.net/qq_45505510/article/details/120880311#t6)

[三、kafka入门案例](https://blog.csdn.net/qq_45505510/article/details/120880311#t7)

[1.3.1 创建工程kafka-demo](https://blog.csdn.net/qq_45505510/article/details/120880311#t8)

[1.3.2 消息生产者](https://blog.csdn.net/qq_45505510/article/details/120880311#t9)

[1.3.3 消息消费者](https://blog.csdn.net/qq_45505510/article/details/120880311#t10)

[1.3.4 测试及结论](https://blog.csdn.net/qq_45505510/article/details/120880311#t11)

[1.3.5 相关概念详解](https://blog.csdn.net/qq_45505510/article/details/120880311#t12)

[1.3.5 生产者详解](https://blog.csdn.net/qq_45505510/article/details/120880311#t13)

[1.3.6 消费者详解](https://blog.csdn.net/qq_45505510/article/details/120880311#t14)

[1.4 spring boot集成kafka收发消息](https://blog.csdn.net/qq_45505510/article/details/120880311#t15)

[1.4.1 环境搭建](https://blog.csdn.net/qq_45505510/article/details/120880311#t16)

[1.4.3 消息消费者](https://blog.csdn.net/qq_45505510/article/details/120880311#t17)

[1.5 传递消息为对象](https://blog.csdn.net/qq_45505510/article/details/120880311#t18)

[附录一](https://blog.csdn.net/qq_45505510/article/details/120880311#t19)

[kafka生产者发送消息成功回调](https://blog.csdn.net/qq_45505510/article/details/120880311#t20)

[后面再更新集群和高级应用吧](https://blog.csdn.net/qq_45505510/article/details/120880311#t21)

___

### 一、[kafka](https://so.csdn.net/so/search?q=kafka&spm=1001.2101.3001.7020)定义

Kafka传统定义：是一个分布式的基于发布/订阅模式的消息队列（Message Queue） 

                         传统的消息队列主要应用场景也就是**缓存/消峰**、**解耦**和**异步通信** 

kafka最新定义：开源的分布式事件流平台（Event Streaming Platform）也可用于高性能数据管道、流分析、数据集成和关键任务的应用

kafka官网：http://kafka.apache.org/

![](https://img-blog.csdnimg.cn/0368a2388c6942799ce2ccdae0c67faa.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_14,color_FFFFFF,t_70,g_se,x_16)

kafka名词解释

-   topic：Kafka将消息分门别类，每一类的消息称之为一个主题（Topic）
-   producer：发布消息的对象称之为主题生产者（Kafka topic producer）
-   consumer：订阅消息并处理发布的消息的对象称之为主题消费者（consumers）
-   broker：已发布的消息保存在一组服务器中，称之为Kafka集群。集群中的每一个服务器都是一个代理（Broker）。 消费者可以订阅一个或多个主题（topic），并从Broker拉数据，从而消费这些已发布的消息。

## 二、kafka安装和配置

## 1.jdk环境

首先需要安装Java环境，同时配置环境变量

## 2.[zookeeper安装](https://so.csdn.net/so/search?q=zookeeper%E5%AE%89%E8%A3%85&spm=1001.2101.3001.7020)

Zookeeper是安装Kafka集群的必要组件，Kafka通过Zookeeper来实施对元数据信息的管理，包括集群、主题、分区等内容。


同样在官网下载安装包到指定目录解压缩

ZooKeeper 官网： [Apache ZooKeeper](http://zookeeper.apache.org/ "Apache ZooKeeper")

下面以一个zookeeper-3.4.14.tar.gz安装包为例，上传到linux服务器，也可以到官网上下载

（1）解压压缩包

```
tar zxvf zookeeper-3.4.14.tar.gz
```

（2）修改配置文件，进入安装路径conf目录，并将zoo\_sample.cfg文件修改为zoo.cfg

```
cd zookeeper-3.4.14  cd conf   mv zoo_sample.cfg zoo.cfg  
```

![](https://img-blog.csdnimg.cn/271ba4ea00ed4079aeac64c1447d6929.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_10,color_FFFFFF,t_70,g_se,x_16)

（3）创建存放数据的目录 data

在zookeeper安装的根目录创建目录 data

```
mkdir data 
```

创建完的效果如下：

![](https://img-blog.csdnimg.cn/8d720be1e03b46ba85efdd58ad8d6ec6.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_8,color_FFFFFF,t_70,g_se,x_16)

（4）配置数据存储目录

进入conf目录下，编辑zoo.cfg

```
vi conf/zoo.cfg
```

修改内容，如下图

![](https://img-blog.csdnimg.cn/2b59e1a409314af3ae1b7ad349f8a6f8.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_15,color_FFFFFF,t_70,g_se,x_16)

（5）启动zookeeper

进入bin目录

```
./zkServer.sh start ./zkServer.sh status ./zkServer.sh restart ./zkServer.sh stop 
```

启动后可以查看进行

```
jps
```

![](https://img-blog.csdnimg.cn/2723f6e7ea3e4258b5031c69b5726428.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_12,color_FFFFFF,t_70,g_se,x_16)

## 3.kafka安装

（1）官网下载

下载地址：http://kafka.apache.org/downloads

![](https://img-blog.csdnimg.cn/db664a9726ce466a889c9168a13c8e42.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_15,color_FFFFFF,t_70,g_se,x_16)

也可以在今天的资源文件夹中找到这个安装，直接上传到服务器即可

（2）解压

```
tar zxvf kafka_2.12-2.2.1.tgz
```

（3）修改参数

修改config目录下的server.properties文件,效果如下

-   修改listeners=PLAINTEXT://host:9092
-   log.dirs=/root/kafka\_2.12-2.2.1/logs 需要在kafka安装目录新建logs目录

![](https://img-blog.csdnimg.cn/4bfb5274b6ef469fba14aefc15f487a0.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_17,color_FFFFFF,t_70,g_se,x_16)

（4）启动kafka

在kafka的根目录

```
bin/kafka-server-start.sh config/server.properties  #启动kafka
```

查看进程

![](https://img-blog.csdnimg.cn/8747e8eb617c45f08df79fc84988a50a.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_9,color_FFFFFF,t_70,g_se,x_16)

注意：启动kafka之前，必须先启动zookeeper

##  三、kafka入门案例

## 1.3.1 创建工程kafka-demo

创建kafka-demo工程，引入依赖信息

```
<properties>    <kafka.client.version>2.0.1</kafka.client.version></properties><dependencies>    <dependency>        <groupId>org.apache.kafka</groupId>        <artifactId>kafka-clients</artifactId>        <version>${kafka.client.version}</version>    </dependency></dependencies>
```

做一个java普通的生产者和消费者只需要依赖`kafka-clients`即可

## 1.3.2 消息生产者

创建类：

```
package com.kafka.simple;import org.apache.kafka.clients.producer.KafkaProducer;import org.apache.kafka.clients.producer.ProducerConfig;import org.apache.kafka.clients.producer.ProducerRecord;import org.apache.kafka.common.protocol.types.Field;import java.util.Properties;public class ProducerFastStart {private static final String TOPIC = "itcast-heima";public static void main(String[] args) {Properties properties = new Properties();        properties.put("bootstrap.servers","192.168.200.130:9092");        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");        properties.put(ProducerConfig.RETRIES_CONFIG,10);        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);        ProducerRecord<String,String> record = new ProducerRecord<String, String>(TOPIC,"00001","hello kafka !");try {            producer.send(record);        }catch (Exception e){            e.printStackTrace();        }        producer.close();    }}
```

## 1.3.3 消息消费者

创建消费者类：

```
package com.kafka.simple;import org.apache.kafka.clients.consumer.ConsumerConfig;import org.apache.kafka.clients.consumer.ConsumerRecord;import org.apache.kafka.clients.consumer.ConsumerRecords;import org.apache.kafka.clients.consumer.KafkaConsumer;import java.time.Duration;import java.util.Collections;import java.util.Properties;public class ConsumerFastStart {private static final String TOPIC = "itcast-heima";public static void main(String[] args) {Properties properties = new Properties();        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.200.130:9092");        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"group2");        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);        consumer.subscribe(Collections.singletonList(TOPIC));while (true){            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));for (ConsumerRecord<String, String> record : records) {                System.out.println(record.value());                System.out.println(record.key());            }        }    }}
```

## 1.3.4 测试及结论

-   生产者发送消息，同一个组中的多个消费者只能有一个消费者接收消息
-   生产者发送消息，如果有多个组，每个组中只能有一个消费者接收消息,如果想要实现广播的效果，可以让每个消费者单独有一个组即可，这样每个消费者都可以接收到消息

## 1.3.5 相关概念详解

![](https://img-blog.csdnimg.cn/8dcec74c471246ee836d701b48632f6b.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_17,color_FFFFFF,t_70,g_se,x_16)

在kafka概述里介绍了概念包括：topic、producer、consumer、broker，这些是最基本的一些概念，想要更深入理解kafka还要知道它的一些其他概念定义：

-   **消息Message**

Kafka 中的数据单元被称为消息message，也被称为记录，可以把它看作数据库表中某一行的记录。

-   **topic**

Kafka将消息分门别类，每一类的消息称之为一个主题（Topic）

-   **批次**

为了提高效率， 消息会分批次写入 Kafka，批次就代指的是一组消息。

-   **分区Partition**

主题可以被分为若干个分区（partition），同一个主题中的分区可以不在一个机器上，有可能会部署在多个机器上，由此来实现 kafka 的伸缩性。topic中的数据分割为一个或多个partition。每个topic至少有一个partition。每个partition中的数据使用多个文件进行存储。partition中的数据是有序的，partition之间的数据是没有顺序的。如果topic有多个partition，消费数据时就不能保证数据的顺序。在需要严格保证消息的消费顺序的场景下，需要将partition数目设为1。

-   **broker**

一个独立的 Kafka 服务器就被称为 broker，broker 接收来自生产者的消息，为消息设置偏移量，并提交消息到磁盘保存。

-   **Broker 集群**

Kafka 集群包含一个或多个服务器，服务器节点称为broker 。 broker 存储 topic 的数据。如果某 topic 有 N个 partition ，集群有 N 个 broker ，那么每个 broker 存储该 topic 的一个partition 。如果某 topic 有 N 个 partition，集群有 (N+M) 个 broker ，那么其中有 N 个 broker 存储该 topic 的一个partition ，剩下的 M 个 broker不存储该 topic 的 partition 数据。如果某 topic 有 N 个 partition ，集群中 broker 数目少于 N 个，那么 一个broker 存储该 topic 的一个或多个partition 。在实际生产环境中，尽量避免这种情况的发生，这种情况容易导致Kafka 集群数据不均衡。

-   **副本Replica**

Kafka 中消息的备份又叫做 副本（Replica），副本的数量是可以配置的，Kafka 定义了两类副本：领导者副本（Leader Replica） 和 追随者副本（Follower Replica）；所有写请求都通过Leader路由，数据变更会广播给所有Follower，Follower与Leader保持数据同步。如果Leader失效，则从Follower中选举出一个新的Leader。当Follower与Leader挂掉、卡住或者同步太慢，leader会把这个follower从ISR列表（保持同步的副本列表）中删除，重新创建一个Follower。

-   **Zookeeper**

kafka对与zookeeper是强依赖的，是以zookeeper作为基础的，即使不做集群，也需要zk的支持。Kafka通过Zookeeper管理集群配置，选举leader，以及在Consumer Group发生变化时进行重平衡。生产者和消费者依据Zookeeper的broker状态信息与broker协调数据的发布和订阅任务。

-   **消费者群组Consumer Group**

生产者与消费者的关系就如同餐厅中的厨师和顾客之间的关系一样，一个厨师对应多个顾客，也就是一个生产者对应多个消费者，消费者群组（Consumer Group）指的就是由一个或多个消费者组成的群体。

-   **偏移量Consumer Offset**

偏移量（Consumer Offset）是一种元数据，它是一个不断递增的整数值，用来记录消费者发生重平衡时的位置，以便用来恢复数据。

-   **重平衡Rebalance**

消费者组内某个消费者实例挂掉后，其他消费者实例自动重新分配订阅主题分区的过程。Rebalance 是 Kafka 消费者端实现高可用的重要手段。

## 1.3.5 生产者详解

**（1）发送消息的工作原理**

![](https://img-blog.csdnimg.cn/0e2817c8b52543ae8d2e80fcc1e9a6f7.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_20,color_FFFFFF,t_70,g_se,x_16)

**（2）发送类型**

-   **发送并忘记（fire-and-forget）**

把消息发送给服务器，并不关心它是否正常到达，大多数情况下，消息会正常到达，因为kafka是高可用的，而且生产者会自动尝试重发，使用这种方式有时候会丢失一些信息

-   **同步发送**

使用send()方法发送，它会返回一个Future对象，调用get()方法进行等待，就可以知道消息是否发送成功

```
try {RecordMetadata recordMetadata = producer.send(record).get();      System.out.println(recordMetadata.offset());  }catch (Exception e){      e.printStackTrace();  }
```

如果服务器返回错误，get()方法会抛出异常，如果没有发生错误，我们就会得到一个RecordMetadata对象，可以用它来获取消息的偏移量

-   **异步发送**

调用send()方法，并指定一个回调函数，服务器在返回响应时调用函数。如下代码

```
try {      producer.send(record, new Callback() {@Overridepublic void onCompletion(RecordMetadata recordMetadata, Exception e) {if(e!=null){                  e.printStackTrace();              }              System.out.println(recordMetadata.offset());          }      });  }catch (Exception e){      e.printStackTrace();  }
```

如果kafka返回一个错误，onCompletion()方法会抛出一个非空（non null）异常，可以根据实际情况处理，比如记录错误日志，或者把消息写入“错误消息”文件中，方便后期进行分析。

**（3）参数详解**

到目前为止，我们只介绍了生产者的几个必要参数（bootstrap.servers、序列化器等）

生产者还有很多可配置的参数，在kafka官方文档中都有说明，大部分都有合理的默认值，所以没有必要去修改它们，不过有几个参数在内存使用，性能和可靠性方法对生产者有影响

-   acks：指的是producer的消息发送确认机制(三个参数)
-   acks=0

    生产者在成功写入消息之前不会等待任何来自服务器的响应，也就是说，如果当中出现了问题，导致服务器没有收到消息，那么生产者就无从得知，消息也就丢失了。不过，因为生产者不需要等待服务器的响应，所以它可以以网络能够支持的最大速度发送消息，从而达到很高的吞吐量。

-   acks=1（默认）

    只要集群首领节点收到消息，生产者就会收到一个来自服务器的成功响应，如果消息无法到达首领节点，生产者会收到一个错误响应，为了避免数据丢失，生产者会重发消息。

-   acks=all

    只有当所有参与赋值的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应，这种模式是最安全的，它可以保证不止一个服务器收到消息，就算有服务器发生崩溃，整个集群仍然可以运行。不过他的延迟比acks=1时更高。

-   retries


生产者从服务器收到的错误有可能是临时性错误，在这种情况下，retries参数的值决定了生产者可以重发消息的次数，如果达到这个次数，生产者会放弃重试返回错误，默认情况下，生产者会在每次重试之间等待100ms

## 1.3.6 消费者详解

（1）消费者工作原理

![](https://img-blog.csdnimg.cn/8ec892555a924e73b5dc2d618674ae99.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_20,color_FFFFFF,t_70,g_se,x_16)

（2）其他参数详解

-   enable.auto.commit

该属性指定了消费者是否自动提交偏移量，默认值是true。为了尽量避免出现重复数据和数据丢失，可以把它设置为false，由自己控制何时提交偏移量。如果把它设置为true,还可以通过配置`auto.commit.interval.ms`属性来控制提交的频率。

-   auto.offset.reset

-   earliest

    当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费

-   latest（kafka默认

    ）

    当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据

-   none

    topic各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常

-   anything else

    向consumer抛出异常


（3）提交和偏移量

每次调用poll()方法，它会返回由生产者写入kafka但还没有被消费者读取过来的记录，我们由此可以追踪到哪些记录是被群组里的哪个消费者读取的，kafka不会像其他JMS队列那样需要得到消费者的确认，这是kafka的一个独特之处，相反，消费者可以使用kafka来追踪消息在分区的位置（偏移量）

消费者会往一个叫做`_consumer_offset`的特殊主题发送消息，消息里包含了每个分区的偏移量。如果消费者一直处于运行状态，那么偏移量就没有什么用处。不过，如果消费者发生崩溃或有新的消费者加入群组，就会触发再均衡，完成再均衡之后，每个消费者可能分配到新的分区，消费者需要读取每个分区最后一次提交的偏移量，然后从偏移量指定的地方继续处理。

如果提交偏移量小于客户端处理的最后一个消息的偏移量，那么处于两个偏移量之间的消息就会被重复处理。

如下图：

![](https://img-blog.csdnimg.cn/6e5da9bd122b4f368a85d7528d25b385.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_12,color_FFFFFF,t_70,g_se,x_16)

如果提交的偏移量大于客户端的最后一个消息的偏移量，那么处于两个偏移量之间的消息将会丢失。

如下图：

![](https://img-blog.csdnimg.cn/bcef9e52f9aa46ffb2bf7e81fbff953a.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_14,color_FFFFFF,t_70,g_se,x_16)

（4）自动提交偏移量

当**`enable.auto.commit`**被设置为true，提交方式就是让消费者自动提交偏移量，每隔5秒消费者会自动把从poll()方法接收的最大偏移量提交上去。提交时间间隔有**`auto.commot.interval.ms`**控制，默认值是5秒。

需要注意到，这种方式可能会导致消息重复消费。假如，某个消费者poll消息后，应用正在处理消息，在3秒后Kafka进行了重平衡，那么由于没有更新位移导致重平衡后这部分消息重复消费。

（5）提交当前偏移量（同步提交）

把**`enable.auto.commit`**设置为false,让应用程序决定何时提交偏移量。使用commitSync()提交偏移量，commitSync()将会提交poll返回的最新的偏移量，所以在处理完所有记录后要确保调用了commitSync()方法。否则还是会有消息丢失的风险。

只要没有发生不可恢复的错误，commitSync()方法会一直尝试直至提交成功，如果提交失败也可以记录到错误日志里。

```
while (true){    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));for (ConsumerRecord<String, String> record : records) {        System.out.println(record.value());        System.out.println(record.key());try {            consumer.commitSync();        }catch (CommitFailedException e){            System.out.println("记录提交失败的异常："+e);        }    }}
```

（6）异步提交

手动提交有一个缺点，那就是当发起提交调用时应用会阻塞。当然我们可以减少手动提交的频率，但这个会增加消息重复的概率（和自动提交一样）。另外一个解决办法是，使用异步提交的API。

```
while (true){    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));for (ConsumerRecord<String, String> record : records) {        System.out.println(record.value());        System.out.println(record.key());    }    consumer.commitAsync(new OffsetCommitCallback() {@Overridepublic void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {if(e!=null){                System.out.println("记录错误的提交偏移量："+ map+",异常信息"+e);            }        }    });}
```

(7）同步和异步组合提交

异步提交也有个缺点，那就是如果服务器返回提交失败，异步提交不会进行重试。相比较起来，同步提交会进行重试直到成功或者最后抛出异常给应用。异步提交没有实现重试是因为，如果同时存在多个异步提交，进行重试可能会导致位移覆盖。

举个例子，假如我们发起了一个异步提交commitA，此时的提交位移为2000，随后又发起了一个异步提交commitB且位移为3000；commitA提交失败但commitB提交成功，此时commitA进行重试并成功的话，会将实际上将已经提交的位移从3000回滚到2000，导致消息重复消费。

```
try {while (true){        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));for (ConsumerRecord<String, String> record : records) {            System.out.println(record.value());            System.out.println(record.key());        }        consumer.commitAsync();    }}catch (Exception e){    e.printStackTrace();    System.out.println("记录错误信息："+e);}finally {try {        consumer.commitSync();    }finally {        consumer.close();    }}
```

## 1.4 spring boot集成kafka收发消息

## 1.4.1 环境搭建

（1）pom依赖，最终的依赖信息

```
<!-- 继承Spring boot工程 --><parent>    <groupId>org.springframework.boot</groupId>    <artifactId>spring-boot-starter-parent</artifactId>    <version>2.1.5.RELEASE</version></parent><properties>    <kafka.version>2.2.7.RELEASE</kafka.version>    <kafka.client.version>2.0.1</kafka.client.version>    <fastjson.version>1.2.58</fastjson.version></properties><dependencies>    <dependency>        <groupId>org.springframework.boot</groupId>        <artifactId>spring-boot-starter-web</artifactId>    </dependency>    <!-- kafkfa -->    <dependency>        <groupId>org.springframework.kafka</groupId>        <artifactId>spring-kafka</artifactId>        <version>${kafka.version}</version>        <exclusions>            <exclusion>                <groupId>org.apache.kafka</groupId>                <artifactId>kafka-clients</artifactId>            </exclusion>        </exclusions>    </dependency>    <dependency>        <groupId>org.apache.kafka</groupId>        <artifactId>kafka-streams</artifactId>        <version>${kafka.client.version}</version>        <exclusions>            <exclusion>                <artifactId>connect-json</artifactId>                <groupId>org.apache.kafka</groupId>            </exclusion>            <exclusion>                <groupId>org.apache.kafka</groupId>                <artifactId>kafka-clients</artifactId>            </exclusion>        </exclusions>    </dependency>    <dependency>        <groupId>org.apache.kafka</groupId>        <artifactId>kafka-clients</artifactId>        <version>${kafka.client.version}</version>    </dependency>    <dependency>        <groupId>com.alibaba</groupId>        <artifactId>fastjson</artifactId>        <version>${fastjson.version}</version>    </dependency></dependencies>
```

（2）在resources下创建文件application.yml

```
server:  port: 9991spring:  application:    name: kafka-demo  kafka:    bootstrap-servers: 192.168.200.130:9092    producer:      retries: 10      key-serializer: org.apache.kafka.common.serialization.StringSerializer      value-serializer: org.apache.kafka.common.serialization.StringSerializer    consumer:      group-id: test-hello-group      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

（3）引导类

```
package com.kafka;import org.springframework.boot.SpringApplication;import org.springframework.boot.autoconfigure.SpringBootApplication;@SpringBootApplicationpublic class KafkaApplication {public static void main(String[] args) {        SpringApplication.run(KafkaApplication.class,args);    }}
```

1.4.2 消息生产者

新建controller

```
package com.kafka.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.kafka.core.KafkaTemplate;import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.RestController;@RestControllerpublic class HelloController {@Autowiredprivate KafkaTemplate<String,String> kafkaTemplate;@GetMapping("/hello")public String hello(){        kafkaTemplate.send("kafka-hello","helloKafka");return "ok";    }}
```

## 1.4.3 消息消费者

新建监听类：

```
package com.itheima.kafka.listener;import org.apache.kafka.clients.consumer.ConsumerRecord;import org.springframework.kafka.annotation.KafkaListener;import org.springframework.stereotype.Component;import java.util.Optional;@Componentpublic class HelloListener {@KafkaListener(topics = {"hello"})public void receiverMessage(ConsumerRecord<?,?> record){        Optional<? extends ConsumerRecord<?, ?>> optional = Optional.ofNullable(record);if(optional.isPresent()){Object value = record.value();            System.out.println(value);        }    }}
```

1.4.4 测试

启动项目访问：http://localhost:9991/hello

## 1.5 传递消息为对象

目前springboot整合后的kafka，因为序列化器是StringSerializer，这个时候如果需要传递对象可以有两种方式

方式一：可以自定义序列化器，对象类型众多，这种方式通用性不强，本章节不介绍

方式二：可以把要传递的对象进行转json字符串，接收消息后再转为对象即可，本项目采用这种方式

（1）新建类User

```
package com.kafka.pojo;public class User {private String username;private Integer age;}
```

（2）修改消息发送

```
@RestControllerpublic class HelloController {@Autowiredprivate KafkaTemplate<String,Object> kafkaTemplate;@GetMapping("/hello")public String hello(){User user = new User();        user.setUsername("zhangsan");        user.setAge(18);        kafkaTemplate.send("kafka-hello", JSON.toJSONString(user));return "ok";    }}
```

（4）修改消费者

```
@Componentpublic class HelloListener {@KafkaListener(topics = {"hello"})public void receiverMessage(ConsumerRecord<?,?> record){        Optional<? extends ConsumerRecord<?, ?>> optional = Optional.ofNullable(record);if(optional.isPresent()){Object value = record.value();User user = JSON.parseObject((String) value, User.class);            System.out.println(user);        }    }}
```

测试效果如下：

![](https://img-blog.csdnimg.cn/e704045c97fe4a8a9955f943c45fe11a.jpg?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rW35bqV5ZCQ5rOh5rOh55qE6bG8,size_20,color_FFFFFF,t_70,g_se,x_16)

## 附录一

### [kafka](https://so.csdn.net/so/search?q=kafka "kafka")生产者发送消息成功失败回调确认

```
@Componentpublic class KafkaSendResultHandler implements ProducerListener {private static final Logger log = LoggerFactory.getLogger(KafkaSendResultHandler.class);@Overridepublic void onSuccess(ProducerRecord producerRecord, RecordMetadata recordMetadata) {String key = producerRecord.key().toString();String topic = producerRecord.topic();        log.info("key：{}，topic：{}， 发送成功回调",key,topic);    }@Overridepublic void onError(ProducerRecord producerRecord, Exception exception) {String key = producerRecord.key().toString();String topic = producerRecord.topic();        log.info("key：{}，topic：{}， 发送异常回调",key,topic);    }}
```

生产者

```
@Componentpublic class SendKafka{@Autowiredprivate KafkaSendResultHandler producerListener;@Autowiredprivate KafkaTemplate<String, String> kafkaTemplate;public void sendKafka(){try {            kafkaTemplate.setProducerListener(producerListener);kafkaTemplate.send("testtopic","testkey","测试值").get();        } catch (Exception e) {            e.printStackTrace();            log.error("{}",e);        }    }}
```

___

### 后面再更新集群和高级应用吧