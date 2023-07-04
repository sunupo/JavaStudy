1. auto_client
consumer回调抛出异常，broker 会重发消息到consumer，重发次数由 `redeliverPolicy.setMaximumRedeliveries(xx);`设置

2. client_acknowledge
msg.acknowledge() 会回复之前所有消息都已经正确接受，导致之前出错的消息不能正确的被执行。

3.

0.

4. individual_acknowledge
与 client_acknowledge 相比，出错的消息会重试到设置的次数，然后再继续执行下一条消息