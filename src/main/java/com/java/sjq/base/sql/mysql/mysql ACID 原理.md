[(134条消息) MYSQL之redolog、undolog、binlog以及MVCC原理\_mysql undolog redolog mvcc\_一切是糊涂的博客-CSDN博客](https://blog.csdn.net/qq_40223516/article/details/126024836)
首先谈一下mysql的4大特性，也是事务的前置特性。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/a5130385e7264965b65d6980176941bb.png)  
原子性由undolog保证，隔离性是由锁和mvcc保证，持久性由redolog保证；一致性则是前面三个保证的。  
这里要区别一下binlog，binlog是再sqlserver级别的，而undolog与redolog是再innoDB级别的。