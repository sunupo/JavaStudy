[RocketMQ保姆级教程 - 掘金](https://juejin.cn/post/7134227366481494046)
大家好，我是三友~~

上周花了一点时间从头到尾、从无到有地搭建了一套RocketMQ的环境，觉得还挺easy的，所以就写篇文章分享给大家。

整篇文章可以大致分为三个部分，第一部分属于一些核心概念和工作流程的讲解；第二部分就是纯手动搭建了一套环境；第三部分是基于环境进行测试和集成到SpringBoot，因为整个过程讲的比较细，所以我称之为“保姆级教程”。

好了，废话补多少，直接进入主题。

## 前言

RocketMQ是阿里巴巴旗下一款开源的MQ框架，经历过双十一考验、Java编程语言实现，有非常好完整生态系统。RocketMQ作为一款纯java、分布式、队列模型的开源消息中间件，支持事务消息、顺序消息、批量消息、定时消息、消息回溯等，总之就是葛大爷的一句话

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/41f4913bb29547378b1fa762e07deee4~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

## 核心概念

-   **NameServer**：可以理解为是一个注册中心，主要是用来保存topic路由信息，管理Broker。在NameServer的集群中，NameServer与NameServer之间是没有任何通信的。
-   **Broker**：核心的一个角色，主要是用来保存topic的信息，接受生产者产生的消息，持久化消息。在一个Broker集群中，相同的BrokerName可以称为一个Broker组，一个Broker组中,BrokerId为0的为主节点，其它的为从节点。BrokerName和BrokerId是可以在Broker启动时通过配置文件配置的。每个Broker组只存放一部分消息。
-   **生产者**：生产消息的一方就是生产者
-   **生产者组**：一个生产者组可以有很多生产者，只需要在创建生产者的时候指定生产者组，那么这个生产者就在那个生产者组
-   **消费者**：用来消费生产者消息的一方
-   **消费者组**：跟生产者一样，每个消费者都有所在的消费者组，一个消费者组可以有很多的消费者，不同的消费者组消费消息是互不影响的。
-   **topic（主题）** ：可以理解为一个消息的集合的名字，生产者在发送消息的时候需要指定发到哪个topic下，消费者消费消息的时候也需要知道自己消费的是哪些topic底下的消息。
-   **Tag（子主题）** ：比topic低一级，可以用来区分同一topic下的不同业务类型的消息，发送消息的时候也需要指定。

这里有组的概念是因为可以用来做到不同的生产者组或者消费者组有不同的配置，这样就可以使得生产者或者消费者更加灵活。

## 工作流程

说完核心概念，再来说一下核心的工作流程，这里我先画了一张图。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2fd7c6c852c5472ca07317929585d101~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

通过这张图就可以很清楚的知道，RocketMQ大致的工作流程：

-   Broker启动的时候，会往每台NameServer（因为NameServer之间不通信，所以每台都得注册）注册自己的信息，这些信息包括自己的ip和端口号，自己这台Broker有哪些topic等信息。
-   Producer在启动之后会跟会NameServer建立连接，定期从NameServer中获取Broker的信息，当发送消息的时候，会根据消息需要发送到哪个topic去找对应的Broker地址，如果有的话，就向这台Broker发送请求；没有找到的话，就看根据是否允许自动创建topic来决定是否发送消息。
-   Broker在接收到Producer的消息之后，会将消息存起来，持久化，如果有从节点的话，也会主动同步给从节点，实现数据的备份
-   Consumer启动之后也会跟会NameServer建立连接，定期从NameServer中获取Broker和对应topic的信息，然后根据自己需要订阅的topic信息找到对应的Broker的地址，然后跟Broker建立连接，获取消息，进行消费

就跟上面的图一样，整体的工作流程还是比较简单的，这里我简化了很多概念，主要是为了好理解。

## 环境搭建

终于讲完了一些简单的概念，接下来就来搭建一套RocketMQ的环境。

通过上面分析，我们知道，在RocketMQ中有NameServer、Broker、生产者、消费者四种角色。而生产者和消费者实际上就是业务系统，所以这里不需要搭建，真正要搭建的就是NameServer和Broker，但是为了方便RocketMQ数据的可视化，这里我多搭建一套可视化的服务。

搭建过程比较简单，按照步骤一步一步来就可以完成，如果提示一些命令不存在，那么直接通过yum安装这些命令就行。

### 一、准备

需要准备一个linux服务器，需要先安装好JDK

关闭防火墙

```
systemctl stop firewalld
systemctl disable firewalld
复制代码
```

#### 下载并解压RocketMQ

##### 1、创建一个目录，用来存放rocketmq相关的东西

```
mkdir /usr/rocketmq
cd /usr/rocketmq
复制代码
```

##### 2、下载并解压rocketmq

下载

```
wget https://archive.apache.org/dist/rocketmq/4.7.1/rocketmq-all-4.7.1-bin-release.zip
复制代码
```

解压

```
unzip rocketmq-all-4.7.1-bin-release.zip
复制代码
```

看到这一个文件夹就完成了

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e31c93e4c5fd4e7b9aa8d267eceeb858~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

然后进入rocketmq-all-4.7.1-bin-release文件夹

```
cd rocketmq-all-4.7.1-bin-release
复制代码
```

RocketMQ的东西都在这了

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/edb82688546b4504ac327fb8f0c357c3~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

### 二、搭建NameServer

#### 修改jvm参数

在启动NameServer之前，强烈建议修改一下启动时的jvm参数，因为默认的参数都比较大，为了避免内存不够，建议修改小，当然，如果你的内存足够大，可以忽略。

```
vi bin/runserver.sh
复制代码
```

修改画圈的这一行

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d0da51f37e184735bc1a562f96c7f494~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

这里你可以直接修改成跟我一样的

```
-server -Xms512m -Xmx512m -Xmn256m -XX:MetaspaceSize=32m -XX:MaxMetaspaceSize=50m
复制代码
```

#### 启动NameServer

修改完之后，执行如下命令就可以启动NameServer了

```
nohup sh bin/mqnamesrv &
复制代码
```

查看NameServer日志

```
tail -f ~/logs/rocketmqlogs/namesrv.log
复制代码
```

如果看到如下的日志，就说明启动成功了

![NameServer日志](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2965ab19e6a84defa47ad5815191f062~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

NameServer日志

### 三、搭建Broker

这里启动单机版的Broker

#### 修改jvm参数

跟启动NameServer一样，也建议去修改jvm参数

```
vi bin/runbroker.sh
复制代码
```

将画圈的地方设置小点，当然也别太小啊

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/dc3cafc6786245ff85dcf66d4ab6ef73~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

当然你还是可以跟我设置的一样

```
-server -Xms1g -Xmx1g -Xmn512m
复制代码
```

#### 修改Broker配置文件broker.conf

这里需要改一下Broker配置文件，需要指定NameServer的地址，因为需要Broker需要往NameServer注册

```
vi conf/broker.conf
复制代码
```

Broker配置文件

![Broker配置文件](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c841328a272f45469fc54182b62cfb16~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

Broker配置文件

这里就能看出Broker的配置了，什么Broker集群的名称啊，Broker的名称啊，Broker的id啊，都跟前面说的对上了。

在文件末尾追加地址

```
namesrvAddr = localhost:9876
复制代码
```

因为NameServer跟Broker在同一台机器，所以是localhost，NameServer端口默认的是9876。

不过这里我还建议再修改一处信息，因为Broker向NameServer进行注册的时候，带过去的ip如果不指定就会自动获取，但是自动获取的有个坑，就是有可能你的电脑无法访问到这个自动获取的ip，所以我建议手动指定你的电脑可以访问到的服务器ip。

我的虚拟机的ip是192.168.200.143，所以就指定为192.168.200.143，如下

```
brokerIP1 = 192.168.200.143
brokerIP2 = 192.168.200.143
复制代码
```

如果以上都配置的话，最终的配置文件应该如下，红圈的为新加的

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4cb1500e2f374fd1bd47fca5f6a3a839~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

#### 启动Broker

```
nohup sh bin/mqbroker -c conf/broker.conf &
复制代码
```

\-c 参数就是指定配置文件

查看日志

```
tail -f ~/logs/rocketmqlogs/broker.log
复制代码
```

当看到如下日志就说明启动成功了

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/07794b9a6b814e2fae0b0aba5549f8ec~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

### 四、搭建可视化控制台

其实前面NameServer和Broker搭建完成之后，就可以用来收发消息了，但是为了更加直观，可以搭一套可视化的服务。

可视化服务其实就是一个jar包，启动就行了。

jar包可以从这获取

> ###### 链接：[pan.baidu.com/s/16s1qwY2q…](https://link.juejin.cn/?target=https%3A%2F%2Fpan.baidu.com%2Fs%2F16s1qwY2qzE2bxR81t5Wm6w "https://pan.baidu.com/s/16s1qwY2qzE2bxR81t5Wm6w")
>
> ###### 提取码：s0sd

将jar包上传到服务器，放到/usr/rocketmq的目录底下，当然放哪都无所谓，这里只是为了方便，因为rocketmq的东西都在这里

然后进入/usr/rocketmq下，执行如下命名

```
nohup java -jar -server -Xms256m -Xmx256m -Drocketmq.config.namesrvAddr=localhost:9876 -Dserver.port=8088 rocketmq-console-ng-1.0.1.jar &
复制代码
```

rocketmq.config.namesrvAddr就是用来指定NameServer的地址的

查看日志

```
tail -f ~/logs/consolelogs/rocketmq-console.log
复制代码
```

当看到如下日志，就说明启动成功了

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fa972bbadb884b57954259e6e897e668~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

然后在浏览器中输入[http://linux服务器的ip:8088/就可以看到控制台了，如果无法访问，可以看看防火墙有没有关闭](https://link.juejin.cn/?target=http%3A%2F%2Flinux%25E6%259C%258D%25E5%258A%25A1%25E5%2599%25A8%25E7%259A%2584ip%3A8088%2F%25E5%25B0%25B1%25E5%258F%25AF%25E4%25BB%25A5%25E7%259C%258B%25E5%2588%25B0%25E6%258E%25A7%25E5%2588%25B6%25E5%258F%25B0%25E4%25BA%2586%25EF%25BC%258C%25E5%25A6%2582%25E6%259E%259C%25E6%2597%25A0%25E6%25B3%2595%25E8%25AE%25BF%25E9%2597%25AE%25EF%25BC%258C%25E5%258F%25AF%25E4%25BB%25A5%25E7%259C%258B%25E7%259C%258B%25E9%2598%25B2%25E7%2581%25AB%25E5%25A2%2599%25E6%259C%2589%25E6%25B2%25A1%25E6%259C%2589%25E5%2585%25B3%25E9%2597%25AD "http://linux%E6%9C%8D%E5%8A%A1%E5%99%A8%E7%9A%84ip:8088/%E5%B0%B1%E5%8F%AF%E4%BB%A5%E7%9C%8B%E5%88%B0%E6%8E%A7%E5%88%B6%E5%8F%B0%E4%BA%86%EF%BC%8C%E5%A6%82%E6%9E%9C%E6%97%A0%E6%B3%95%E8%AE%BF%E9%97%AE%EF%BC%8C%E5%8F%AF%E4%BB%A5%E7%9C%8B%E7%9C%8B%E9%98%B2%E7%81%AB%E5%A2%99%E6%9C%89%E6%B2%A1%E6%9C%89%E5%85%B3%E9%97%AD")

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aab68fb003714359a34ef53a9027e0fa~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

右上角可以把语言切换成中文

![Broker集群信息](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/02747d127589459bb60d19adaa568df1~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

Broker集群信息

![topic信息](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/20979eea5f374404960779488b6549b2~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

topic信息

通过控制台可以查看生产者、消费者、Broker集群等信息，非常直观。

功能很多，这里就不一一介绍了。

## 测试

环境搭好之后，就可以进行测试了。

#### 引入依赖

```
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-client</artifactId>
    <version>4.7.1</version>
</dependency>
复制代码
```

#### 生产者发送消息

```
public class Producer {
    public static void main(String[] args) throws Exception {
        //创建一个生产者，指定生产者组为sanyouProducer
        DefaultMQProducer producer = new DefaultMQProducer("sanyouProducer");

        // 指定NameServer的地址
        producer.setNamesrvAddr("192.168.200.143:9876");
        // 第一次发送可能会超时，我设置的比较大
        producer.setSendMsgTimeout(60000);

        // 启动生产者
        producer.start();

        // 创建一条消息
        // topic为 sanyouTopic
        // 消息内容为 三友的java日记
        // tags 为 TagA
        Message msg = new Message("sanyouTopic", "TagA", "三友的java日记 ".getBytes(RemotingHelper.DEFAULT_CHARSET));

        // 发送消息并得到消息的发送结果，然后打印
        SendResult sendResult = producer.send(msg);
        System.out.printf("%s%n", sendResult);

        // 关闭生产者
        producer.shutdown();
    }

}
复制代码
```

-   构建一个消息生产者DefaultMQProducer实例，然后指定生产者组为sanyouProducer；
-   指定NameServer的地址：服务器的ip:9876，因为需要从NameServer拉取Broker的信息
-   producer.start() 启动生产者
-   构建一个内容为三友的java日记的消息，然后指定这个消息往sanyouTopic这个topic发送
-   producer.send(msg)：发送消息，打印结果
-   关闭生产者

运行结果如下

```
SendResult [sendStatus=SEND_OK, msgId=C0A81FAF54F818B4AAC2475FD2010000, offsetMsgId=C0A8C88F00002A9F000000000009AE55, messageQueue=MessageQueue [topic=sanyouTopic, brokerName=broker-a, queueId=0], queueOffset=0]
复制代码
```

sendStatus=SEND\_OK 说明发送成功了，此时就能后控制台看到未消费的消息了。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9285ae4152b546148d165cc86310a5fc~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

到控制台看到消息那块，然后选定发送的topic，查询的时间范围手动再选一下，不选就查不出来(我怀疑这是个bug)，然后查询就能看到了一条消息。

然后点击一下MESSAGE DETAIL就能够看到详情。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/da2d65727707477c814403b3e2af3b8d~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

这里就能看到发送消息的详细信息。

左下角消息的消费的消费，因为我们还没有消费者订阅这个topic，所以左下角没数据。

#### 消费者消费消息

```
public class Consumer {
    public static void main(String[] args) throws InterruptedException, MQClientException {

        // 通过push模式消费消息，指定消费者组
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("sanyouConsumer");

        // 指定NameServer的地址
        consumer.setNamesrvAddr("192.168.200.143:9876");

        // 订阅这个topic下的所有的消息
        consumer.subscribe("sanyouTopic", "*");

        // 注册一个消费的监听器，当有消息的时候，会回调这个监听器来消费消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.printf("消费消息:%s", new String(msg.getBody()) + "\n");
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // 启动消费者
        consumer.start();

        System.out.printf("Consumer Started.%n");
    }
}
复制代码
```

-   创建一个消费者实例对象，指定消费者组为sanyouConsumer
-   指定NameServer的地址：服务器的ip:9876
-   订阅 sanyouTopic 这个topic的所有信息
-   consumer.registerMessageListener ，这个很重要，是注册一个监听器，这个监听器是当有消息的时候就会回调这个监听器，处理消息，所以需要用户实现这个接口，然后处理消息。
-   启动消费者

启动之后，消费者就会消费刚才生产者发送的消息，于是控制台就打印出如下信息

```
Consumer Started.
消费消息:三友的java日记 
复制代码
```

此时再去看控制台

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cf9a825784074ebeb2fe3814c9820920~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

发现被sanyouConsumer这个消费者组给消费了。

## SpringBoot环境下集成RocketMQ

### 集成

在实际项目中肯定不会像上面测试那样用，都是集成SpringBoot的。

#### 1、引入依赖

```
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.1.1</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>2.1.1.RELEASE</version>
</dependency>
复制代码
```

#### 2、yml配置

```
rocketmq:
  producer:
    group: sanyouProducer
  name-server: 192.168.200.143:9876
复制代码
```

#### 3、创建消费者

SpringBoot底下只需要实现RocketMQListener接口，然后加上@RocketMQMessageListener注解即可

```
@Component
@RocketMQMessageListener(consumerGroup = "sanyouConsumer", topic = "sanyouTopic")
public class SanYouTopicListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String msg) {
        System.out.println("处理消息:" + msg);
    }

}
复制代码
```

@RocketMQMessageListener需要指定消费者属于哪个消费者组，消费哪个topic，NameServer的地址已经通过yml配置文件配置类

#### 4、测试

```
@SpringBootTest(classes = RocketMQApplication.class)
@RunWith(SpringRunner.class)
public class RocketMQTest {

    @Autowired
    private RocketMQTemplate template;

    @Test
    public void send() throws InterruptedException {
        template.convertAndSend("sanyouTopic", "三友的java日记");
        Thread.sleep(60000);
    }

}
复制代码
```

直接注入一个RocketMQTemplate，然后通过RocketMQTemplate发送消息。

运行结果如下：

```
处理消息:三友的java日记
复制代码
```

的确消费到消息了。

### 原理

其实原理是一样的，只不过在SpringBoot中给封装了一层，让使用起来更加简单。

#### 1、RocketMQTemplate构造代码

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f33fda63c24d49b59043f2bdd396d5f8~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

所以从这可以看出，最终在构造RocketMQTemplate的时候，传入了一个DefaultMQProducer，所以可想而知，最终RocketMQTemplate发送消息也是通过DefaultMQProducer发送的。

#### 2、@RocketMQMessageListener 注解处理

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/508582b14b7d44eabf3ca200e6a50929~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

从这可以看出，会为每一个加了@RocketMQMessageListener注解的对象创建一个DefaultMQPushConsumer，所以最终也是通过DefaultMQPushConsumer消费消息的。

至于监听器，是在这

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/18ab57f8ba11406cb2971dfaa1664add~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

遍历每条消息，然后调用handleMessage，最终会调用实现了RocketMQListener的对象处理消息。

## 最后

通过上面的理论介绍和实际的环境搭建再加上代码的测试，相信应该可以对RocketMQ有个入门，有兴趣的小伙伴可以手动搭起来，整个过程顺利的话可能就十几二十分钟这样子。

最后我再说一句，从文章整体也可以看出本文没有涉及太深入的一些机制和原理的讲解，比如消息是如何存储的，事务和延迟消息是如何实现的，主从是如何同步的等等，甚至压根就没提到队列这个词，主要是因为我打算后面再写一篇文章，来单独剖析这些机制和原理。

最后的最后，本文所有的代码地址:

> ##### [github.com/sanyou3/roc…](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Fsanyou3%2Frocketmq-demo.git "https://github.com/sanyou3/rocketmq-demo.git")

搜索关注公众号 **三友的java日记** ，及时干货不错过，公众号致力于通过画图加上通俗易懂的语言讲解技术，让技术更加容易学习。

**往期热门文章推荐**

[三万字盘点Spring/Boot的那些常用扩展点](https://link.juejin.cn/?target=http%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzg5MDczNDI0Nw%3D%3D%26mid%3D2247489480%26idx%3D1%26sn%3D55e2f9cedb449c9e7615c2818b04eb46%26chksm%3Dcfd94400f8aecd1653557c05885c037cf932fc7596850ba7386a5dd85ad741f728093f8aa070%26scene%3D21%23wechat_redirect "http://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247489480&idx=1&sn=55e2f9cedb449c9e7615c2818b04eb46&chksm=cfd94400f8aecd1653557c05885c037cf932fc7596850ba7386a5dd85ad741f728093f8aa070&scene=21#wechat_redirect")

[RocketMQ的push消费方式实现的太聪明了](https://link.juejin.cn/?target=http%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzg5MDczNDI0Nw%3D%3D%26mid%3D2247489718%26idx%3D1%26sn%3Da19aa1e075396228fd7fdeae49ffdf1d%26chksm%3Dcfd94b7ef8aec2687341268ed8bde580e7cf92a7c9d0dd547f8d3a5e970393fc3405ae03de8f%26scene%3D21%23wechat_redirect "http://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247489718&idx=1&sn=a19aa1e075396228fd7fdeae49ffdf1d&chksm=cfd94b7ef8aec2687341268ed8bde580e7cf92a7c9d0dd547f8d3a5e970393fc3405ae03de8f&scene=21#wechat_redirect")

[一网打尽异步神器CompletableFuture](https://link.juejin.cn/?target=http%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzg5MDczNDI0Nw%3D%3D%26mid%3D2247487611%26idx%3D1%26sn%3De9a2373d12fbbafdb2b67803f19a6d5a%26chksm%3Dcfd943b3f8aecaa5dc730d74d4e4d6d4c662f768f63a6221f2d2db8745311e6e39bd6f9f5564%26scene%3D21%23wechat_redirect "http://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247487611&idx=1&sn=e9a2373d12fbbafdb2b67803f19a6d5a&chksm=cfd943b3f8aecaa5dc730d74d4e4d6d4c662f768f63a6221f2d2db8745311e6e39bd6f9f5564&scene=21#wechat_redirect")

[@Async注解的坑，小心](https://link.juejin.cn/?target=http%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzg5MDczNDI0Nw%3D%3D%26mid%3D2247487761%26idx%3D1%26sn%3Defcecf89099e55f7a89579283edc27be%26chksm%3Dcfd942d9f8aecbcf188d12c5560c75a3e7ce2d064177d9665a1e2f8453f1f68cffac79871ee4%26scene%3D21%23wechat_redirect "http://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247487761&idx=1&sn=efcecf89099e55f7a89579283edc27be&chksm=cfd942d9f8aecbcf188d12c5560c75a3e7ce2d064177d9665a1e2f8453f1f68cffac79871ee4&scene=21#wechat_redirect")

[7000字+24张图带你彻底弄懂线程池](https://link.juejin.cn/?target=http%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzg5MDczNDI0Nw%3D%3D%26mid%3D2247484636%26idx%3D1%26sn%3D834df6a5bf598819d30b2b8f6d42d242%26chksm%3Dcfd95714f8aede0210ad9d215b69b25851c17cfb02cab849c6bccea49868b66adda596487b13%26scene%3D21%23wechat_redirect "http://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247484636&idx=1&sn=834df6a5bf598819d30b2b8f6d42d242&chksm=cfd95714f8aede0210ad9d215b69b25851c17cfb02cab849c6bccea49868b66adda596487b13&scene=21#wechat_redirect")