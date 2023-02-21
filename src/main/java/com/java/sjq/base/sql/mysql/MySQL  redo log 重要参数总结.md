[MySQL中Redo Log相关的重要参数总结 - 潇湘隐者 - 博客园](https://www.cnblogs.com/kerrycode/p/13814012.html)

# **参数介绍**

下面介绍、总结一下MySQL的Redo Log相关的几个重要参数：innodb\_log\_buffer\_size、innodb\_log\_file\_size、innodb\_log\_files\_in\_group

## **innodb\_log\_buffer\_size**

<table><tbody><tr><td><p><a rel="noopener"></a><b><span><span face="等线"><span>Command-Line Format</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>--innodb-log-buffer-size=#</span></span></span></p></td></tr><tr><td><p><b><span><span face="等线"><span>System Variable</span></span></span></b><b><span></span></b></p></td><td><p><u><span lang="X-NONE"><span face="Courier New"><span color="#0074a3">innodb_log_buffer_size</span></span></span></u></p></td></tr><tr><td><p><b><span><span face="等线"><span>Scope</span></span></span></b></p></td><td><p><span><span face="等线"><span>Global</span></span></span></p></td></tr><tr><td><p><b><span><span face="等线"><span>Dynamic</span></span></span></b><b><span></span></b></p></td><td><p><span><span face="等线"><span>Yes</span></span></span><span></span></p></td></tr><tr><td><p><u><span lang="X-NONE"><span face="Courier New"><span>SET_VAR</span></span></span></u><span><b><span lang="X-NONE"><span face="Times New Roman">&nbsp;</span></span></b></span><b><span><span face="等线"><span>Hint Applies</span></span></span></b></p></td><td><p><span><span face="等线"><span>No</span></span></span></p></td></tr><tr><td><p><b><span><span face="等线"><span>Type</span></span></span></b><b><span></span></b></p></td><td><p><span><span face="等线"><span>Integer</span></span></span><span></span></p></td></tr><tr><td><p><b><span><span face="等线"><span>Default Value</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>16777216</span></span></span></p></td></tr><tr><td><p><b><span><span face="等线"><span>Minimum Value</span></span></span></b><b><span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>1048576</span></span></span></p></td></tr><tr><td><p><b><span><span face="等线"><span>Maximum Value</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>4294967295</span></span></span></p></td></tr></tbody></table>

The size in bytes of the buffer that InnoDB uses to write to the log files on disk. The default is 16MB. A large log buffer enables large transactions to run without the need to write the log to disk before the transactions commit. Thus, if you have transactions that update, insert, or delete many rows, making the log buffer larger saves disk I/O. For related information, see Memory Configuration, and Section 8.5.4, “Optimizing InnoDB Redo Logging”. For general I/O tuning advice, see Section 8.5.8, “Optimizing InnoDB Disk I/O”.

参数用来设置缓存还未提交的事务的缓冲区的大小，通俗来说也就是日志缓冲区的大小。一般默认值16MB是够用的，但如果事务之中含有blog/text等大字段，这个缓冲区会被很快填满会引起额外的IO负载。可通过查看 **innodb\_log\_waits** 状态，如果不为0的话，则需要增加innodb\_log\_buffer\_size。

```
mysql> show variables like 'innodb_log_buffer_size';
```

```
+------------------------+----------+
```

```
| Variable_name          | Value    |
```

```
+------------------------+----------+
```

```
| innodb_log_buffer_size | 16777216 |
```

```
+------------------------+----------+
```

```
1 row in set (0.00 sec)
```

```
mysql> show status like 'innodb_log_waits';
```

```
+------------------+-------+
```

```
| Variable_name    | Value |
```

```
+------------------+-------+
```

```
| Innodb_log_waits | 0     |
```

```
+------------------+-------+
```

```
1 row in set (0.00 sec)
```

```
mysql>
```

## **innodb\_log\_file\_size**

参数innodb\_log\_file\_size用于设定MySQL日志组中每个日志文件的大小。此参数是一个全局的静态参数，不能动态修改。

```
mysql> show variables like 'innodb_log_file_size';
```

```
+----------------------+----------+
```

```
| Variable_name        | Value    |
```

```
+----------------------+----------+
```

```
| innodb_log_file_size | 50331648 |
```

```
+----------------------+----------+
```

```
1 row in set (0.02 sec)
```

官方文档关于参数innodb\_log\_file\_size的介绍如下：

<table><tbody><tr><td><p><a rel="noopener"></a><b><span lang="X-NONE"><span face="Courier New"><span>Command-Line Format</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>--innodb-log-file-size=#</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>System Variable</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><u><span lang="X-NONE"><span face="Courier New"><span color="#0074a3">innodb_log_file_size</span></span></span></u></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Scope</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>Global</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Dynamic</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>No</span></span></span><span lang="X-NONE"></span></p></td></tr><tr><td><p><span face="Courier New"><u><span lang="X-NONE"><span>SET_VAR</span></span></u><b><span lang="X-NONE"><span> Hint Applies</span></span></b></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>No</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Type</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>Integer</span></span></span><span lang="X-NONE"></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Default Value</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>50331648</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Minimum Value</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>4194304</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Maximum Value</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>512GB / innodb_log_files_in_group</span></span></span></p></td></tr></tbody></table>

The size in bytes of each log file in a log group. The combined size of log files (innodb\_log\_file\_size \* innodb\_log\_files\_in\_group) cannot exceed a maximum value that is slightly less than 512GB. A pair of 255 GB log files, for example, approaches the limit but does not exceed it. The default value is 48MB.

Generally, the combined size of the log files should be large enough that the server can smooth out peaks and troughs in workload activity, which often means that there is enough redo log space to handle more than an hour of write activity. The larger the value, the less checkpoint flush activity is required in the buffer pool, saving disk I/O. Larger log files also make crash recovery slower.

The minimum innodb\_log\_file\_size is 4MB.

For related information, see Redo Log File Configuration. For general I/O tuning advice, see Section 8.5.8, “Optimizing InnoDB Disk I/O”.

If innodb\_dedicated\_server is enabled, the innodb\_log\_file\_size value is automatically configured if it is not explicitly defined. For more information, see Section 15.8.12, “Enabling Automatic Configuration for a Dedicated MySQL Server”.

**注意事项：**

·         参数innodb\_log\_file\_size的单位为字节，它的默认值（MySQL 5.6.8以及之后版本）**默认为48M， 50331648/1024/1024=48M**。而在之前的MySQL版本中（例如MySQL 5.5），此参数的默认值为5M。

·         参数innodb\_log\_file\_size的最小值跟MySQL版本有关系，MySQL 5.7.11之前的版本中，参数innodb\_log\_file\_size的最小值为1MB，**MySQL 5.7.11以后版本，参数innodb\_log\_file\_size的最小值增加到4MB。**

·         **参数innodb\_log\_file\_size的最大值，二进制日志文件大小（innodb\_log\_file\_size \* innodb\_log\_files\_in\_group）不能超过512GB**，**所以一般而言，其大小值为512GB / innodb\_log\_files\_in\_group**，而innodb\_log\_files\_in\_group最小值为2，所以innodb\_log\_file\_size最大值不能超过256GB。其实这个参数也跟MySQL版本有关，MySQL 5.5和之前的版本中，innodb\_log\_file\_size最大值为2GB。

1. ·**如果参数innodb\_log\_file\_size设置太小，就会导致MySQL的日志文件（redo log）频繁切换，频繁的触发数据库的检查点（Checkpoint），导致刷新脏页（dirty page）到磁盘的次数增加。从而影响IO性能**。
2. **<u>另外，如果有一个大的事务，把所有的日志文件写满了，还没有写完，这样就会导致日志不能切换（因为实例恢复还需要，不能被循环复写，好比Oracle中的redo log无法循环覆盖）这样MySQL就Hang住了</u>**。
3. 如果参数innodb\_log\_file\_size设置太大的话，虽然大大提升了IO性能，但是当MySQL由于意外（断电，OOM-Kill等）宕机时，二进制日志很大，那么恢复的时间必然很长。而且这个恢复时间往往不可控，受多方面因素影响。所以必须权衡二者进行综合考虑。

## **innodb\_log\_files\_in\_group**

参数innodb\_log\_files\_in\_group指定日志组个数。默认为2个日志组。

```
mysql> show variables like 'innodb_log_files_in_group';
```

```
+---------------------------+-------+
```

```
| Variable_name             | Value |
```

```
+---------------------------+-------+
```

```
| innodb_log_files_in_group | 2     |
```

```
+---------------------------+-------+
```

```
1 row in set (0.00 sec)
```

官方文档的介绍如下所示：

<table><tbody><tr><td><p><a rel="noopener"></a><b><span lang="X-NONE"><span face="Courier New"><span>Command-Line Format</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>--innodb-log-files-in-group=#</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>System Variable</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><u><span lang="X-NONE"><span face="Courier New"><span color="#0074a3">innodb_log_files_in_group</span></span></span></u></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Scope</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>Global</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Dynamic</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>No</span></span></span><span lang="X-NONE"></span></p></td></tr><tr><td><p><span face="Courier New"><u><span lang="X-NONE"><span>SET_VAR</span></span></u><b><span lang="X-NONE"><span> Hint Applies</span></span></b></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>No</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Type</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>Integer</span></span></span><span lang="X-NONE"></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Default Value</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>2</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Minimum Value</span></span></span></b><b><span lang="X-NONE"></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>2</span></span></span></p></td></tr><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Maximum Value</span></span></span></b></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>100</span></span></span></p></td></tr></tbody></table>

The number of log files in the log group. InnoDB writes to the files in a circular fashion. The default (and recommended) value is 2. The location of the files is specified by innodb\_log\_group\_home\_dir. The combined size of log files (innodb\_log\_file\_size \* innodb\_log\_files\_in\_group) can be up to 512GB.

此参数的默认值为2，最小值为2，最大值为100，一般可能2~3居多。这个参数其实没有什么可以分析的。

**innodb\_mirrored\_log\_groups**

innodb\_mirrored\_log\_groups：指定日志镜像文件组的数量，默认为 1. 相信熟悉Oracle的应该不会陌生。不过这个参数是一个弃用的参数，MySQL 5.6中已经提示为启用参数，在MySQL 5.7中已经移除了。这里不做过多介绍。

# **参数优化**

其它几个参数的优化，其实没有太多可以说的，主要是关于参数innodb\_log\_file\_size的调优。参数innodb\_log\_file\_size的大小设置或者优化设置有没有什么guideline呢？在MySQL 8.0之前，一般是计算一段时间内生成的事务日志（redo log）的大小， 而MySQL的日志文件的大小最少应该承载一个小时的业务日志量。

一个Guideline：计算、统计一分钟内生成的事务日志大小，然后以这个值为均值，计算一个小时内生成了多少日志量。参考博客“How to calculate a good InnoDB log file size”

```
mysql> pager grep sequence;
```

```
PAGER set to 'grep sequence'
```

```
mysql> show engine innodb status\G select sleep(60); show engine innodb status\G
```

```
Log sequence number          1103198881953
```

```
1 row in set (0.00 sec)
```

```
1 row in set (1 min 0.00 sec)
```

```
Log sequence number          1103205163584
```

```
1 row in set (0.00 sec)
```

```
mysql> nopager;
```

```
PAGER set to stdout
```

[![clip_image001](https://img2020.cnblogs.com/blog/73542/202010/73542-20201014115549648-1701819067.png "clip_image001")](https://img2020.cnblogs.com/blog/73542/202010/73542-20201014115549084-2027544230.png)

```
mysql> select (1103205163584-1103198881953)/1024/1024 as MB_per_min;
```

```
+------------+
```

```
| MB_per_min |
```

```
+------------+
```

```
| 5.99063015 |
```

```
+------------+
```

```
1 row in set (0.00 sec)
```

```
mysql> select (1103205163584-1103198881953)/1024/1024*60 as MB_per_hour;
```

```
+--------------+
```

```
| MB_per_hour  |
```

```
+--------------+
```

```
| 359.43780899 |
```

```
+--------------+
```

```
1 row in set (0.03 sec)
```

```
mysql> 
```

但是关于这个Guideline也有一个问题(个人看法)，**就是这一分钟是位于业务繁忙时段？ 还是业务空闲时段？ MySQL生成日的志是平均的还是有较大的波动范围？用一分钟内生成的日志大小做均值推算一个小时内生成的事务日志大小，这个可能存在较大的误差，所以，正确的操作应该是计算半小时或一小时内生成的日志量大小。这样不仅更接近均值，而且误差也更小**。

```
mysql> pager grep sequence;
```

```
PAGER set to 'grep sequence'
```

```
mysql> show engine innodb status\G select sleep(60*60);show engine innodb status\G
```

```
Log sequence number          1114192951353
```

```
1 row in set (0.00 sec)
```

```
1 row in set (1 hour 0.00 sec)
```

```
Log sequence number          1114578626251
```

```
1 row in set (0.01 sec)
```

```
mysql> nopager;
```

```
PAGER set to stdout
```

```
mysql> 
```

MySQL 8.0引入了innodb\_dedicated\_server自适应参数，可基于服务器的内存来动态设置innodb\_buffer\_pool\_size，innodb\_log\_file\_size和innodb\_flush\_method。默认情况下，此参数是关闭的。

如果设置参数innodb\_dedicated\_server为ON后，MySQL会自动探测服务器的内存资源，确定innodb\_buffer\_pool\_size, innodb\_log\_file\_size 和 innodb\_flush\_method 三个参数的取值。具体取值策略如下

**innodb\_log\_file\_size****：**

在MySQL 8.0.13之前的版本（MySQL 8.\*）中，根据服务器内存来动态设置innodb\_log\_file\_size大小。规则如下

<table><tbody><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Detected Server Memory</span></span></span></b></p></td><td><p><b><span lang="X-NONE"><span face="Courier New"><span>Log File Size</span></span></span></b><b><span lang="X-NONE"></span></b></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span>&lt; 1GB</span></span></span><span lang="X-NONE"></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>48MiB (the default value)</span></span></span><span lang="X-NONE"></span></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span>&lt;= 4GB</span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>128M</span></span></span></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span>&lt;= 8GB</span></span></span><span lang="X-NONE"></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>512M</span></span></span><span lang="X-NONE"></span></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span>&lt;= 16GB</span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>1024M</span></span></span></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span>&gt; 16GB</span></span></span><span lang="X-NONE"></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span>2048M</span></span></span><span lang="X-NONE"></span></p></td></tr></tbody></table>


从MySQL 8.0.14开始，根据buffer pool size的大小进行配置innodb\_log\_file\_size参数

<table><tbody><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span color="#333333">buffer pool size</span></span></span></b></p></td><td><p><b><span lang="X-NONE"><span face="Courier New"><span color="#333333">log file size</span></span></span></b></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">&lt;=8GB</span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">512mb</span></span></span></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">8GB--128GB</span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">1024mb</span></span></span></p></td></tr><tr><td><p><span color="#333333"><span><span face="宋体"><span>大于</span></span></span><span lang="X-NONE"><span face="Courier New"><span>128GB</span></span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">2048mb</span></span></span></p></td></tr></tbody></table>

**innodb\_log\_files\_in\_group**


MySQL 8.0.14中，根据buffer pool size的大小进行自动配置（单位是GB）。

<table><tbody><tr><td><p><b><span lang="X-NONE"><span face="Courier New"><span color="#333333">buffer pool size</span></span></span></b></p></td><td><p><b><span lang="X-NONE"><span face="Courier New"><span color="#333333">log file number</span></span></span></b></p></td></tr><tr><td><p><span color="#333333"><span><span face="等线"><span>小于</span></span></span><span lang="X-NONE"><span face="Courier New"><span>8GB</span></span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">ROUND(buffer pool size)</span></span></span></p></td></tr><tr><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">8GB--128GB</span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">ROUND(buffer pool size * 0.75)</span></span></span></p></td></tr><tr><td><p><span color="#333333"><span><span face="等线"><span>大于</span></span></span><span lang="X-NONE"><span face="Courier New"><span>128GB</span></span></span></span></p></td><td><p><span lang="X-NONE"><span face="Courier New"><span color="#333333">64</span></span></span></p></td></tr></tbody></table>

如果buffer pool size小于2GB，innodb\_log\_files\_in\_group最小值是2。

注意：官方只是建议在可以使用全部的系统资源的专用服务器上配置开启该参数。如果MySQL和其它应用（例如Tomcat、Apach等）共享资源的话，是不建议开启该参数的。

另外，参数innodb\_dedicated\_server是一个只读参数，需要在配置文件my.cnf中设置。

mysql> set global innodb\_dedicated\_server=ON;

ERROR 1238 (HY000): Variable 'innodb\_dedicated\_server' is a read only variable

**参考资料：**

https://www.percona.com/blog/2017/10/18/chose-mysql-innodb\_log\_file\_size/

https://www.percona.com/blog/2008/11/21/how-to-calculate-a-good-innodb-log-file-size/

https://dev.mysql.com/doc/refman/8.0/en/innodb-parameters.html#sysvar\_innodb\_log\_file\_size