[部署方式  RocketMQ](httpsrocketmq.apache.orgzhdocs4.xdeployment01deploy#%E5%A4%9A%E8%8A%82%E7%82%B9%E9%9B%86%E7%BE%A4%E5%A4%9A%E5%89%AF%E6%9C%AC%E6%A8%A1%E5%BC%8F-%E5%BC%82%E6%AD%A5%E5%A4%8D%E5%88%B6)

## 部署方式

Apache RocketMQ 5.0 版本完成基本消息收发，包括 NameServer、Broker、Proxy 组件。 在 5.0 版本中 Proxy 和 Broker 根据实际诉求可以分为 Local 模式和 Cluster 模式，一般情况下如果没有特殊需求，或者遵循从早期版本平滑升级的思路，可以选用Local模式。

-   在 Local 模式下，Broker 和 Proxy 是同进程部署，只是在原有 Broker 的配置基础上新增 Proxy 的简易配置就可以运行。
-   在 Cluster 模式下，Broker 和 Proxy 分别部署，即在原有的集群基础上，额外再部署 Proxy 即可。

## [部署方案和使用约束](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E9%83%A8%E7%BD%B2%E6%96%B9%E6%A1%88%E5%92%8C%E4%BD%BF%E7%94%A8%E7%BA%A6%E6%9D%9F "部署方案和使用约束的直接链接")

## [Local模式部署](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#local%E6%A8%A1%E5%BC%8F%E9%83%A8%E7%BD%B2 "Local模式部署的直接链接")

由于 Local 模式下 Proxy 和 Broker 是同进程部署，Proxy本身无状态，因此主要的集群配置仍然以 Broker 为基础进行即可。

### [单组节点单副本模式](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%8D%95%E7%BB%84%E8%8A%82%E7%82%B9%E5%8D%95%E5%89%AF%E6%9C%AC%E6%A8%A1%E5%BC%8F "单组节点单副本模式的直接链接")

警告

这种方式风险较大，因为 Broker 只有一个节点，一旦Broker重启或者宕机时，会导致整个服务不可用。不建议线上环境使用, 可以用于本地测试。

#### 启动 NameServer[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8-nameserver "启动 NameServer的直接链接")

```
### 首先启动Name Server$ nohup sh mqnamesrv & ### 验证Name Server 是否启动成功$ tail -f ~/logs/rocketmqlogs/namesrv.logThe Name Server boot success...
```

#### 启动 Broker+Proxy[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8-brokerproxy "启动 Broker+Proxy的直接链接")

```
$ nohup sh bin/mqproxy -n localhost:9876 &### 验证Broker 是否启动成功，例如Broker的IP为：192.168.1.2，且名称为broker-a$ tail -f ~/logs/rocketmqlogs/Broker.log The broker[xxx, 192.169.1.2:10911] boot success...
```

### 多组节点（集群）单副本模式[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%A4%9A%E7%BB%84%E8%8A%82%E7%82%B9%E9%9B%86%E7%BE%A4%E5%8D%95%E5%89%AF%E6%9C%AC%E6%A8%A1%E5%BC%8F "多组节点（集群）单副本模式的直接链接")

一个集群内全部部署 Master 角色，不部署Slave 副本，例如2个Master或者3个Master，这种模式的优缺点如下：

-   优点：配置简单，单个Master宕机或重启维护对应用无影响，在磁盘配置为RAID10时，即使机器宕机不可恢复情况下，由于RAID10磁盘非常可靠，消息也不会丢（异步刷盘丢失少量消息，同步刷盘一条不丢），性能最高；
    
-   缺点：单台机器宕机期间，这台机器上未被消费的消息在机器恢复之前不可订阅，消息实时性会受到影响。
    

#### 启动NameServer[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8nameserver "启动NameServer的直接链接")

NameServer需要先于Broker启动，且如果在生产环境使用，为了保证高可用，建议一般规模的集群启动3个NameServer，各节点的启动命令相同，如下：

```
# 首先启动Name Server$ nohup sh mqnamesrv & ### 验证Name Server 是否启动成功$ tail -f ~/logs/rocketmqlogs/namesrv.logThe Name Server boot success...
```

#### 启动Broker+Proxy集群[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8brokerproxy%E9%9B%86%E7%BE%A4 "启动Broker+Proxy集群的直接链接")

```
### 在机器A，启动第一个Master，例如NameServer的IP为：192.168.1.1$ nohup sh mqproxy -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-noslave/broker-a.properties & ### 在机器B，启动第二个Master，例如NameServer的IP为：192.168.1.1$ nohup sh mqproxy -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-noslave/broker-b.properties &...
```

备注

如上启动命令是在单个NameServer情况下使用的。对于多个NameServer的集群，Broker启动命令中`-n`后面的地址列表用分号隔开即可，例如 `192.168.1.1:9876;192.161.2:9876`。

## 多节点（集群）多副本模式-异步复制[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%A4%9A%E8%8A%82%E7%82%B9%E9%9B%86%E7%BE%A4%E5%A4%9A%E5%89%AF%E6%9C%AC%E6%A8%A1%E5%BC%8F-%E5%BC%82%E6%AD%A5%E5%A4%8D%E5%88%B6 "多节点（集群）多副本模式-异步复制的直接链接")

每个Master配置一个Slave，有多组 Master-Slave，HA采用异步复制方式，主备有短暂消息延迟（毫秒级），这种模式的优缺点如下：

-   优点：即使磁盘损坏，消息丢失的非常少，且消息实时性不会受影响，同时Master宕机后，消费者仍然可以从Slave消费，而且此过程对应用透明，不需要人工干预，性能同多Master模式几乎一样；
    
-   缺点：Master宕机，磁盘损坏情况下会丢失少量消息。
    

#### 启动NameServer[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8nameserver-1 "启动NameServer的直接链接")

```
### 首先启动Name Server$ nohup sh mqnamesrv & ### 验证Name Server 是否启动成功$ tail -f ~/logs/rocketmqlogs/namesrv.logThe Name Server boot success...
```

#### 启动Broker+Proxy集群[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8brokerproxy%E9%9B%86%E7%BE%A4-1 "启动Broker+Proxy集群的直接链接")

```
### 在机器A，启动第一个Master，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-async/broker-a.properties & ### 在机器B，启动第二个Master，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-async/broker-b.properties & ### 在机器C，启动第一个Slave，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-async/broker-a-s.properties & ### 在机器D，启动第二个Slave，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-async/broker-b-s.properties &
```

### 多节点（集群）多副本模式-同步双写[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%A4%9A%E8%8A%82%E7%82%B9%E9%9B%86%E7%BE%A4%E5%A4%9A%E5%89%AF%E6%9C%AC%E6%A8%A1%E5%BC%8F-%E5%90%8C%E6%AD%A5%E5%8F%8C%E5%86%99 "多节点（集群）多副本模式-同步双写的直接链接")

每个Master配置一个Slave，有多对Master-Slave，HA采用同步双写方式，即只有主备都写成功，才向应用返回成功，这种模式的优缺点如下：

-   优点：数据与服务都无单点故障，Master宕机情况下，消息无延迟，服务可用性与数据可用性都非常高；
    
-   缺点：性能比异步复制模式略低（大约低10%左右），发送单个消息的RT会略高，且目前版本在主节点宕机后，备机不能自动切换为主机。
    

#### 启动NameServer[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8nameserver-2 "启动NameServer的直接链接")

```
### 首先启动Name Server$ nohup sh mqnamesrv & ### 验证Name Server 是否启动成功$ tail -f ~/logs/rocketmqlogs/namesrv.logThe Name Server boot success...
```

#### 启动 Broker+Proxy 集群[](https://rocketmq.apache.org/zh/docs/4.x/deployment/01deploy#%E5%90%AF%E5%8A%A8-brokerproxy-%E9%9B%86%E7%BE%A4 "启动 Broker+Proxy 集群的直接链接")

```
### 在机器A，启动第一个Master，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-sync/broker-a.properties & ### 在机器B，启动第二个Master，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-sync/broker-b.properties & ### 在机器C，启动第一个Slave，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-sync/broker-a-s.properties & ### 在机器D，启动第二个Slave，例如NameServer的IP为：192.168.1.1$ nohup sh mqbroker -n 192.168.1.1:9876 -c $ROCKETMQ_HOME/conf/2m-2s-sync/broker-b-s.properties &
```

提示

以上 Broker 与 Slave 配对是通过指定相同的 BrokerName 参数来配对，Master 的 BrokerId 必须是 0，Slave 的 BrokerId 必须是大于 0 的数。另外一个 Master 下面可以挂载多个 Slave，同一 Master 下的多个 Slave 通过指定不同的 BrokerId 来区分。$ROCKETMQ\_HOME指的RocketMQ安装目录，需要用户自己设置此环境变量。