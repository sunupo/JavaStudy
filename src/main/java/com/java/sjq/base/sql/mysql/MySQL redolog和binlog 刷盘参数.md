[ redolog和binlog 刷盘参数](https://blog.csdn.net/qq_43490312/article/details/125932864#:~:text=1%20%E8%AE%BE%E7%BD%AE%E4%B8%BA%200%20%E7%9A%84%E6%97%B6%E5%80%99%EF%BC%8C%E8%A1%A8%E7%A4%BA%E6%AF%8F%E6%AC%A1%E4%BA%8B%E5%8A%A1%E6%8F%90%E4%BA%A4%E6%97%B6%E9%83%BD%E5%8F%AA%E6%98%AF%E6%8A%8A%20redo%20log%20%E7%95%99%E5%9C%A8%20redo,%E8%AE%BE%E7%BD%AE%E4%B8%BA%202%20%E7%9A%84%E6%97%B6%E5%80%99%EF%BC%8C%E8%A1%A8%E7%A4%BA%E6%AF%8F%E6%AC%A1%E4%BA%8B%E5%8A%A1%E6%8F%90%E4%BA%A4%E6%97%B6%E9%83%BD%E5%8F%AA%E6%98%AF%E6%8A%8A%20redo%20log%20%E5%86%99%E5%88%B0%20page%20cache%E3%80%82)

## 涉及参数（都是事务提交后的动作参数）：

[redolog](https://so.csdn.net/so/search?q=redolog&spm=1001.2101.3001.7020)：innodb\_flush\_log\_at\_trx\_commit

[binlog](https://so.csdn.net/so/search?q=binlog&spm=1001.2101.3001.7020) ：sync\_binlog binlog\_group\_commit\_sync\_delay binlog\_group\_commit\_sync\_no\_delay\_count

主要分几个维度来分析和理解

## 写入机制

### binlog

未提交前，写入binlog cache，提交后，写入file cache（简称write），然后根据sync\_cache参数配置[fsync](https://so.csdn.net/so/search?q=fsync&spm=1001.2101.3001.7020)到磁盘的时机

### redolog

未提交前，写入log buffer，提交后，写入file cache（简称write），，根据redolog：innodb\_flush\_log\_at\_trx\_commit参数配置fsync到磁盘的时机

### binlog cache和log buffer区别

<u>binlog cache 是写binlog线程内存，是系统分配的和mysql进程内存无关</u>

log buffer 是使用的mysql进程占有的内存

### 未提交事务的redolog刷盘场景

-   后台线程每秒轮询，把logbuffer里面中的日志写到file cache，然后调用fsync刷盘
    
-   redlog buffer 占用空间为buffer pool一半的空间的时候，会把日志写入file cache中不刷盘
    
-   并行的事务提交，且redolog：innodb\_flush\_log\_at\_trx\_commit参数为1，则会写入file cache中并调用fsync刷盘
    

## 参数说明

binlog sync\_binlog

-   sync\_binlog=0 的时候，表示每次提交事务都只 write，不 fsync；
-   sync\_binlog=1 的时候，表示每次提交事务都会执行 fsync；
-   sync\_binlog=N(N>1) 的时候，表示每次提交事务都 write，但累积 N 个事务后才 fsync。

redolog：innodb\_flush\_log\_at\_trx\_commit

-   设置为 0 的时候，表示每次事务提交时都只是把 redo log 留在 redo log buffer 中 ;
-   设置为 1 的时候，表示每次事务提交时都将 redo log 直接持久化到磁盘；
-   设置为 2 的时候，表示每次事务提交时都只是把 redo log 写到 page cache。

### “双1”配置

通常我们说 MySQL 的“双 1”配置，指的就是 sync\_binlog 和 innodb\_flush\_log\_at\_trx\_commit 都设置成 1。也就是说，一个事务完整提交前，需要等待两次刷盘，一次是 redo log（prepare 阶段），一次是 binlog。

mysql可以根据redolog prepare状态的持久化的日志+binlog进行崩溃恢复的

## 

## binlog组提交参数

![在这里插入图片描述](https://img-blog.csdnimg.cn/a61de9d4a5e64e7d843e00a2a0ab82da.png)

参数：binlog\_group\_commit\_sync\_delay 和binlog\_group\_commit\_sync\_no\_delay\_count

-   binlog\_group\_commit\_sync\_delay 参数，表示延迟多少微秒后才调用 fsync;
-   binlog\_group\_commit\_sync\_no\_delay\_count 参数，表示累积多少次以后才调用 fsync。

这两个条件是或的关系，也就是说只要有一个满足条件就会调用 fsync。所以，当 binlog\_group\_commit\_sync\_delay 设置为 0 的时候，binlog\_group\_commit\_sync\_no\_delay\_count 也无效了。

## 丢失数据场景分析

1.  大促期间吗，设置的是sync\_binlog=0和innodb\_flush\_log\_at\_trx\_commit=2  
    在主机重启时，会有几种风险:

-   如果事务的binlog和redo log都还未fsync,则该事务数据丢失
-   如果事务binlog fsync成功,redo log未fsync,则该事务数据丢失。虽然binlog落盘成功,但是binlog没有恢复redo log的能力,所以redo log不能恢复.  
    不过后续可以解析binlog来恢复这部分数据
-   如果事务binlog fsync未成功,redo log成功。由于redo log恢复数据是在引擎层,所以重新启动数据库,redo log能恢复数据,但是不能恢复server层的binlog,则binlog丢失。如果该事务还未从filesystem page cache里发送给从库,那么主从就会出现不一致的情况

注意：filesystem page cache中的数据是可以读到的

2.  设置sync\_binlog = N，如果是**主机异常重启**，会丢失n条数据
3.  redolog：innodb\_flush\_log\_at\_trx\_commit 设置2，会丢失数据

## io瓶颈的优化

-   组提交，设置 binlog\_group\_commit\_sync\_delay 和 binlog\_group\_commit\_sync\_no\_delay\_count 参数，减少 binlog 的写盘次数。这个方法是基于“额外的故意等待”来实现的，因此可能会增加语句的响应时间，但没有丢失数据的风险。
-   将 sync\_binlog 设置为大于 1 的值（比较常见是 100~1000）。这样做的风险是，主机掉电时会丢 binlog 日志。
-   将 innodb\_flush\_log\_at\_trx\_commit 设置为 2。这样做的风险是，主机掉电的时候会丢数据。