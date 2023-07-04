[(122条消息) ActiveMQ入门 (七) 死信队列\_activemq 死信队列\_TechHao-Plus的博客-CSDN博客](https://blog.csdn.net/weixin_42639673/article/details/124303637)

## [ActiveMQ](https://so.csdn.net/so/search?q=ActiveMQ&spm=1001.2101.3001.7020)入门 (七) 死信队列

## 一、ActiveMQ死信队列设置

## 1.消息重发的情况

A transacted session is used and rollback() is called.  
A transacted session is closed before commit is called.  
A session is using CLIENT\_ACKNOWLEDGE and Session.recover() is called.

```
当一个消息被重发超过6(缺省为6次)次数时，会给broker发送一个"Poison ack"，这个消息被认为是a
poison pill，这时broker会将这个消息发送到死信队列，以便后续处理。
```

注意三点：  
1）缺省持久消息过期，会被送到DLQ，非持久消息不会送到DLQ  
2）缺省的死信队列是ActiveMQ.DLQ，如果没有特别指定，死信都会被发送到这个队列。  
3）可以通过配置文件(activemq.xml)来调整死信发送策略。

## 二、死信队列配置

## 1.为每个队列建立独立的死信队列

修改activeMQ.xml

```
<destinationPolicy>
<policyMap>
<policyEntries>
<policyEntry queue=">">
<deadLetterStrategy>
<individualDeadLetterStrategy queuePrefix="DLQ."
useQueueForQueueMessages="true" />
</deadLetterStrategy>
</policyEntry>

<policyEntry topic=">" >
<pendingMessageLimitStrategy>
<constantPendingMessageLimitStrategy limit="1000"/>
</pendingMessageLimitStrategy>
</policyEntry>
</policyEntries>
</policyMap>
</destinationPolicy>
```

## 三、RedeliveryPolicy重发策略设置

修改启动类  
![在这里插入图片描述](https://img-blog.csdnimg.cn/9831530ca6664243b4e5586cf4bccad1.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAd2VpeGluXzQyNjM5Njcz,size_20,color_FFFFFF,t_70,g_se,x_16)