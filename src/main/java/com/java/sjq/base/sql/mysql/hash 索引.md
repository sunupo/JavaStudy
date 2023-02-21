[ Mysql如何创建Hash索引](https://blog.csdn.net/weixin_43889700/article/details/103429624)

## MySQL创建[Hash](https://so.csdn.net/so/search?q=Hash&spm=1001.2101.3001.7020)索引

以下是武汉理工大学数据库综合实验的一小部分  
最近在写数据库实验，现在进行到索引部分，下面内容记录了学习过程中需要的一些知识，本次笔记主要记录如何创建Hash索引

题目如下：  
创建一个包含(employeeID, name, education)等字段的临时员工表 (tmpEmployee) ，并在该表的员工编号字段上创建一个HASH索引

代码如下：  
创建表

```
mysql> create table tmpEmployee(
    -> employeeID CHAR(6) NOT NULL PRIMARY KEY,
    -> name CHAR(10) NOT NULL,
    -> education CHAR(4) NOT NULL
    -> );
Query OK, 0 rows affected (0.03 sec)
```

创建Hash索引

```
mysql> create index hash_id using hash on tmpEmployee(employeeID);
Query OK, 0 rows affected (0.04 sec)
Records: 0  Duplicates: 0  Warnings: 0
```

```
create index 索引名 using hash on 表名(列名);
```

结果

```
mysql> show index from tmpEmployee;
+-------------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+
| Table       | Non_unique | Key_name | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment |
+-------------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+
| tmpemployee |          0 | PRIMARY  |            1 | employeeID  | A         |           0 |     NULL | NULL   |      | BTREE      |         |               |
| tmpemployee |          1 | hash_id  |            1 | employeeID  | A         |           0 |     NULL | NULL   |      | BTREE      |         |               |
+-------------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+
2 rows in set (0.00 sec)
```

因为MySQL 5.5之后默认的存储引擎是InnoDB 支持 B-Tree和R-Tree 但是默认为B-Tree 所以索引类型依然是B-Tree