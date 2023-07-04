[Redis数据丢失问题 - 简书](http://events.jianshu.io/p/6d775284606d)

## Redis数据丢失问题

[![](https://upload.jianshu.io/users/upload_avatars/17502375/ac479da9-5a01-4e79-a453-b1cd77252159?imageMogr2/auto-orient/strip|imageView2/1/w/96/h/96/format/webp)](http://events.jianshu.io/u/643a6b684d57)

0.6572021.09.07 01:35:49字数 854阅读 4,231

## 一、两种数据丢失的情况

主备切换的过程中(**`异步复制，脑裂`**)，可能会导致数据丢失

### 1.1 异步复制导致的数据丢失

因为`master -> slave的复制是异步`的（客户端发送给redis，主节点数据同步到内存中后就返回成功了）  
所以可能有部分数据还没复制到slave，master就宕机了，此时master内存中的数据也没了，这些部分数据就丢失了。

### 1.2 脑裂导致的数据丢失

脑裂，也就是说，某个master所在机器突然脱离了正常的网络，跟其他slave机器不能连接，但是实际上master还运行着（这种分布式中间件脑裂是常规操作,类似的，比如[rabiitmq脑裂](https://www.jianshu.com/p/e426f3a6aa11)）

![](http://upload-images.jianshu.io/upload_images/17502375-1163cf2d7388fb1d.png)

此时哨兵可能就会认为master宕机了，然后开启选举，将其他slave切换成了master  
这个时候，集群里就会有两个master，也就是所谓的脑裂

此时虽然某个slave被切换成了master，但是可能client还没来得及切换到新的master，还继续写向旧master的数据，然后旧master再次恢复的时候，会被作为一个slave挂到新的master上去，自己的数据会清空，重新从新的master复制数据，就导致了我们之前在脑裂时候向旧master写的数据全部都丢失了。

## 二 如何解决redis数据丢失的问题

解决以上两种情况redis数据丢失的问题`都是靠`以下两个参数配置将数据损失降到最低。  
`min-slaves-to-write x`  
`min-slaves-max-lag y`  
(要求y秒内至少有x个slave同步接收到这个数据,比如x=1，y=10)

### 2.1减少异步复制的数据丢失

有了min-slaves-max-lag这个配置，就可以确保说，一旦slave复制数据和ack延时太长，就`认为`可能master宕机后`损失`的数据`太多`了，那么就`拒绝`新的`写`请求，这样可以把master`宕机时`由于部分数据未同步到slave导致的数据丢失的损失`降低的可控范围内`，但是[仅有一个从库要谨慎设置1,只有一个从库且要去维护的时候,请先设置 最少写从库的个数为0,再去维护从库](https://links.jianshu.com/go?to=https%3A%2F%2Fblog.csdn.net%2Fu011944141%2Farticle%2Fdetails%2F93775639)

![](http://upload-images.jianshu.io/upload_images/17502375-b25dd06afbd62ce5.png)

比如如上图所示，我们如果发现redis slave结点数据同步延迟时间太长，我们就任务主节点挤压了很多数据没有同步，这时候如果宕机的话，redis要丢失很多数据，因此我们先停止新的写入，防止宕机时候丢失的数据更多，于此同时全力进行数据同步，当然我们可以在延迟很高的时候呢做限流降级，也可以把数据丢到mq里，每隔一段时间进行一次消费给他重新回流到redis的机会

### 2.2 减少脑裂的数据丢失

如果一个master出现了脑裂，跟其他slave丢了连接，那么上面两个配置可以确保说，如果不能继续给指定数量的slave发送数据，而且slave超过10秒没有给自己ack消息，那么就直接拒绝客户端的写请求

这样脑裂后的**旧master就不会接受client的新数据**，也就避免了更多的数据丢失

上面的配置就确保了，如果跟任何一个slave（配置的x为所有从结点的数量）丢了连接，在10秒后发现没有slave给自己ack，那么就拒绝新的写请求

因此在脑裂场景下，最多就丢失10秒的数据。

![](http://upload-images.jianshu.io/upload_images/17502375-a036d6f88ccf5e2f.png)

上面两个参数保证了发生脑裂后多长时间停止新的写入，让我们数据丢失的损失降低到最少，这里脑裂状态持续的越久就会丢失越久的数据，因为他重启后会变成从结点，所有数据同步于新的master，原来的数据都丢了

更多精彩内容，就在简书APP

![](http://upload.jianshu.io/images/js-qrc.png)

"喜欢的点点关注，点点赞哟?"

还没有人赞赏，支持一下

[![  ](https://upload.jianshu.io/users/upload_avatars/17502375/ac479da9-5a01-4e79-a453-b1cd77252159?imageMogr2/auto-orient/strip|imageView2/1/w/100/h/100/format/webp)](http://events.jianshu.io/u/643a6b684d57)

[名字是乱打的](http://events.jianshu.io/u/643a6b684d57 "名字是乱打的")[![  ](https://upload.jianshu.io/user_badge/19c2bea4-c7f7-467f-a032-4fed9acbc55d)](https://www.jianshu.com/mobile/creator)你只管努力,只管向前,命运会给你最好的安排

总资产120共写了45.2W字获得1,236个赞共300个粉丝

-   我被篮球砸伤了脚踝，被送到医院后，竟然遇到我老公全家，陪另一个女人产检。 平时对我的冷言冷语的小姑子吴彤彤挽着小三...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 18375评论 0赞 9

-   我是他体贴入微，乖巧懂事的金丝雀。 上位后，当着满座宾朋，我播放了他曾经囚禁、霸凌我，还逼疯了我妈妈视频。 1 我...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 5893评论 0赞 2

-   一、 面基当天，我把谈了三个月的网恋对象拉黑了。 事情是这样的。 那天我刚上完早课，叼了个冰棒走在回家的路上，就听...

-   被退婚后，我嫁给了当朝新帝。 本只想在后宫低调度日，却不料新帝是个恋爱脑。 放着家世优越的皇后和貌美如花的贵妃不要...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 2446评论 0赞 30

-   央央一时 我的男朋友，是个满脑子只有研究的物理系教授。 末世爆发，他变成了丧尸，别的丧尸，一个劲的咬人，而他，一个...

-   正文 我爸妈意外去世后，我才知道，我表妹才是他们的亲女儿。 我只是从福利院抱来，替她挡灾的。 我表妹凌楚，接管了他...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 991评论 0赞 0

-   正文 我心血来潮检查老公的手机，却意外看到他的聊天记录，“你还爱我对吗？”“昨晚的事你就忘了吧。”虽然只有短短两句...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 2691评论 0赞 0

-   正文 出嫁四月，我的夫君带回他的心头好白月光。将她抬为平妻，从此与我平起平坐。 我强忍心痛转移注意力，最后偶然发现...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 453评论 0赞 0

-   正文 被顾辰扔到床上的时候，我才参加完一个活动，保姆车刚刚离开，顾辰就敲响了我的房门。 我叫林星，是一个比较出名的...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 585评论 0赞 3

-   我吃了整整三年的避子药，因为怀过皇嗣的妃嫔都死了，无一例外。三年前帝后大婚，洞房花烛的第二天早上我就毅然决然饮下了...

-   我跟我前夫在商场里面给小孩买衣服，他现在的老婆不停地给他打电话。 前夫没办法，只能撒谎说自己在公司开会。 对方听清...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 5834评论 0赞 1

-   好不容易攻略到头，逮住机会和反派同归于尽。 系统告诉我让我攻略没让我死？ 重开一局，攻略对象变成了上一世跟我一起炸...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 1378评论 0赞 1

-   人人都说尚书府的草包嫡子修了几辈子的福气，才能尚了最受宠的昭宁公主。 只可惜公主虽容貌倾城，却性情淡漠，不敬公婆，...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 1496评论 1赞 5

-   正文 那天我在老公的包里发现了两个避孕套，他说是社区工作人员宣传防范艾滋病派发的。 我当然不信，那包装明显是便利店...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 5288评论 0赞 12

-   正文 太子周落回不近女色，只钟情于太子少傅江景，而甄爽身为将门虎女也深深迷恋江景。无奈皇命难为，一道圣旨成功地把甄...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 1679评论 0赞 0

-   作者：猫打滚儿 文案： 八岁那年，我被我爸当成诬陷村长的诱饵，我最崇拜的叔叔是他的同谋，那夜之后，这三个男人都失踪...

-   皇帝的白月光自尽了，我却瞧着他一点也不伤心。 那日我去见贵妃最后一面，却惊觉她同我长得十分相似。 回宫后我的头就开...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 1334评论 0赞 1

-   谁能想到有朝一日，逼宫这种事会发生在我身边。 被逼走的是我亲妈，始作俑者是我亲小姨。 为了争得我的抚养权，母亲放弃...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 1584评论 0赞 2

-   正文 我的男朋友，被我相处了五年的好闺蜜抢走了。 把他让给我吧，我求你了。」 随后她把高领毛衣往下一扯，漏出很明显...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 5813评论 0赞 16

-   我朋友是做直播的，人长得好看，公司也肯花力气捧。 轻轻松松半年，就在平台上攒了100多万粉丝。 而就在昨天开播的时...

    [![](https://upload.jianshu.io/users/upload_avatars/4790772/388e473c-fe2f-40e0-9301-e357ae8f1b41.jpeg)茶点故事](http://events.jianshu.io/u/0f438ff0a55f)阅读 8925评论 1赞 16


### 被以下专题收入，发现更多相似内容

### 推荐阅读[更多精彩内容](http://events.jianshu.io/)

-   课程大纲 1、两种数据丢失的情况 2、解决异步复制和脑裂导致的数据丢失 ---------------------...

-   课程大纲 1、两种数据丢失的情况2、解决异步复制和脑裂导致的数据丢失 1、两种数据丢失的情况 主备切换的过程，可能...

    [![](https://cdn2.jianshu.io/assets/default_avatar/3-9a2bcc21a5d89e21dafc73b39dc5f582.jpg)浪白条](http://events.jianshu.io/u/587b74b4c8f3)阅读 300评论 0赞 0

-   异步复制导致的数据丢失： 因为master->slave的复制是异步的，所有有可能部分数据还没复制到slave,m...

    [![](https://upload.jianshu.io/users/upload_avatars/2853934/ce97aceb-336a-46f8-b49e-0a574820f9d9.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/48/h/48/format/webp)小李一枚](http://events.jianshu.io/u/7745cf6c9e68)阅读 1,138评论 0赞 0

-   1 数据丢失情况 1.1 异步复制导致的数据丢失 因为master -> slave的复制是异步的，所以可能有部分...

-   一、两种数据丢失的情况 主备切换的过程，可能会导致数据丢失 （1）异步复制导致的数据丢失 因为master -> ...



# [Redis主从集群切换数据丢失问题如何应对？](https://baijiahao.baidu.com/s?id=1724995402384917747&wfr=spider&for=pc)

[![到百度首页](https://mbdp01.bdstatic.com/static/landing-pc/img/logo_top.79fdb8c2.png "到百度首页")](https://www.baidu.com/)

[![](https://gips0.baidu.com/it/u=3883188608,879118020&fm=3012&app=3012&autime=1677151027&size=b200,200)](https://author.baidu.com/home?from=bjh_article&app_id=1570162957820137)

关注

-   异步复制同步丢失


-   集群产生脑裂数据丢失


### 1.异步复制丢失

对于Redis主节点与从节点之间的数据复制，是异步复制的，当客户端发送写请求给master节点的时候，客户端会返回OK，然后同步到各个slave节点中。

如果此时master还没来得及同步给slave节点时发生宕机，那么master内存中的数据会丢失；

要是master中开启持久化设置数据可不可以保证不丢失呢？答案是否定的。在master 发生宕机后，sentinel集群检测到master发生故障，重新选举新的master，如果旧的master在故障恢复后重启，那么此时它需要同步新master的数据，此时新的master的数据是空的（假设这段时间中没有数据写入）。那么旧master中的数据就会被刷新掉，此时数据还是会丢失。

### 2.集群产生脑裂

首先我们需要理解集群的脑裂现象，这就好比一个人有两个大脑，那么到底受谁来控制呢？在分布式集群中，分布式协作框架zookeeper很好的解决了这个问题，通过控制半数以上的机器来解决。

那么在Redis中，集群脑裂产生数据丢失的现象是怎么样的呢？

假设我们有一个redis集群，正常情况下client会向master发送请求，然后同步到salve，sentinel集群监控着集群，在集群发生故障时进行自动故障转移。

![](https://pics5.baidu.com/feed/8ad4b31c8701a18b7b32740941bfe1012938fe32.jpeg@f_auto?token=64471869266d14ec6ff17b483f1e2870)

此时，由于某种原因，比如网络原因，集群出现了分区，master与slave节点之间断开了联系，sentinel监控到一段时间没有联系认为master故障，然后重新选举，将slave切换为新的master。

但是master可能并没有发生故障，只是网络产生分区，此时client任然在旧的master上写数据，而新的master中没有数据，如果不及时发现问题进行处理可能旧的master中堆积大量数据。在发现问题之后，旧的master降为slave同步新的master数据，那么之前的数据被刷新掉，大量数据丢失。

![](https://pics7.baidu.com/feed/72f082025aafa40f534fe4157ff4e5467af01982.jpeg@f_auto?token=66e8c0ccd956aabf4e9b50e65a6398f8)

在了解了上面的两种数据丢失场景后，我们如何保证数据可以不丢失呢？在分布式系统中，衡量一个系统的可用性，我们一般情况下会说4个9,5个9的系统达到了高可用（99.99%，99.999%，据说淘宝是5个9）。对于redis集群，我们不可能保证数据完全不丢失，只能做到使得尽量少的数据丢失。

### 二、如何保证尽量少的数据丢失？

```
min-slaves-to-write 1min-slaves-max-lag 10
```

min-slaves-to-write默认情况下是0，min-slaves-max-lag默认情况下是10。

以上面配置为例，这两个参数表示至少有1个salve的与master的同步复制延迟不能超过10s，一旦所有的slave复制和同步的延迟达到了10s，那么此时master就不会接受任何请求。

我们可以减小min-slaves-max-lag参数的值，这样就可以避免在发生故障时大量的数据丢失，一旦发现延迟超过了该值就不会往master中写入数据。

那么对于client，我们可以采取降级措施，将数据暂时写入本地缓存和磁盘中，在一段时间后重新写入master来保证数据不丢失；也可以将数据写入kafka消息队列，隔一段时间去消费kafka中的数据。

通过上面两个参数的设置我们尽可能的减少数据的丢失，具体的值还需要在特定的环境下进行测试设置。
