
[(122条消息) ActiveMQ入门 (八)使用常见问题/面试题\_activitymq突然宕机\_TechHao-Plus的博客-CSDN博客](https://blog.csdn.net/weixin_42639673/article/details/124338988)

### 文章目录

-   [一、ActiveMQ宕机了怎么办？](https://blog.csdn.net/weixin_42639673/article/details/124338988#ActiveMQ_4)
-   [二、如何防止消费方消息重复消费？](https://blog.csdn.net/weixin_42639673/article/details/124338988#_9)
-   -   [1.数据库操作](https://blog.csdn.net/weixin_42639673/article/details/124338988#1_10)
-   [2.非数据库操作](https://blog.csdn.net/weixin_42639673/article/details/124338988#2_13)
-   [三、如何防止消费方消息重复消费？](https://blog.csdn.net/weixin_42639673/article/details/124338988#_17)

___

## 一、[ActiveMQ](https://so.csdn.net/so/search?q=ActiveMQ&spm=1001.2101.3001.7020)宕机了怎么办？

Zookeeper集群+ Replicated LevelDB + ActiveMQ集群  
(目前只看了一遍视频，没有动手操作，具体等遇到问题再看)

## 二、如何防止消费方消息重复消费？

## 1.数据库操作

把消息的ID作为表的唯一主键，这样在重试的情况下，会触发主键冲突，从而避免数据出现[脏数据](https://so.csdn.net/so/search?q=%E8%84%8F%E6%95%B0%E6%8D%AE&spm=1001.2101.3001.7020)。

## 2.非数据库操作

可以借助第三方的应用，例如Redis，来记录消费记录。每次消息被消费完成时候，把当前消息的ID作为key存入redis，每次消费前，先到redis查询有没有该消息的消  
费记录。

## 三、如何防止消费方消息重复消费？

1）在消息生产者和消费者使用事务  
2）在消费方采用手动消息确认（ACK）  
3）消息持久化，例如JDBC或日志