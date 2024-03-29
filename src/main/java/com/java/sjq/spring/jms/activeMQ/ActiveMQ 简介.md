
- 哪些情况下会发生消息的重复消费呢？其实就是客户端消息签收失败的情况下，这包括但不限于以下情况：
-  1.消费者端开启事务，但最终事务回滚而未提交，或者在提交之前关闭了连接而提交失败
-  2.需要手动签收(CLENT_ACKNOWLEDGE)的消息，消费者端在签收之后又调用了 session.recover();


消息什么时候会被重新传递

1、在一个事物性会话中，调用了 rollback()；

2、在调用 commit()之前，事务已经关闭 close()

3、回话正在使用 ACK 确认(即手动手动签收 CLIENT_ACKNOWLEDGE)时，Session.recover()被调用

4、客户端连接超时（可能正在执行的业务代码所需要的时间比配置超时时间要长）


防止重复调用引发的问题
-  ActiveMQ中的消息有时是会被重复消费的，而我们消费消息时大都会在拿到消息后去调用其他的方法，比如说将消息的内容解析为一个对象保存到数据库中。一旦发生消息的重复消费时就会重复保存，这是有问题的，因此我们需要考虑如何防止重复调用。其实我们是没有办法防止重复调用的，只能在重复调用时进行消息是否重复消费的校验，当然对于幂等性接口也可以不进行校验。
-  那如何进行校验呢？有很多种方式，比如说我们将消费过的消息的messageId保存到数据库，每次消费消息前先到数据库中查一下该消息是否已被消费。在分布式系统中，也可以将消费过的消息放入redis中，以messageId作为key，message对象作为value(其实value不重要，当然也要看需求本身)，在消费消息时先从redis中查找该消息是否已被消费。
