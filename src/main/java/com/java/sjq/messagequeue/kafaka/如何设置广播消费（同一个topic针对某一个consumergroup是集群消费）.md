[(125条消息) Kafka集群消费和广播消费的实现\_kafka广播消费\_WannaRunning的博客-CSDN博客](https://blog.csdn.net/qq_29569183/article/details/122109725)

### 集群消费
**默认情况**下Kafka中同一个topic下的消息对于某一个消费组来说是集群消费模式,也就是只会被组内一个消费实例所消费。

### 广播消费
同一个topic下的消息被多个消费者消费称为广播消费.由于Kafka默认是集群消费模式,所以广播消费的实现方式就是为广播消费的多个应用实例都设置不同的GroupId即每个实例都是单独的消费组.
————————————————
版权声明：本文为CSDN博主「WannaRunning」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_29569183/article/details/122109725

