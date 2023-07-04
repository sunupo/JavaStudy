
# kafka
## offset的维护
offset 是跟随分区 partition 的，每个分区 partition 分别对应 offset。

由于Consumer在消费过程中可能会出现断电宕机等故障，Consumer恢复后，需要从故障前的位置继续消费，所以Consumer需要实时记录自己消费到哪个位置，以便故障恢复后继续消费。

从0.9版本开始，Consumer默认将offset保存在Kafka一个内置的名字叫_consumeroffsets的topic中。默认是无法读取的，可以通过设置consumer.properties中的exclude.internal.topics=false来读取。

`_consumerOffsets` 这个 topic 也用于 kafka 事务：[rocket 与 kafka 的区别（事务）.md](rocket%20%E4%B8%8E%20kafka%20%E7%9A%84%E5%8C%BA%E5%88%AB%EF%BC%88%E4%BA%8B%E5%8A%A1%EF%BC%89.md)

————————————————
版权声明：本文为CSDN博主「wangfy_」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/chushoufengli/article/details/114662193