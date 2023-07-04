## [kafka消费者的三种模式（最多/最少/恰好消费一次）](https://baijiahao.baidu.com/s?id=1647192820875012954&wfr=spider&for=pc)
几种不同的注册方式

subscribe方式：当主题分区数量变化或者consumer数量变化时，会进行rebalance；注册rebalance监听器，可以手动管理offset不注册监听器，kafka自动管理assign方式：手动将consumer与partition进行对应，kafka不会进行rebanlance

![](https://pics0.baidu.com/feed/cc11728b4710b912693f2c446711680690452284.jpeg@f_auto?token=24ccd04789daac0242e4555125bb9a09&s=4B358042DCB095B59F71FC9A0000D091)

关键配置及含义
```

enable.auto.commit 是否自动提交自己的offset值；默认值时true

auto.commit.interval.ms 自动提交时长间隔；默认值时5000 ms

consumer.commitSync(); offset提交命令；
```

### 默认配置

采用默认配置情况下，既不能完全保证At-least-once 也不能完全保证at-most-once；

比如：

在自动提交之后，数据消费流程失败，这样就会有丢失，不能保证at-least-once；

数据消费成功，但是自动提交失败，可能会导致重复消费，这样也不能保证at-most-once；

但是将自动提交时长设置得足够小，则可以最大限度地保证at-most-once；

### at most once模式

基本思想是保证每一条消息commit成功之后，再进行消费处理；

设置自动提交为false，接收到消息之后，首先commit，然后再进行消费

### at least once模式

基本思想是保证每一条消息处理成功之后，再进行commit；

设置自动提交为false；消息处理成功之后，手动进行commit；

采用这种模式时，最好保证消费**操作的“幂等性”，防止重复消费；**

### exactly once模式

核心思想是将offset作为唯一id与消息同时处理，并且保证处理的原子性；

设置自动提交为false；消息处理成功之后再提交；

比如对于关系型数据库来说，可以将**id设置为消息处理结果的唯一索引**，再次处理时，如果发现该索引已经存在，那么就不处理；