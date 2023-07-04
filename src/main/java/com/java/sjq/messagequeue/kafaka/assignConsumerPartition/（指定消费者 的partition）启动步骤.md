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
   观察到有两条消息，在两个主题被生产
4. 运行[Consumer.java](Consumer.java)
   consumer订阅了两个topic。结果观察到，两个topic的消息都能被消费
5. 重启运行[Consumer.java](Consumer.java)
   因为 producer
结论：kafka 可以订阅多个 topic
```java
//订阅主题
Collection<String> topics= new ArrayList<>();
topics.add(TOPIC1);
topics.add(TOPIC2);
consumer.subscribe(topics);
```

下面这样只有TOPIC1 被订阅
```java
consumer.subscribe(Collections.singletonList(TOPIC2));
consumer.subscribe(Collections.singletonList(TOPIC1));
```