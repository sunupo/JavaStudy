# [Kafka与RocketMQ区别\_kafka rocketmq区别\_Code@Z的博客-CSDN博客](https://blog.csdn.net/MrLiar17/article/details/119462424)

## 刷盘
### kafka
-   Kafka使用异步刷盘方式，异步Replication

### rocketmq


-   RocketMQ支持异步实时刷盘，同步刷盘，同步Replication，异步Replication
> 刷盘方式可以通过Broker配置文件里的flushDiskType参数设置，这个参数有两种值： 
> - SYNC_FLUSH (同步刷盘)。 
> - ASYNC_FLUSH (异步刷盘)。
>   ```text
>   //master
>   brokerClusterName=DefaultCluster
>   brokerName=broker-a
>   brokerId=0
>   deleteWhen=04
>   fileReservedTime=48
>   brokerRole=ASYNC_MASTER
>   flushDiskType=ASYNC_FLUSH
>   
>   // slave
>   brokerClusterName=DefaultCluster
>   brokerName=broker-a
>   brokerId=1
>   deleteWhen=04
>   fileReservedTime=48
>   brokerRole=SLAVE
>   flushDiskType=ASYNC_FLUSH
>   ```

> 日志保留时间
当 kafka server 的被写入海量消息后，会生成很多数据文件，且占用大量磁盘空间，如果不及时清理，可能导致磁盘空间不够用，kafka 默认是保留7天。
> 
> 参数： log.retention.hours = 168
> 

- 同步刷盘
- 异步刷盘（RocketMQ默认）
- 异步刷盘+缓冲区


# 总结

目前很多主流MQ都有自己的刷盘机制，可以从下表对比其中的差异。

<table border="1" cellpadding="1" cellspacing="1" style="width:603px;"><tbody><tr><td><p id="ue4355401" style="text-align:center;">MQ名</p></td><td style="width:148px;"><p id="u3252cc1d" style="text-align:center;">刷盘机制</p></td><td style="width:368px;"><p id="uf46ed5c1" style="text-align:center;">刷盘配置</p></td></tr><tr><td><p id="uf298955e">RocketMQ</p></td><td style="width:148px;"><p id="u60e44188">同步刷盘、异步刷盘</p></td><td style="width:368px;"><p id="uecfe64c6">Broker配置文件里的flushDiskType里配置</p></td></tr><tr><td><p id="u609d9853"><a href="https://so.csdn.net/so/search?q=Kafka&amp;spm=1001.2101.3001.7020" target="_blank" class="hl hl-1" data-report-click="{&quot;spm&quot;:&quot;1001.2101.3001.7020&quot;,&quot;dest&quot;:&quot;https://so.csdn.net/so/search?q=Kafka&amp;spm=1001.2101.3001.7020&quot;,&quot;extra&quot;:&quot;{\&quot;searchword\&quot;:\&quot;Kafka\&quot;}&quot;}" data-tit="Kafka" data-pretit="kafka">Kafka</a></p></td><td style="width:148px;"><p id="u34074a2e">默认异步刷盘，可以通过修改参数变成同步刷盘</p></td><td style="width:368px;"><p id="ufeda9782">Broker配置</p><ol><li id="u680421af">log.flush.interval.messages：多少条消息刷盘一次</li><li id="u48659c6a">log.flush.interval.ms：刷盘间隔时间</li><li id="ub84e9033">log.flush.scheduler.interval.ms：周期性刷盘</li></ol></td></tr><tr><td><p id="ua0452f45"><a href="https://so.csdn.net/so/search?q=RabbitMQ&amp;spm=1001.2101.3001.7020" target="_blank" class="hl hl-1" data-report-click="{&quot;spm&quot;:&quot;1001.2101.3001.7020&quot;,&quot;dest&quot;:&quot;https://so.csdn.net/so/search?q=RabbitMQ&amp;spm=1001.2101.3001.7020&quot;,&quot;extra&quot;:&quot;{\&quot;searchword\&quot;:\&quot;RabbitMQ\&quot;}&quot;}" data-tit="RabbitMQ" data-pretit="rabbitmq">RabbitMQ</a></p></td><td style="width:148px;"><p id="u14990526">异步持久化：根据时间段批量将内存中的数据刷新到磁盘</p></td><td style="width:368px;"><ol><li id="u88f459dc">队列持久化：queue_declare创建队列时声明durable=true</li><li id="ud4461391">消息持久化：调用basic_publish方法时的属性delivery_mode=2。</li></ol></td></tr></tbody></table>