[mysql 悬挂事务问题|mysql\_新浪新闻](http://k.sina.com.cn/article_1939498534_739a6626019016ea2.html)

从前面的实践篇章节中，我们很容易知道两个事务要操作相同行的数据会产生锁等待的情况。那么是不是意味着上面的代码只会影响到自己事务里面的表呢？现在假设上面的代码只会用到A表，那么是不是同一数据库中的其他的B、C、D表是不是不受影响呢。先揭晓答案：会受到影响，B、C、D表的数据行也会被锁。这是为啥？

首先介绍一下Spring的事务的实现机制。

![](http://k.sina.com.cn//n.sinaimg.cn/sinakd20220916s/666/w1080h386/20220916/35fe-eb0fe14ff7c1736502b40b2a7c0e80cf.png)

Spring事务是如何保证iBatis执行sql时，这些sql用的是相同的Connection？答案是：ThreadLocal。在执行完doBegin方法后，其实是通过bindResouce方法将从DruidDataSource连接池中获得的链接放入当前线程的TheadLocal,这里的TheadLocal中存放的是一个Map, key是dataSouce，value是connectionHolder（connectionHolder中持有Connection的引用。近似认为connectionHolder和Connection是一回事）。

![](http://k.sina.com.cn//n.sinaimg.cn/sinakd20220916s/767/w1080h487/20220916/e444-ec90e46f4bcf3529e3e8051408e1f0b1.png)

IBatis在执行sql时，通过DataSourceUtils.getConnection获取数据库链接。这里会优先从当前线程的ThreadLocal中获取，如果获取不到，从数据源中获取。

![](http://k.sina.com.cn//n.sinaimg.cn/sinakd20220916s/45/w1080h565/20220916/62f0-82fd15ba0c684cd43274986b0e9abd17.png)

ThreadLocal中的变量什么时候会被清除呢？当commit和rollback的时候，ThreadLocal中的变量会被清理掉。

![](http://k.sina.com.cn//n.sinaimg.cn/sinakd20220916s/669/w1080h389/20220916/6496-b46ac425ba9624fadd6bc5a69ac3d195.png)

![](http://k.sina.com.cn//n.sinaimg.cn/sinakd20220916s/498/w1080h218/20220916/f5a2-eadf3e2cf60d3582f6c8ba2bc1205062.png)

<iframe adtypeturning="false" width="300px" height="250px" frameborder="0" marginwidth="0" marginheight="0" vspace="0" hspace="0" allowtransparency="true" scrolling="no" sandbox="allow-popups allow-same-origin allow-scripts allow-top-navigation-by-user-activation" src="javascript:'<html><body style=background:transparent;></body></html>'" id="sinaadtk_sandbox_id_8" style="float:left;" name="sinaadtk_sandbox_id_8"></iframe>

从上面的分析过程中，可以看出，当事务没有被commit和rollback的时候，当前线程可能会有上次残留的ThreadLocal的。因为当前线程是从线程池中获取的，线程是会被复用的。如果当前线程之前执行的事务没有被正确commit或者rollback的话，现在继续要获取链接并执行sql，由于上次是开启了事务且未提交，这次的sql也会被认为进入事务，这些sql也会锁住相应的数据行，这样就造成数据库中大面积的表被锁。

总结

1.尽量不使用getTransaction这种人工控制事务（这种方式比较容易埋坑，推荐使用@Transactional ），如果要使用，请务必要try catch。一定注意提前return的问题（由于提前return导致rollback和commit都没被执行，这种case也很常见）。否则万一出问题，可能真的很头大；

2.参数校验一定要严谨，任何类型转化的地方不做类型检查可能都会产生异常；