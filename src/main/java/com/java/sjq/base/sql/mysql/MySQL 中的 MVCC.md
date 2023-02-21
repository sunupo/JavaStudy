[InnoDB的MVCC如何解决不可重复读和快照读的幻读,当前读用next-key解决幻读](https://blog.csdn.net/luzhensmart/article/details/88134189)

- 在快照读读情况下，mysql通过mvcc来避免幻读。（因为 RR隔离级别下，除了在当前实务中修改数据，否则只会建立一次快照，于是不会出现幻读）
- 在当前读读情况下，mysql通过next-key来避免幻读。（因为当前读每次操作最新的数据，最新的数据可能是被修改过的，于是会出现幻读。于是需要加上 next-key 避免其他事务修改，从而避免幻读）



[mysql如何解决幻读 - 孙龙-程序员 - 博客园](https://www.cnblogs.com/sunlong88/p/16668992.html)

总结下 MySQL 解决幻读的手段：

隔离级别：可重复读RR

- 快照读 MVCC + 当前读 Next-Lock Key(只在可重复读隔离级别下生效)

隔离级别：SERIALIZABLE

- 在这个隔离级别下，事务在读操作时，先加表级别的共享锁，直到事务结束才释放；事务在写操作时，先加表级别的排它锁，直到事务结束才释放。也就是说，[串行化](https://www.zhihu.com/search?q=串行化&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A2256579614})锁定了整张表，幻读不存在的



[数据库系统原理 | CS-Notes](http://www.cyc2018.xyz/%E6%95%B0%E6%8D%AE%E5%BA%93/%E6%95%B0%E6%8D%AE%E5%BA%93%E7%B3%BB%E7%BB%9F%E5%8E%9F%E7%90%86.html#%E4%BA%94%E3%80%81%E5%A4%9A%E7%89%88%E6%9C%AC%E5%B9%B6%E5%8F%91%E6%8E%A7%E5%88%B6)

# 基本思想

MVCC 利用了多版本的思想，写操作更新最新的版本快照，而读操作去读旧版本快照，没有互斥关系。



Next-Key Locks 是 MySQL 的 InnoDB 存储引擎的一种锁实现。

MVCC 不能解决幻影读问题，Next-Key Locks 就是为了解决这个问题而存在的。

在可重复读（REPEATABLE READ）隔离级别下，使用 MVCC + Next-Key Locks 可以解决幻读问题。





![img](MySQL%20%E4%B8%AD%E7%9A%84%20MVCC.assets/b999a9014c086e0638ee7a398bd47afd0bd1cb67.jpeg)



[(113条消息) mysql的innodb RR隔离级别能解决幻读吗\_偷学紧箍咒的妖怪的博客-CSDN博客\_mysql的rr隔离级别 能解决幻读吗](https://blog.csdn.net/chang765721/article/details/125891721)

![](MySQL%20%E4%B8%AD%E7%9A%84%20MVCC.assets/original.png)

版权声明：本文为博主原创文章，遵循 [CC 4.0 BY-SA](http://creativecommons.org/licenses/by-sa/4.0/) 版权协议，转载请附上原文出处链接和本声明。

## mysql的innodb RR隔离级别能解决幻读吗

> 笔记：
>
> RR + next-key lock

先说结论：能解决，但不能完全解决。

在RR和RC级别下，数据库的读分为快照读和当前读:  
**快照读**：单纯的select操作。读取的是快照（ReadView）中的数据，可能是历史数据  
**当前读**：select … for update/in share mode、update、insert、delete。读取的总是当前的最新数据

**对于快照读**，是通过mvcc来解决事务中的脏读、不可重复读以及幻读问题的。  
实际上不管是RR还是RC，读取数据时根据undolog数据链的事务id和ReadView中各个参数（当前活跃的事务id集合，下一个未分配的事务id，活跃事务id中最小的事务id）对比的规则都是同一套，只是二者ReadView生成的时机不同。  
<u>对于RR隔离级别，ReadView是在事务读开始时候产生，在事务结束之前都是用这一个readView，因此无论怎么变化，看到的东西都不会变化。从而解决了脏读、不可重复读以及幻读问题。</u>  
<u>对于RC隔离级别，每次读取的时候都会产生一个readView，因此无法解决不可重复读以及幻读问题。</u>  
**因此：RR隔离级别下，快照读是可以解决脏读、不可重复读以及幻读问题的。**



为什么说间隙锁在RR隔离级别中才生效？

<u>对于RR隔离级别，ReadView是在事务读开始时候产生，在事务结束之前都是用这一个readView，因此无论怎么变化，看到的东西都不会变化。从而解决了脏读、不可重复读以及幻读问题。</u> 



**对于当前读**，是通过锁（记录锁，间隙锁等）来解决脏读、不可重复读以及幻读问题的，你读取的行，以及行的间隙都会被加锁，直到事务提交时才会释放，其他的事务无法进行修改，所以也不会出现不可重复读、幻读的情形。而间隙锁在RR隔离级别中才生效。  
**因此：RR隔离级别下，当前读也是可以解决脏读、不可重复读以及幻读问题的。**

那幻读是怎么产生的呢？为什么说RR隔离级别下没有完全解决幻读问题。  
**其实在RR级别下，有下面两种情况是可以产生幻读情况的。**  
1、事务1 先快照读，事务2新增了一条数据并提交事务，事务1再当前读。  
2、事务1 先快照读，事务2新增了一条数据并提交事务，事务1对事务2提交的数据进行了修改，事务1再次快照读。

情况1不用说了吧，很好理解。对于情况2， 事务1的更新操作不属于快照读，因此事务1的更新操作是可以生效的，而当前数据会记录最新修改的记录，最新修改的记录为当前事务自己，所以是能看到的。(结尾的情况2。 事务1先快照读，说明没有加 for update 或者 lock in share mode 加锁，于是就不会加上 next-key lock，事务2修改数据的时候发现没有加锁于是就可以修改。事务二修改提交之后事务1 能看到是正常的。mysql 解决幻读 是 MVCC 和 next-key -lock结合，少了 next-key-lock于是会出现幻读。)