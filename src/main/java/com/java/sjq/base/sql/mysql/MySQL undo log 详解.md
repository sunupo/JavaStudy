[(110条消息) Undo log日志详解\_三3三的博客-CSDN博客](https://blog.csdn.net/LT11hka/article/details/125870981)

## 今天和大家分享一下Undo log日志的知识点

### 1.什么是undo log？

> 撤销日志，在[数据库事务](https://so.csdn.net/so/search?q=%E6%95%B0%E6%8D%AE%E5%BA%93%E4%BA%8B%E5%8A%A1&spm=1001.2101.3001.7020)开始之前，MYSQL会去记录更新前的数据到undo log文件中。如果事务回滚或者数据库崩溃时，可以利用undo log日志中记录的日志信息进行回退。同时也可以提供多版本并发控制下的读(MVCC)。（[具体的MVCC实现机制详解看这篇博客](https://blog.csdn.net/LT11hka/article/details/122260034?spm=1001.2014.3001.5502)）

### 2.undo log生命周期

> **undo log产生：** 在事务开始之前生成  
> **undo log销毁：** 当事务提交之后，undo log并不能立马被删除，而是放入待清理的链表，由purge线程判断是否由其他事务在使用undo段中表的上一个事务之前的版本信息，决定是否可以清理undo log的日志空间。  
> **注意：** undo log也会生产redo log，undo log也要实现持久性保护。

### 3\. uodo log日志的作用

> 首先简单说一下undolog 和redo log的区别  
> undo log是逻辑日志，实现事务的原子性  
>   undo log记录的是事务\[开始前\]的数据状态，记录的是更新之前的值  
>   undo log实现事务的原子性(提供回滚)  
> redo log是物理日志，实现事务的持久性  
>   redo log记录的是事务\[完成后\]的数据状态，记录的是更新之后的值  
>   redo log实现事务的持久性(保证数据的完整性)

1.undo log日志可以实现事务的回滚操作  
我们在进行数据更新操作的时候，不仅会记录redo log，还会记录undo log，如果因为某些原因导致[事务回滚](https://so.csdn.net/so/search?q=%E4%BA%8B%E5%8A%A1%E5%9B%9E%E6%BB%9A&spm=1001.2101.3001.7020)，那么这个时候MySQL就要执行回滚（rollback）操作，利用undo log将数据恢复到事务开始之前的状态。

如我们执行下面一条删除语句：

```
delete from book where id = 1;
```

那么此时undo log会生成一条与之相反的insert 语句【反向操作的语句】，在需要进行事务回滚的时候，直接执行该条sql，可以将数据完整还原到修改前的数据，从而达到事务回滚的目的。

再比如我们执行一条update语句：

```
update book set name = "三国" where id = 1;   ---修改之前name=西游记
```

此时undo log会记录一条相反的update语句，如下：

```
update book set name = "西游记" where id = 1;
```

如果这个修改出现异常，可以使用undo log日志来实现回滚操作，以保证事务的一致性。

2.undo log实现多版本并发控制 MVCC  
[具体的MVCC实现机制详解看这篇博客](https://blog.csdn.net/LT11hka/article/details/122260034?spm=1001.2014.3001.5502)

### 4\. uodo log的工作原理

![在这里插入图片描述](https://img-blog.csdnimg.cn/bbd9b47cd66648cd99c3ce54090ba69e.png)

> 如上图所示：  
> 当事务A进行一个update操作，将id=1修改成id=2。首先会修改buffer pool中的缓存数据，同时会将旧数据备份到undo log buffer中，记录的是还原操作的sql语句。此时如果事务B要查询修改的数据，但是事务A还没有提交，那么事务B就会从undo log buffer中，查询到事务A修改之前的数据，也就是id=1。此时undo log buffer会将数据持久化到undo log日志中(落盘操作)。undo日志持久化之后，才会将数据真正写入磁盘中，也就是写入ibd的文件中。最后才会执行事务的提交。

### 5\. uodo log的存储机制

> ![在这里插入图片描述](https://img-blog.csdnimg.cn/dd4e0aefabbc4bcfbd2f2e31f1c595db.png)  
> 具体参数存储形式如上图所示

### 6\. uodo log的配置参数

> innodb\_max\_undo\_log\_size: undo日志文件的最大值，默认1GB，初始化大小10M  
> innodb\_undo\_log\_truncate: 标识是否开启自动收缩undo log表空间的操作  
> innodb\_undo\_tablespaces: 设置独立表空间的个数，默认为0，标识不开启独立表空间，undo日志保存在ibdata1中  
> innodb\_undo\_directory: undo日志存储的目录位置  
> innodb\_undo\_logs: 回滚的个数 默认128