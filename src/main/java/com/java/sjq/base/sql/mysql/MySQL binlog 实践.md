> [MySQL :: MySQL 5.7 Reference Manual :: 4.6.7 mysqlbinlog — Utility for Processing Binary Log Files --- MySQL ：： MySQL 5.7 参考手册 ：： 4.6.7 mysqlbinlog — 用于处理二进制日志文件的实用程序](https://dev.mysql.com/doc/refman/5.7/en/mysqlbinlog.html)
> 
> [Mysql-Windows下开启binlog及查看-CSDN博客](https://blog.csdn.net/StrugglingXuYang/article/details/122968419)

1. 在[C:\ProgramData\MySQL\MySQL Server 5.7\my.ini](..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2F..%2FProgramData%2FMySQL%2FMySQL%20Server%205.7%2Fmy.ini) 启用 binlog
```text
log-bin=mysql-bin
binlog-format=mixed
```

2. 重启服务
```shell
net stop MySQL57
```

```shell
net start MySQL57
```
在 `C:\ProgramData\MySQL\MySQL Server 5.7\Data\mysql-bin` 路径下新增了两个文件
```text
mysql-bin.000001
mysql-bin.index
```

3. 随便执行一个增删改查语句
```mysql
insert into laf.user values("lisi",111111,0,0);
insert into laf.user values("wangwu",111111,0,0);
update laf.user set isAdmin=1 where userid = 'wangwu';
# 上面直接使用 laf.user 这种方式，mysqlbinlog -d 参数筛选不出来
use laf;
insert into user values("zhaoliu",111111,0,0);
update laf.user set isAdmin=1 where userid = 'zhaoliu';
```

```shell
# select * from laf.user;

'lisi', '111111', '0', '0'
'qqq', '123', '0', '0'
'sunupo', '123456', '1', '0'
'wangwu', '111111', '1', '0'
'zhangsan', '123456', '0', '0'
'zhaoliu', '111111', '1', '0'
```

4. 查看binlog
- show binlog events;
- show binlog events in 'bin-log.000001';
```shell
mysql> show binlog events in 'mysql-bin.000001';
+------------------+------+----------------+-----------+-------------+-------------------------------------------------------------------+
| Log_name         | Pos  | Event_type     | Server_id | End_log_pos | Info                                                              |
+------------------+------+----------------+-----------+-------------+-------------------------------------------------------------------+
| mysql-bin.000001 |    4 | Format_desc    |         1 |         123 | Server ver: 5.7.34-log, Binlog ver: 4                             |
| mysql-bin.000001 |  123 | Previous_gtids |         1 |         154 |                                                                   |
| mysql-bin.000001 |  154 | Anonymous_Gtid |         1 |         219 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'                              |
| mysql-bin.000001 |  219 | Query          |         1 |         293 | BEGIN                                                             |
| mysql-bin.000001 |  293 | Query          |         1 |         408 | insert into laf.user values("lisi",111111,0,0)                    |
| mysql-bin.000001 |  408 | Xid            |         1 |         439 | COMMIT /* xid=66 */                                               |
| mysql-bin.000001 |  439 | Anonymous_Gtid |         1 |         504 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'                              |
| mysql-bin.000001 |  504 | Query          |         1 |         578 | BEGIN                                                             |
| mysql-bin.000001 |  578 | Query          |         1 |         695 | insert into laf.user values("wangwu",111111,0,0)                  |
| mysql-bin.000001 |  695 | Xid            |         1 |         726 | COMMIT /* xid=132 */                                              |
| mysql-bin.000001 |  726 | Anonymous_Gtid |         1 |         791 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'                              |
| mysql-bin.000001 |  791 | Query          |         1 |         865 | BEGIN                                                             |
| mysql-bin.000001 |  865 | Query          |         1 |         987 | update laf.user set isAdmin=1 where userid = 'wangwu'             |
| mysql-bin.000001 |  987 | Xid            |         1 |        1018 | COMMIT /* xid=146 */                                              |
| mysql-bin.000001 | 1018 | Anonymous_Gtid |         1 |        1083 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'                              |
| mysql-bin.000001 | 1083 | Query          |         1 |        1160 | BEGIN                                                             |
| mysql-bin.000001 | 1160 | Query          |         1 |        1277 | use `laf`; insert into user values("zhaoliu",111111,0,0)          |
| mysql-bin.000001 | 1277 | Xid            |         1 |        1308 | COMMIT /* xid=193 */                                              |
| mysql-bin.000001 | 1308 | Anonymous_Gtid |         1 |        1373 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'                              |
| mysql-bin.000001 | 1373 | Query          |         1 |        1450 | BEGIN                                                             |
| mysql-bin.000001 | 1450 | Query          |         1 |        1576 | use `laf`; update laf.user set isAdmin=1 where userid = 'zhaoliu' |
| mysql-bin.000001 | 1576 | Xid            |         1 |        1607 | COMMIT /* xid=195 */                                              |
| mysql-bin.000001 | 1607 | Anonymous_Gtid |         1 |        1672 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'                              |
| mysql-bin.000001 | 1672 | Query          |         1 |        1749 | BEGIN                                                             |
| mysql-bin.000001 | 1749 | Query          |         1 |        1875 | use `laf`; update laf.user set isAdmin=1 where userid = 'zhaoliu' |
| mysql-bin.000001 | 1875 | Xid            |         1 |        1906 | COMMIT /* xid=197 */                                              |
+------------------+------+----------------+-----------+-------------+-------------------------------------------------------------------+
26 rows in set (0.00 sec)

```


5. 将binlog转换为log
```shell
#首先需要切换到存放mysqlbinlog.exe应用程序的目录bin后，执行以下命令
mysqlbinlog.exe --base64-output=decode-rows -v "C:\ProgramData\MySQL\MySQL Server 5.7\Data\mysql-bin.000001" >mysqlbin.log
```

```shell
PS C:\Windows\system32> cat mysqlbin.log
```
```shell
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=1*/;
/*!50003 SET @OLD_COMPLETION_TYPE=@@COMPLETION_TYPE,COMPLETION_TYPE=0*/;
DELIMITER /*!*/;
# at 4
#230413 11:26:47 server id 1  end_log_pos 123 CRC32 0x1d703089  Start: binlog v 4, server v 5.7.34-log created 230413 11:26:47 at startup
# Warning: this binlog is either in use or was not closed properly.
ROLLBACK/*!*/;
# at 123
#230413 11:26:47 server id 1  end_log_pos 154 CRC32 0xab70b50f  Previous-GTIDs
# [empty]
# at 154
#230413 11:33:19 server id 1  end_log_pos 219 CRC32 0xba032068  Anonymous_GTID  last_committed=0        sequence_number=1       rbr_only=no
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 219
#230413 11:33:19 server id 1  end_log_pos 293 CRC32 0x8e334ef9  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681356799/*!*/;
SET @@session.pseudo_thread_id=4/*!*/;
SET @@session.foreign_key_checks=1, @@session.sql_auto_is_null=0, @@session.unique_checks=1, @@session.autocommit=1/*!*/;
SET @@session.sql_mode=1344274432/*!*/;
SET @@session.auto_increment_increment=1, @@session.auto_increment_offset=1/*!*/;
/*!\C utf8mb4 *//*!*/;
SET @@session.character_set_client=45,@@session.collation_connection=45,@@session.collation_server=8/*!*/;
SET @@session.lc_time_names=0/*!*/;
SET @@session.collation_database=DEFAULT/*!*/;
BEGIN
/*!*/;
# at 293
#230413 11:33:19 server id 1  end_log_pos 408 CRC32 0x1ee3f8a1  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681356799/*!*/;
insert into laf.user values("lisi",111111,0,0)
/*!*/;
# at 408
#230413 11:33:19 server id 1  end_log_pos 439 CRC32 0x4f1e3b07  Xid = 66
COMMIT/*!*/;
SET @@SESSION.GTID_NEXT= 'AUTOMATIC' /* added by mysqlbinlog */ /*!*/;
DELIMITER ;
# End of log file
/*!50003 SET COMPLETION_TYPE=@OLD_COMPLETION_TYPE*/;
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=0*/;
```

6. 将binlog中的命令截取并转换成可执行的sql文件

```shell
# 通过该命令查询到所需要恢复的事件起止的位置
mysql> show binlog EVENTS in 'mysql-bin.000001'


+------------------+-----+----------------+-----------+-------------+------------------------------------------------+
| Log_name         | Pos | Event_type     | Server_id | End_log_pos | Info                                           |
+------------------+-----+----------------+-----------+-------------+------------------------------------------------+
| mysql-bin.000001 |   4 | Format_desc    |         1 |         123 | Server ver: 5.7.34-log, Binlog ver: 4          |
| mysql-bin.000001 | 123 | Previous_gtids |         1 |         154 |                                                |
| mysql-bin.000001 | 154 | Anonymous_Gtid |         1 |         219 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'           |
| mysql-bin.000001 | 219 | Query          |         1 |         293 | BEGIN                                          |
| mysql-bin.000001 | 293 | Query          |         1 |         408 | insert into laf.user values("lisi",111111,0,0) |
| mysql-bin.000001 | 408 | Xid            |         1 |         439 | COMMIT /* xid=66 */                            |
+------------------+-----+----------------+-----------+-------------+------------------------------------------------+
6 rows in set (0.00 sec)
```


```shell
#指定binlog日志位置并指定数据库和需要恢复操作的起始位置，并转换为 sql 文件
mysqlbinlog C:\ProgramData\MySQL\MySQL Server 5.7\Data\mysql-bin.000001 -d mybatis --skip-gtids --start-position=154 --stop-position=408>testbinlog.sql
```
- binlog的全路径
- 数据库名称
- 需要恢复操作的起始位置
- 生成的sql文件

文件夹路径空格有问题，![img_7.png](img_7.png)
> ```shell
> PS C:\Windows\system32> mysqlbinlog C:\ProgramData\MySQL\MySQL Server 5.7\Data\mysql-bin.000001 -d mybatis --skip-gtids --start-position=154 --stop-position=408 > testbinlog.sql
> mysqlbinlog: File 'C:\ProgramData\MySQL\MySQL' not found (Errcode: 2 - No such file or directory)
> ```
> 
切换路径执行。
```shell
PS C:\Windows\system32> cd 'C:\ProgramData\MySQL\MySQL Server 5.7\Data'
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data> mysqlbinlog .\mysql-bin.000001 -d laf --skip-gtids --start-position=154 --stop-position=439 > testbinlog.sql
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data> cat .\testbinlog.sql
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=1*/;
/*!50003 SET @OLD_COMPLETION_TYPE=@@COMPLETION_TYPE,COMPLETION_TYPE=0*/;
DELIMITER /*!*/;
# at 4
#230413 11:26:47 server id 1  end_log_pos 123 CRC32 0x1d703089  Start: binlog v 4, server v 5.7.34-log created 230413 11:26:47 at startup
# Warning: this binlog is either in use or was not closed properly.
ROLLBACK/*!*/;
BINLOG '
d3Y3ZA8BAAAAdwAAAHsAAAABAAQANS43LjM0LWxvZwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAB3djdkEzgNAAgAEgAEBAQEEgAAXwAEGggAAAAICAgCAAAACgoKKioAEjQA
AYkwcB0=
'/*!*/;
/*!50616 SET @@SESSION.GTID_NEXT='AUTOMATIC'*//*!*/;
# at 154
# at 219
#230413 11:33:19 server id 1  end_log_pos 293 CRC32 0x8e334ef9  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681356799/*!*/;
SET @@session.pseudo_thread_id=4/*!*/;
SET @@session.foreign_key_checks=1, @@session.sql_auto_is_null=0, @@session.unique_checks=1, @@session.autocommit=1/*!*/;
SET @@session.sql_mode=1344274432/*!*/;
SET @@session.auto_increment_increment=1, @@session.auto_increment_offset=1/*!*/;
/*!\C utf8mb4 *//*!*/;
SET @@session.character_set_client=45,@@session.collation_connection=45,@@session.collation_server=8/*!*/;
SET @@session.lc_time_names=0/*!*/;
SET @@session.collation_database=DEFAULT/*!*/;
BEGIN
/*!*/;
# at 293
# at 408
#230413 11:33:19 server id 1  end_log_pos 439 CRC32 0x4f1e3b07  Xid = 66
COMMIT/*!*/;
DELIMITER ;
# End of log file
/*!50003 SET COMPLETION_TYPE=@OLD_COMPLETION_TYPE*/;
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=0*/;
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data>

```
-d 参数无法筛选出来 `insert into laf.user values("wangwu",111111,0,0);`

去掉 -d 

```shell
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data> mysqlbinlog .\mysql-bin.000001 --skip-gtids --start-position=154 --stop-position=439 > testbinlog.sql
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data> cat .\testbinlog.sql
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=1*/;
/*!50003 SET @OLD_COMPLETION_TYPE=@@COMPLETION_TYPE,COMPLETION_TYPE=0*/;
DELIMITER /*!*/;
# at 4
#230413 11:26:47 server id 1  end_log_pos 123 CRC32 0x1d703089  Start: binlog v 4, server v 5.7.34-log created 230413 11:26:47 at startup
# Warning: this binlog is either in use or was not closed properly.
ROLLBACK/*!*/;
BINLOG '
d3Y3ZA8BAAAAdwAAAHsAAAABAAQANS43LjM0LWxvZwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAB3djdkEzgNAAgAEgAEBAQEEgAAXwAEGggAAAAICAgCAAAACgoKKioAEjQA
AYkwcB0=
'/*!*/;
/*!50616 SET @@SESSION.GTID_NEXT='AUTOMATIC'*//*!*/;
# at 154
# at 219
#230413 11:33:19 server id 1  end_log_pos 293 CRC32 0x8e334ef9  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681356799/*!*/;
SET @@session.pseudo_thread_id=4/*!*/;
SET @@session.foreign_key_checks=1, @@session.sql_auto_is_null=0, @@session.unique_checks=1, @@session.autocommit=1/*!*/;
SET @@session.sql_mode=1344274432/*!*/;
SET @@session.auto_increment_increment=1, @@session.auto_increment_offset=1/*!*/;
/*!\C utf8mb4 *//*!*/;
SET @@session.character_set_client=45,@@session.collation_connection=45,@@session.collation_server=8/*!*/;
SET @@session.lc_time_names=0/*!*/;
SET @@session.collation_database=DEFAULT/*!*/;
BEGIN
/*!*/;
# at 293
#230413 11:33:19 server id 1  end_log_pos 408 CRC32 0x1ee3f8a1  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681356799/*!*/;
insert into laf.user values("lisi",111111,0,0)
/*!*/;
# at 408
#230413 11:33:19 server id 1  end_log_pos 439 CRC32 0x4f1e3b07  Xid = 66
COMMIT/*!*/;
DELIMITER ;
# End of log file
/*!50003 SET COMPLETION_TYPE=@OLD_COMPLETION_TYPE*/;
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=0*/;
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data>
```

不使用偏移
```shell
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data> mysqlbinlog .\mysql-bin.000001 -d laf > testbinlog.sql
WARNING: The option --database has been used. It may filter parts of transactions, but will include the GTIDs in any case. If you want to exclude or include transactions, you should use the options --exclude-gtids or --include-gtids, respectively, instead.
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data> cat .\testbinlog.sql
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=1*/;
/*!50003 SET @OLD_COMPLETION_TYPE=@@COMPLETION_TYPE,COMPLETION_TYPE=0*/;
DELIMITER /*!*/;
# at 4
#230413 11:26:47 server id 1  end_log_pos 123 CRC32 0x1d703089  Start: binlog v 4, server v 5.7.34-log created 230413 11:26:47 at startup
# Warning: this binlog is either in use or was not closed properly.
ROLLBACK/*!*/;
BINLOG '
d3Y3ZA8BAAAAdwAAAHsAAAABAAQANS43LjM0LWxvZwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAB3djdkEzgNAAgAEgAEBAQEEgAAXwAEGggAAAAICAgCAAAACgoKKioAEjQA
AYkwcB0=
'/*!*/;
# at 123
#230413 11:26:47 server id 1  end_log_pos 154 CRC32 0xab70b50f  Previous-GTIDs
# [empty]
# at 154
#230413 11:33:19 server id 1  end_log_pos 219 CRC32 0xba032068  Anonymous_GTID  last_committed=0        sequence_number=1       rbr_only=no
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 219
#230413 11:33:19 server id 1  end_log_pos 293 CRC32 0x8e334ef9  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681356799/*!*/;
SET @@session.pseudo_thread_id=4/*!*/;
SET @@session.foreign_key_checks=1, @@session.sql_auto_is_null=0, @@session.unique_checks=1, @@session.autocommit=1/*!*/;
SET @@session.sql_mode=1344274432/*!*/;
SET @@session.auto_increment_increment=1, @@session.auto_increment_offset=1/*!*/;
/*!\C utf8mb4 *//*!*/;
SET @@session.character_set_client=45,@@session.collation_connection=45,@@session.collation_server=8/*!*/;
SET @@session.lc_time_names=0/*!*/;
SET @@session.collation_database=DEFAULT/*!*/;
BEGIN
/*!*/;
# at 293
# at 408
#230413 11:33:19 server id 1  end_log_pos 439 CRC32 0x4f1e3b07  Xid = 66
COMMIT/*!*/;
# at 439
#230413 15:37:16 server id 1  end_log_pos 504 CRC32 0x1e1b9e8b  Anonymous_GTID  last_committed=1        sequence_number=2       rbr_only=no
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 504
#230413 15:37:16 server id 1  end_log_pos 578 CRC32 0xabd995e2  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681371436/*!*/;
BEGIN
/*!*/;
# at 578
# at 695
#230413 15:37:16 server id 1  end_log_pos 726 CRC32 0x277b785b  Xid = 132
COMMIT/*!*/;
# at 726
#230413 15:43:23 server id 1  end_log_pos 791 CRC32 0x6f9830d1  Anonymous_GTID  last_committed=2        sequence_number=3       rbr_only=no
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 791
#230413 15:43:23 server id 1  end_log_pos 865 CRC32 0xc6989ae8  Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681371803/*!*/;
BEGIN
/*!*/;
# at 865
# at 987
#230413 15:43:23 server id 1  end_log_pos 1018 CRC32 0xfb81766d         Xid = 146
COMMIT/*!*/;
# at 1018
#230413 16:03:47 server id 1  end_log_pos 1083 CRC32 0x335d2e14         Anonymous_GTID  last_committed=3        sequence_number=4       rbr_only=no
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 1083
#230413 16:03:47 server id 1  end_log_pos 1160 CRC32 0xceecef69         Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681373027/*!*/;
BEGIN
/*!*/;
# at 1160
#230413 16:03:47 server id 1  end_log_pos 1277 CRC32 0x2862d635         Query   thread_id=4     exec_time=0     error_code=0
use `laf`/*!*/;
SET TIMESTAMP=1681373027/*!*/;
insert into user values("zhaoliu",111111,0,0)
/*!*/;
# at 1277
#230413 16:03:47 server id 1  end_log_pos 1308 CRC32 0xa6a296aa         Xid = 193
COMMIT/*!*/;
# at 1308
#230413 16:03:55 server id 1  end_log_pos 1373 CRC32 0x092053e5         Anonymous_GTID  last_committed=4        sequence_number=5       rbr_only=no
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 1373
#230413 16:03:55 server id 1  end_log_pos 1450 CRC32 0x0c97a8c5         Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681373035/*!*/;
BEGIN
/*!*/;
# at 1450
#230413 16:03:55 server id 1  end_log_pos 1576 CRC32 0xb4f34ee9         Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681373035/*!*/;
update laf.user set isAdmin=1 where userid = 'zhaoliu'
/*!*/;
# at 1576
#230413 16:03:55 server id 1  end_log_pos 1607 CRC32 0xf3fcdbe3         Xid = 195
COMMIT/*!*/;
# at 1607
#230413 16:03:55 server id 1  end_log_pos 1672 CRC32 0x8140430d         Anonymous_GTID  last_committed=5        sequence_number=6       rbr_only=no
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 1672
#230413 16:03:55 server id 1  end_log_pos 1749 CRC32 0x0f285e24         Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681373035/*!*/;
BEGIN
/*!*/;
# at 1749
#230413 16:03:55 server id 1  end_log_pos 1875 CRC32 0xb39d4835         Query   thread_id=4     exec_time=0     error_code=0
SET TIMESTAMP=1681373035/*!*/;
update laf.user set isAdmin=1 where userid = 'zhaoliu'
/*!*/;
# at 1875
#230413 16:03:55 server id 1  end_log_pos 1906 CRC32 0xc7702138         Xid = 197
COMMIT/*!*/;
SET @@SESSION.GTID_NEXT= 'AUTOMATIC' /* added by mysqlbinlog */ /*!*/;
DELIMITER ;
# End of log file
/*!50003 SET COMPLETION_TYPE=@OLD_COMPLETION_TYPE*/;
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=0*/;
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data>
```



# 附录手册
```shell
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data> mysqlbinlog -help
C:\Program Files\MySQL\MySQL Server 5.7\bin\mysqlbinlog.exe Ver 3.4 for Win64 at x86_64
Copyright (c) 2000, 2021, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Dumps a MySQL binary log in a format usable for viewing or for piping to
the mysql command line client.

Usage: C:\Program Files\MySQL\MySQL Server 5.7\bin\mysqlbinlog.exe [options] log-files
  -?, --help          Display this help and exit.
  --base64-output=name
                      Determine when the output statements should be
                      base64-encoded BINLOG statements: 'never' disables it and
                      works only for binlogs without row-based events;
                      'decode-rows' decodes row events into commented
                      pseudo-SQL statements if the --verbose option is also
                      given; 'auto' prints base64 only when necessary (i.e.,
                      for row-based events and format description events).  If
                      no --base64-output[=name] option is given at all, the
                      default is 'auto'.
  --bind-address=name IP address to bind to.
  --character-sets-dir=name
                      Directory for character set files.
  -d, --database=name List entries for just this database (local log only).
  --rewrite-db=name   Rewrite the row event to point so that it can be applied
                      to a new database
  -#, --debug[=#]     This is a non-debug version. Catch this and exit.
  --debug-check       This is a non-debug version. Catch this and exit.
  --debug-info        This is a non-debug version. Catch this and exit.
  --default-auth=name Default authentication client-side plugin to use.
  -D, --disable-log-bin
                      Disable binary log. This is useful, if you enabled
                      --to-last-log and are sending the output to the same
                      MySQL server. This way you could avoid an endless loop.
                      You would also like to use it when restoring after a
                      crash to avoid duplication of the statements you already
                      have. NOTE: you will need a SUPER privilege to use this
                      option.
  -F, --force-if-open Force if binlog was not closed properly.
                      (Defaults to on; use --skip-force-if-open to disable.)
  -f, --force-read    Force reading unknown binlog events.
  -H, --hexdump       Augment output with hexadecimal and ASCII event dump.
  -h, --host=name     Get the binlog from server.
  -i, --idempotent    Notify the server to use idempotent mode before applying
                      Row Events
  -l, --local-load=name
                      Prepare local temporary files for LOAD DATA INFILE in the
                      specified directory.
  -o, --offset=#      Skip the first N entries.
  -p, --password[=name]
                      Password to connect to remote server.
  --plugin-dir=name   Directory for client-side plugins.
  -P, --port=#        Port number to use for connection or 0 for default to, in
                      order of preference, my.cnf, $MYSQL_TCP_PORT,
                      /etc/services, built-in default (3306).
  --protocol=name     The protocol to use for connection (tcp, socket, pipe,
                      memory).
  -R, --read-from-remote-server
                      Read binary logs from a MySQL server. This is an alias
                      for read-from-remote-master=BINLOG-DUMP-NON-GTIDS.
  --read-from-remote-master=name
                      Read binary logs from a MySQL server through the
                      COM_BINLOG_DUMP or COM_BINLOG_DUMP_GTID commands by
                      setting the option to either BINLOG-DUMP-NON-GTIDS or
                      BINLOG-DUMP-GTIDS, respectively. If
                      --read-from-remote-master=BINLOG-DUMP-GTIDS is combined
                      with --exclude-gtids, transactions can be filtered out on
                      the master avoiding unnecessary network traffic.
  --raw               Requires -R. Output raw binlog data instead of SQL
                      statements, output is to log files.
  -r, --result-file=name
                      Direct output to a given file. With --raw this is a
                      prefix for the file names.
  --secure-auth       Refuse client connecting to server if it uses old
                      (pre-4.1.1) protocol. Deprecated. Always TRUE
  --server-id=#       Extract only binlog entries created by the server having
                      the given id.
  --server-id-bits=#  Set number of significant bits in server-id
  --set-charset=name  Add 'SET NAMES character_set' to the output.
  --shared-memory-base-name=name
                      Base name of shared memory.
  -s, --short-form    Just show regular queries: no extra info and no row-based
                      events. This is for testing only, and should not be used
                      in production systems. If you want to suppress
                      base64-output, consider using --base64-output=never
                      instead.
  -S, --socket=name   The socket file to use for connection.
  --ssl-mode=name     SSL connection mode.
  --ssl               Deprecated. Use --ssl-mode instead.
                      (Defaults to on; use --skip-ssl to disable.)
  --ssl-verify-server-cert
                      Deprecated. Use --ssl-mode=VERIFY_IDENTITY instead.
  --ssl-ca=name       CA file in PEM format.
  --ssl-capath=name   CA directory.
  --ssl-cert=name     X509 cert in PEM format.
  --ssl-cipher=name   SSL cipher to use.
  --ssl-key=name      X509 key in PEM format.
  --ssl-crl=name      Certificate revocation list.
  --ssl-crlpath=name  Certificate revocation list path.
  --tls-version=name  TLS version to use, permitted values are: TLSv1, TLSv1.1,
                      TLSv1.2
  --server-public-key-path=name
                      File path to the server public RSA key in PEM format.
  --get-server-public-key
                      Get server public key
  --start-datetime=name
                      Start reading the binlog at first event having a datetime
                      equal or posterior to the argument; the argument must be
                      a date and time in the local time zone, in any format
                      accepted by the MySQL server for DATETIME and TIMESTAMP
                      types, for example: 2004-12-25 11:25:56 (you should
                      probably use quotes for your shell to set it properly).
  -j, --start-position=#
                      Start reading the binlog at position N. Applies to the
                      first binlog passed on the command line.
  --stop-datetime=name
                      Stop reading the binlog at first event having a datetime
                      equal or posterior to the argument; the argument must be
                      a date and time in the local time zone, in any format
                      accepted by the MySQL server for DATETIME and TIMESTAMP
                      types, for example: 2004-12-25 11:25:56 (you should
                      probably use quotes for your shell to set it properly).
  --stop-never        Wait for more data from the server instead of stopping at
                      the end of the last log. Implicitly sets --to-last-log
                      but instead of stopping at the end of the last log it
                      continues to wait till the server disconnects.
  --stop-never-slave-server-id=#
                      The slave server_id used for --read-from-remote-server
                      --stop-never. This option cannot be used together with
                      connection-server-id.
  --connection-server-id=#
                      The slave server_id used for --read-from-remote-server.
                      This option cannot be used together with
                      stop-never-slave-server-id.
  --stop-position=#   Stop reading the binlog at position N. Applies to the
                      last binlog passed on the command line.
  -t, --to-last-log   Requires -R. Will not stop at the end of the requested
                      binlog but rather continue printing until the end of the
                      last binlog of the MySQL server. If you send the output
                      to the same MySQL server, that may lead to an endless
                      loop.
  -u, --user=name     Connect to the remote server as username.
  -v, --verbose       Reconstruct pseudo-SQL statements out of row events. -v
                      -v adds comments on column data types.
  -V, --version       Print version and exit.
  --open-files-limit=#
                      Used to reserve file descriptors for use by this program.
  -c, --verify-binlog-checksum
                      Verify checksum binlog events.
  --binlog-row-event-max-size=#
                      The maximum size of a row-based binary log event in
                      bytes. Rows will be grouped into events smaller than this
                      size if possible. This value must be a multiple of 256.
  --skip-gtids        Do not preserve Global Transaction Identifiers; instead
                      make the server execute the transactions as if they were
                      new.
  --include-gtids=name
                      Print events whose Global Transaction Identifiers were
                      provided.
  --exclude-gtids=name
                      Print all events but those whose Global Transaction
                      Identifiers were provided.

Variables (--variable-name=value)
and boolean options {FALSE|TRUE}  Value (after reading options)
--------------------------------- ----------------------------------------
base64-output                     (No default value)
bind-address                      (No default value)
character-sets-dir                (No default value)
database                          (No default value)
rewrite-db                        (No default value)
default-auth                      (No default value)
disable-log-bin                   FALSE
force-if-open                     TRUE
force-read                        FALSE
hexdump                           FALSE
host                              elp
idempotent                        FALSE
local-load                        (No default value)
offset                            0
plugin-dir                        (No default value)
port                              0
read-from-remote-server           FALSE
read-from-remote-master           (No default value)
raw                               FALSE
result-file                       (No default value)
secure-auth                       TRUE
server-id                         0
server-id-bits                    32
set-charset                       (No default value)
shared-memory-base-name           (No default value)
short-form                        FALSE
socket                            (No default value)
ssl                               TRUE
ssl-verify-server-cert            FALSE
ssl-ca                            (No default value)
ssl-capath                        (No default value)
ssl-cert                          (No default value)
ssl-cipher                        (No default value)
ssl-key                           (No default value)
ssl-crl                           (No default value)
ssl-crlpath                       (No default value)
tls-version                       (No default value)
server-public-key-path            (No default value)
get-server-public-key             FALSE
start-datetime                    (No default value)
start-position                    4
stop-datetime                     (No default value)
stop-never                        FALSE
stop-never-slave-server-id        -1
connection-server-id              -1
stop-position                     18446744073709551615
to-last-log                       FALSE
user                              (No default value)
open-files-limit                  18432
verify-binlog-checksum            FALSE
binlog-row-event-max-size         4294967040
skip-gtids                        FALSE
include-gtids                     (No default value)
exclude-gtids                     (No default value)
PS C:\ProgramData\MySQL\MySQL Server 5.7\Data>
```