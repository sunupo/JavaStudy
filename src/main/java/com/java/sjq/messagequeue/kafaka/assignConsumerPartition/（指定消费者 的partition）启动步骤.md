文件路径是这个：kafka_2.13-2.8.2 ，cmd 启动报错。原因：[java - The input line is too long when starting kafka - Stack Overflow](https://stackoverflow.com/questions/48834927/the-input-line-is-too-long-when-starting-kafka)
1. 启动 zoopkeeper
    ```shell
      D:\SoftWare\ZoopKeeper\apache-zookeeper-3.7.1-bin\bin\zkServer.cmd
    ```
2. 启动 kafka
    ```shell
     D:\SoftWare\messagequeue\kafka\bin\windows\kafka-server-start.bat D:\SoftWare\messagequeue\kafka\config\server.properties
    ```
3. 运行[Producer.java](Producer.java)
   观察到有两条消息，在两个topic被生产,并且所在的分区partition都是0。
   ```text
   RecordMetadata:	black-topic-0@18
   RecordMetadata:	black-topic-0@19
   
   RecordMetadata:	white-topic-0@18
   RecordMetadata:	white-topic-0@19
   ```
4. 运行[Consumer1.java](Consumer1.java)
   consumer1订阅了两个topic,每个partition指定了分区partition=0。 结果观察到，两个topic的消息都能被消费
5. 重启运行[Producer.java](Producer.java)
   重新审生产消息。观察到有两条消息，在两个topic被生产,并且所在的分区partition都是0。
6. 运行[Consumer2.java](Consumer2.java)
   consumer1订阅了两个topic,topic1指定了分区partition=0，topic2指定了分区partition=1。 结果观察到，只有topic1的消息能被消费。 
因为topic2的消息在分区partition=0，但是这个分区我们没有订阅。

结论：
kafka 可以给消费者consumer指定assign消费的 partition，但是这样就失去了 subscribe 提供的 Rebalance。
