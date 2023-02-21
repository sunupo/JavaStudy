[(110条消息) mysql的binlog开启方式,查看方式.三种binlog模式介绍.以及使用binlog恢复数据.删除binlog\_好大的月亮的博客-CSDN博客\_mysql 开启binlog](https://blog.csdn.net/weixin_43944305/article/details/108620849)

## 判断MySQL是否已经开启binlog

```
SHOW VARIABLES LIKE 'log_bin';
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020091614005832.png#pic_center)

**查看MySQL的binlog模式**

```
show global variables like "binlog%";
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916143255196.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzk0NDMwNQ==,size_16,color_FFFFFF,t_70#pic_center)

**几个关于binlog常用的命令**

```
#查看日志开启状态 
show variables like 'log_%';
#查看所有binlog日志列表
show master logs;
#查看最新一个binlog日志的编号名称，及其最后一个操作事件结束点 
show master status;
#刷新log日志，立刻产生一个新编号的binlog日志文件，跟重启一个效果 
flush logs;
#清空所有binlog日志 
reset master;
```

`因为binlog日志文件是二进制文件`，没法用vi等打开，这时就`需要mysql的自带的mysqlbinlog工具进行解码`  
demo  
`mysqlbinlog mysql-bin.000005`  
可以将二进制文件转为可阅读的sql语句。

## 如何开启binlog日志

修改`mysql`的`my.cnf`配置文件  
一般默认是在`/etc/my.cnf`路径下

简单开启`binlog` `demo`  
**在`mysqld`下添加第一种方式**

```
#第一种方式:
#开启binlog日志
log_bin=ON
#binlog日志的基本文件名
log_bin_basename=/var/lib/mysql/mysql-bin
#binlog文件的索引文件，管理所有binlog文件
log_bin_index=/var/lib/mysql/mysql-bin.index
#配置serverid
server-id=1
```

**在`mysqld`下添加第二种方式**

```
#第二种方式:
#此一行等同于上面log_bin三行,这里可以写绝对路径,也可以直接写mysql-bin(后者默认就是在/var/lib/mysql目录下)
log-bin=/var/lib/mysql/mysql-bin
#配置serverid
server-id=1
```

修改完配置后，重启`mysql`。执行`SHOW VARIABLES LIKE 'log_bin';` `Value` 值为 `ON`即可。

**详细binlog的配置简介**

```
[mysqld]
#设置日志三种格式：STATEMENT、ROW、MIXED 。
binlog_format = mixed
#设置日志路径，注意路经需要mysql用户有权限写,这里可以写绝对路径,也可以直接写mysql-bin(后者默认就是在/var/lib/mysql目录下)
log-bin = /data/mysql/logs/mysql-bin.log
#设置binlog清理时间
expire_logs_days = 7
#binlog每个日志文件大小
max_binlog_size = 100m
#binlog缓存大小
binlog_cache_size = 4m
#最大binlog缓存大小
max_binlog_cache_size = 512m
#配置serverid
server-id=1
```

-   `STATMENT`模式`(默认)`：`基于SQL语句的复制(statement-based replication, SBR)，每一条会修改数据的sql语句会记录到binlog中`。  
    优点：`不需要记录每一条SQL语句与每行的数据变化`，这样子binlog的日志也会比较少，减少了磁盘IO，提高性能。
    
    缺点：在某些情况下会导致master-slave中的数据不一致(比如：`delete from t where a>=4 and t_modified<='2018-11-10' limit 1;`在主库执行这个语句的时候，如果使用的是`a索引`，会删除`(4,4,'2018-11-10')`这条记录，如果使用的是`t_modified的索引`则会删除`(5,5,'2018-11-09');`所以在执行这条sql语句的时候提示： `Unsafe statement written to the binary log using statement format since BINLOG_FORMAT = STATEMENT. The statement is unsafe because it uses a LIMIT clause. This is unsafe because the set of rows included cannot be predicted.`  
    `由于 statement 格式下，记录到 binlog 里的是语句原文，因此可能会出现这样一种情况：在主库执行这条 SQL 语句的时候，用的是索引 a；而在备库执行这条 SQL 语句的时候，却使用了索引 t_modified`。因此，MySQL  
    认为这样写是有风险的。 sleep()函数， last\_insert\_id()，以及user-defined  
    functions(udf)等也会出现问题)；
    
-   `ROW:``基于行的复制(row-based replication, RBR)`格式：`不记录每一条SQL语句的上下文信息，仅需记录哪条数据被修改了，修改成了什么样子了`。  
    优点：不会出现某些特定情况下的存储过程、或function、或trigger的调用和触发无法被正确复制的问题。  
    `缺点：会产生大量的日志，尤其是alter table的时候会让日志暴涨。`
    
-   `MIXED:``混合模式复制(mixed-based replication, MBR)`：以上两种模式的混合使用，`一般的复制使用STATEMENT模式保存binlog`，`对于STATEMENT模式无法复制的操作使用ROW模式保存binlog`，MySQL会根据执行的SQL语句选择日志保存方式。
    

## mysql查看binlog

`binlog`是二进制文件，普通文件查看器`cat` `more` `vi`等都无法打开，必须使用自带的 `mysqlbinlog` 命令查看

在查看binlog的时候可能会遇到  
`mysqlbinlog: [ERROR] unknown variable 'default-character-set=utf8'`  
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916202340857.png#pic_center)  
原因是`mysqlbinlog`这个工具无法识别`binlog`中的配置中的`default-character-set=utf8`这个指令。  
两个方法可以解决这个问题

1.  在`MySQL`的配置`/etc/my.cnf`中将`default-character-set=utf8` 修改为  
    `character-set-server = utf8`，但是这`需要重启MySQL服务`，如果你的MySQL服务正在忙，那这样的代价会比较大。
2.  用`mysqlbinlog --no-defaults mysql-bin.000001` 命令打开

但是用`mysqlbinlog`打开的`binlog`日志内容很多不容易分辨查看`pos`点信息

`这里介绍一种更为方便的查询命令：`  
使用`root`用户登录进`mysql`

**语法:**  
`show binlog events [IN 'log_name'] [FROM pos] [LIMIT [offset,] row_count];`

-   IN ‘log\_name’ 指定要查询的binlog文件名(不指定就是第一个binlog文件)
-   FROM pos 指定从哪个pos起始点开始查起(不指定就是从整个文件首个pos点开始算)
-   LIMIT \[offset,\] 偏移量(不指定就是0)
-   row\_count 查询总条数(不指定就是所有行)

查看总共有几个`binlog`文件

```
show master logs;
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916203448567.png#pic_center)

```
show binlog events in 'mysql-bin.000001' limit 10
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200916203523550.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80Mzk0NDMwNQ==,size_16,color_FFFFFF,t_70#pic_center)

## 通过binlog数据恢复

首先要找到记录出现问题点的那个`binlog`  
然后找到`pos`点,不然把误操作直接redo了还是GG

恢复命令的语法格式：

```
mysqlbinlog mysql-bin.0000xx | mysql -u用户名 -p密码 数据库名
```

常用参数选项解释：

-   –start-position=875 起始pos点
-   –stop-position=954 结束pos点
-   –start-datetime=“2016-9-25 22:01:08” 起始时间点
-   –stop-datetime=“2019-9-25 22:09:46” 结束时间点
-   –database=xxx指定只恢复xxx数据库(一台主机上往往有多个数据库，只限本地log日志)

> 实际是将读出的binlog日志内容，通过管道符传递给mysql命令。这些命令、文件尽量写成绝对路径；

**使用`.sql`文件全量恢复(记得剔除掉问题语句比如`drop`)demo**

```
root@ba586179fe4b:/opt/backup# mysqlbinlog /opt/backup/mysql-bin.000003 > /opt/backup/000003.sql
root@ba586179fe4b:/opt/backup# vi /opt/backup/000003.sql #删除里面的drop语句
# 删掉drop语句前后的# at 到 /*！*/之间的内容
root@ba586179fe4b:/opt/backup# mysql -uroot -p123456 -v < /opt/backup/000003.sql
```

**指定pos结束点恢复(部分恢复)：**

```
root@ba586179fe4b:/opt/backup# mysqlbinlog --stop-position=571 --database=codehui /var/lib/mysql/mysql-bin.000003 | mysql -uroot -p123456 -v codehui

mysql> use codehui;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> select * from test;
+----+--------+
| id | name   |
+----+--------+
|  1 | code   |
|  2 | php    |
|  3 | java   |
|  4 | golang |
|  5 | shell  |
+----+--------+
5 rows in set (0.00 sec)
```

**pos点区间恢复**

```
root@ba586179fe4b:/opt/backup# mysqlbinlog --start-position=882 --stop-position=995 --database=codehui /var/lib/mysql/mysql-bin.000003 | mysql -uroot -p123456 -v codehui
```

**也可指定时间区间恢复(部分恢复)**

```
# 起始时间点
--start-datetime="YYYY-MM-DD H:I:S"
# 结束时间点
--stop-datetime ="YYYY-MM-DD H:I:S"

# 用法举例
mysqlbinlog --start-position=811 --start-datetime="YYYY-MM-DD H:I:S" --stop-datetime="YYYY-MM-DD H:I:S" --database=codehui /var/lib/mysql/mysql-bin.000003 | mysql -uroot -p123456 -v codehui
```

参考了大佬的文章,然后自己操作了一番写了点心得  
[https://www.jianshu.com/p/e1cacf8cceff](https://www.jianshu.com/p/e1cacf8cceff)

## 删除binlog

删除mysql的binlog日志有两种方法：自动删除和手动删除

**自动删除**

永久生效：修改mysql的配置文件`my.cnf`，添加binlog过期时间的配置项：expire\_logs\_days=30，然后重启mysql，这个有个致命的缺点就是需要重启mysql。

临时生效：进入mysql，用以下命令设置全局的参数：set [global](https://so.csdn.net/so/search?q=global&spm=1001.2101.3001.7020) expire\_logs\_days=30; （上面的数字30是保留30天的意思。）

**手动删除**

可以直接删除binlog文件，但是可以`通过mysql提供的工具来删除更安全，因为purge会更新mysql-bin.index中的条目，而直接删除的话，mysql-bin.index文件不会更新`。mysql-bin.index的作用是加快查找binlog文件的速度。

（1）直接删除

找到binlog所在目录，用rm binglog名 直接删除

demo  
`rm mysql-bin.010`

（2）通过mysql提供的工具来删除(`推荐`)

删除之前可以先看一下purge的用法：help purge;

demo：

```
#删除所有binlog日志，新日志编号从头开始
RESET MASTER;
#删除mysql-bin.010之前所有日志
PURGE MASTER LOGS TO 'mysql-bin.010';
#删除2003-04-02 22:46:26之前产生的所有日志
PURGE MASTER LOGS BEFORE '2003-04-02 22:46:26';
```