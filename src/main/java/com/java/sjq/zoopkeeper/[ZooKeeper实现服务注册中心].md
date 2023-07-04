[ZooKeeper实现服务注册中心](https://mp.weixin.qq.com/s/QrcYaam9IiqZo9kcR_55tw)
ZooKeeper实现服务注册中心

ZooKeeper官网地址：https://zookeeper.apache.org/

**一、Zookeeper概述**

ZooKeeper是一个开源的、分布式的应用程序协调服务。它提供的功能包括：命名服务、配置管理、集群管理、分布式锁、负载均衡、分布式队列等。

**（1）命名服务。**可以简单理解为电话簿。电话号码不好记，但是人名好记，要打谁的电话，直接查人名就好了。分布式环境下，经常需要对应用／服务进行统一命名，便于识别不同的服务。类似于域名与IP之间的对应关系，域名容易记住。ZooKeeper通过名称来获取资源或服务的地址、提供者等信息。

**（2）配置管理。**分布式系统都有大量的服务器，比如在搭建Hadoop的HDFS的时候，需要在一台Master主机器上配置好HDFS需要的各种配置文件，然后通过scp命令把这些配置文件复制到其他节点上，这样各个机器拿到的配置信息是一致的，才能成功运行HDFS服务。ZooKeeper提供了这样的一种服务：一种集中管理配置的方法，我们在这个集中的地方修改了配置，所有对这个配置感兴趣的服务都可以获得变更。这样就省去手动复制配置，还保证了可靠性和一致性。

**（3）集群管理。**集群管理包含两点：是否有机器退出和加入、选举Master。在分布式集群中，经常会由于各种原因，比如硬件故障、软件故障、网络问题等，有些新的节点会加入进来，也有老的节点会退出集群。这个时候，集群中有些机器（比如Master节点）需要感知到这种变化，然后根据这种变化做出对应的决策。ZooKeeper集群管理就是感知变化，做出对应的策略。

**（4）分布式锁。**ZooKeeper的一致性文件系统使得锁的问题变得容易。锁服务可以分为两类，一类是保持独占；另一类是控制时序。单机程序的各个进程需要对互斥资源进行访问时需要加锁，分布式程序分布在各个主机上的进程对互斥资源进行访问时也需要加锁。

**二、ZooKeeper的原理**

ZooKeeper一个常用的使用场景是担任服务生产者和服务消费者的注册中心，这也是接下来的章节中会使用到的。服务生产者将自己提供的服务注册到ZooKeeper中心，服务消费者在进行服务调用的时候先到ZooKeeper中查找服务，获取服务生产者的详细信息之后，再去调用服务生产者的内容与数据，具体如图所示  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloCuUDxUgjiaFMG1YICMYHBAkEz19VFNBIYk83WY5oQnVxdDECicd3CZ4g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

三、ZooKeeper集群总体架构

ZooKeeper集群中有4种角色，如表所示。  
![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)

ZooKeeper集群由一组Server节点组成，这一组Server节点中存在一个角色为Leader的节点，其他节点为Follower或Observer。ZooKeeper集群总体架构如图所示。  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloKn1iaaB1TvnppY5u8dAXGcPStE6tU9ibSZdljj0tuXqkcyWaIrvbRZ1g/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

ZooKeeper拥有一个层次的命名空间，这和标准的文件系统非常相似。ZooKeeper中的每个节点被称为Znode，每个节点可以拥有子节点。ZooKeeper数据模型架构如图7-5所示。  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloMofD17RG12CewSqOjFTukr1Mh0UcbiaBQqRQYVJOZiawU74SzFbxXMeg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

ZooKeeper命名空间中的Znode兼具文件和目录两种特点，既可以像文件一样维护数据、元信息、ACL、时间戳等数据结构，又可以像目录一样作为路径标识的一部分。

每个Znode由以下3部分组成。  
stat：存储状态信息，用于描述该Znode的版本、权限等信息。  
data：存储与该Znode关联的数据。  
children：存储该Znode下的子节点。

ZooKeeper虽然可以关联一些数据，但并没有被设计为常规的关系型数据库或者大数据存储，相反的是，Znode用来管理调度数据，比如分布式应用中的配置文件信息、状态信息、汇集位置等。ZooKeeper规定节点的数据大小不能超过1MB，但在实际使用中Znode的数据量应该尽可能小，因为数据量过大会导致ZooKeeper性能明显下降。


Znode有以下4种类型：  
PERSISTENT：**持久节点**。ZooKeeper客户端与ZooKeeper服务器端断开连接后，该节点依旧存在。

PERSISTENT\_SEQUENTIAL：**持久顺序节点**。ZooKeeper客户端与ZooKeeper服务器端断开连接后，该节点依旧存在，并且ZooKeeper给该节点名称进行顺序编号。  
EPHEMERAL：**临时节点**。和持久节点不同的是，临时节点的生命周期和客户端会话绑定。如果客户端会话失效，那么这个节点会被自动清除。在临时节点下不能创建子节点。  
EPHEMERAL\_SEQUENTIAL：**临时顺序节点**。临时顺序节点的生命周期和客户端会话绑定。如果客户端会话失效，那么这个节点会被自动清除。创建的节点会自动加上编号。

**四、命令行客户端zkCli.sh**

ZooKeeper为我们提供了一系列的脚本程序，放置在bin目录下，具体如下：  
zkCleanup.sh：用于清理ZooKeeper的历史数据，包括事务日志文件与快照数据文件。  
zkCli.sh：用于连接ZooKeeper服务器的命令行客户端。

zkEnv.sh：用于设置ZooKeeper的环境变量。

zkServer.sh：用于启动ZooKeeper服务器。

可以使用zkCli.sh命令行客户端来连接与操作ZooKeeper服务器，这是本节的重点。下面我们来看一些具体实例。  
（1）连接ZooKeeper服务器  
在bin目录下执行zkCli.sh脚本  
（2）连接远程ZooKeeper服务器

```
 命令格式：sh zkCli.sh -service <ip>:<port>。
```

（3）列出子节点  
命令格式：ls path \[watch\]。

（4）判断节点是否存在  
使用stat 命令判断 /ay 节点是否存在，很明显，/ay节点不存在  
\[zk: localhost:2181(CONNECTED) 6\] stat /ay  
Node does not exist: /ay

（5）创建节点命令格式：create \[-s\] \[-e\] path data acl。  
\[-s\]选项：用于指定该节点是否为顺序节点。  
\[-e\]选择：用于指定该节点是否为临时节点。

（6）获取节点数据

```
命令格式：get path [watch]。
```

（7）更新节点数据

命令格式：set path data \[version\]。

（8）删除节点

命令格式：delete path \[version\]

**五、ZkClient连接ZooKeeper**

ZkClient是基于ZooKeeper原生的Java API开发的一个易用性更好的客户端，实现了Session超时自动重连、Watcher反复注册等功能，规避了ZooKeeper原生的Java API使用不方便的问题。接下来我们先简单了解ZkClient提供的API。

**（1）创建会话连接API，代码如下所示：**  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloEEpibUYEDCCL4AjVQEfKA8zgrZ7DW5tARFdp5ZDib4mv5QuVG0WnOOZg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)  
serverstring：格式为host1:port1,host2:port2组成的字符串。  
connectionTimeout：创建连接的超时时间，单位为ms。  
sessionTimeout：会话超时时间，单位为ms。  
zkSerializer：自定义zk节点存储数据的序列化方式。对于zkSerializer，ZkClient默认使用Java自带的序列化方式。  
IZkConnection：接口自定义实现。对于IZkConnection接口，ZkClient默认提供了两种实现，分别是zkConnection和InMemoryConnection。一般使用ZkConnection即可满足绝大多数使用场景的需要。

**（2）创建节点API，代码如下所示：**  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloiaO2aEMrercHPSKjLnVnn6KK1hlZB9wuRkrcicd7mbJjO3psvSG1HWxg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloeIMcOla9xq3R7xOheSj2AseF4rmayrE9Kpl0neicxDq20icgwMH0eH7w/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)  
**（3）删除节点主要API，代码如下所示：**  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANlovRicjPPHVr09U9UBouMcZM027hLCzEtaYGluPBxlFfSNcUE8PCR1zkg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloKicCOKODgMInfMBiaxgKhMaibMpaxHnEA7xbelicFkQxImO03aYF6ypxpA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)  
**（4）读取节点主要API，代码如下所示：**  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANloXOVghJMib85BdaHg59PwUOevhY9mg4TCeJIQdZ9sqpfezGUDeibAAmIg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)  
**（5）更新节点主要API，代码如下所示：**  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANlocsbicf5IzUPuaicfQx0t36ia42wgicTtyd7gzKpK8La23zCIIDlSXFC1mg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**六、Spring Boot项目引入ZKClient连接ZooKeeper服务器**

了解ZkClient提供的CRUD接口，，接下来我们学习如何使用ZkClient连接ZooKeeper服务器

1、在pom.xml文件中引入ZkClient的Maven依赖  
![图片](data:image/svg+xml,%3C%3Fxml version='1.0' encoding='UTF-8'%3F%3E%3Csvg width='1px' height='1px' viewBox='0 0 1 1' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'%3E%3Ctitle%3E%3C/title%3E%3Cg stroke='none' stroke-width='1' fill='none' fill-rule='evenodd' fill-opacity='0'%3E%3Cg transform='translate(-249.000000, -126.000000)' fill='%23FFFFFF'%3E%3Crect x='249' y='126' width='1' height='1'%3E%3C/rect%3E%3C/g%3E%3C/g%3E%3C/svg%3E)  
2、我们使用ZkClient对节点进行增、删、改、查等操作  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANlo8Wrw1B0Hubvh1ia4KprsiaiaYrnD3IL5CJT7NMZdSV2ribwibhpVD3C1wWA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**七、ZooKeeper实现服务注册与发现**

ZooKeeper充当一个服务注册表（Service Registry），让多个服务提供者形成一个集群，让服务消费者通过服务注册表获取具体的服务访问地址（IP+端口）去访问具体的服务提供者，如图7-6所示。  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANlo6vqUJeFGHqXiaqA8TXBQTCAbmIibqw5grVToFbrYUDXo1aZBY1gO7gyQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

ZooKeeper类似于分布式文件系统，当服务提供者部署后，将自己的服务注册到ZooKeeper的某一路径上：/{service}/{version}/{ip:port}，比如我们的ProductService部署到两台机器上，ZooKeeper上就会创建两条目录：/ProductService/1.0.0/100.19.20.01:16888和/ProductService/1.0.0/100.19.20.02:16888。

再来看一张更容易理解的图，具体如图7-7所示。  
![图片](https://mmbiz.qpic.cn/mmbiz_png/ysfza24cADtAzTdcEdcTxutpojeQANlokZYcuqnTD4RiaE5GOnEyeCmOQbPmQatbjAOhqJeO9wqPTZDwAQO2aCw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

（1）ZooKeeper客户端通过创建ZooKeeper的一个实例对象连接ZooKeeper服务器，调用该类的接口与服务器交互。  
（2）根据服务提供者发布的服务列表循环调用create接口，创建目录节点，同时将服务属性（服务名称和IP）写入目录节点的内容中。  
（3）服务消费者同样通过ZooKeeper客户端创建ZooKeeper的一个实例对象，连接ZooKeeper服务器，调用该类的接口与服务器交互。  
（4）服务消费者在第一次调用服务时，会通过注册中心找到相应的服务的IP地址列表，并缓存到本地，以供后续使用。当消费者调用服务时，不会再去请求注册中心，而是直接通过负载均衡算法从IP列表中获取一个服务提供者的服务器调用服务。  
（5）当服务提供者的某台服务器宕机或下线时，相应的IP会被移除。同时，注册中心会将新的服务IP地址列表发送给服务消费者机器，缓存在消费者本机。  
（6）当某个服务的所有服务器都下线了，这个服务也就下线了。  
（7）同样，当服务提供者的某台服务器上线时，注册中心会将新的服务IP地址列表发送给服务消费者机器，缓存在消费者本机。

（8）服务提供方可以根据服务消费者的数量来作为服务下线的依据。  
ZooKeeper提供了“心跳检测”功能，它会定时向各个服务提供者发送请求，实际上建立的是一个Socket长连接，如果长期没有响应，服务中心就认为该服务提供者已经“挂了”，并将其剔除，比如10.10.10.02这台机器如果宕机了，ZooKeeper上的路径就只剩/ProductService/1.0.0/10.10.10.01:16888。

服务消费者会去监听相应路径（/ProductService/1.0.0），一旦路径上的数据有任何变化（增加或减少），ZooKeeper都会通知服务消费方，服务提供者地址列表已经发生改变，从而进行更新。更为重要的是ZooKeeper与生俱来的容错容灾能力（比如Leader选举），可以确保服务注册表的高可用性。

引用出处：https://www.cnblogs.com/callbin/p/14599990.html