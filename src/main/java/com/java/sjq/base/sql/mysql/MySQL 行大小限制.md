> [mysql 5.7 row-size-limit](https://dev.mysql.com/doc/refman/5.7/en/column-count-limit.html#row-size-limits)

# row size limit

以下 `InnoDB` 和 `MyISAM` 示例中演示了 65，535 字节的 MySQL 最大行大小限制。无论存储引擎如何，都会强制实施此限制，即使存储引擎可能能够支持更大的行也是如此。

```shell
mysql> CREATE TABLE t (a VARCHAR(10000), b VARCHAR(10000),
       c VARCHAR(10000), d VARCHAR(10000), e VARCHAR(10000),
       f VARCHAR(10000), g VARCHAR(6000)) ENGINE=InnoDB CHARACTER SET latin1;
ERROR 1118 (42000): Row size too large. The maximum row size for the used
table type, not counting BLOBs, is 65535. This includes storage overhead,
check the manual. You have to change some columns to TEXT or BLOBs
```

```shell
mysql> CREATE TABLE t (a VARCHAR(10000), b VARCHAR(10000),
       c VARCHAR(10000), d VARCHAR(10000), e VARCHAR(10000),
       f VARCHAR(10000), g VARCHAR(6000)) ENGINE=MyISAM CHARACTER SET latin1;
ERROR 1118 (42000): Row size too large. The maximum row size for the used
table type, not counting BLOBs, is 65535. This includes storage overhead,
check the manual. You have to change some columns to TEXT or BLOBs
```



在下面的 `MyISAM` 示例中，将列更改为 `TEXT` 可避免 65，535 字节的行大小限制，并允许操作成功，因为 `BLOB` 和 `TEXT` 列仅对行大小贡献 9 到 12 个字节。

```shell
mysql> CREATE TABLE t (a VARCHAR(10000), b VARCHAR(10000),
       c VARCHAR(10000), d VARCHAR(10000), e VARCHAR(10000),
       f VARCHAR(10000), g TEXT(6000)) ENGINE=MyISAM CHARACTER SET latin1;
Query OK, 0 rows affected (0.02 sec)
```



对于 `InnoDB` 表，操作会成功，因为将列更改为 `TEXT` 可避免 MySQL 65，535 字节的行大小限制，而可变长度列的 `InnoDB` 页外存储可避免 `InnoDB` 行大小限制。

```shell
mysql> CREATE TABLE t (a VARCHAR(10000), b VARCHAR(10000),
       c VARCHAR(10000), d VARCHAR(10000), e VARCHAR(10000),
       f VARCHAR(10000), g TEXT(6000)) ENGINE=InnoDB CHARACTER SET latin1;
Query OK, 0 rows affected (0.02 sec)
```

# Column Count Limits 列计数限制

MySQL 的每个表有 4096 列的硬限制，但给定表的有效最大值可能更少。确切的列限制取决于几个因素：

- 表的最大行大小约束列的数量（可能还有大小），因为所有列的总长度不能超过此大小。
- 各个列的存储要求限制了给定最大行大小内的列数。某些数据类型的存储要求取决于存储引擎、存储格式和字符集等因素。请参见。[Section 11.7, “Data Type Storage](https://dev.mysql.com/doc/refman/5.7/en/storage-requirements.html)。
- 存储引擎可能会施加限制表列计数的其他限制。例如，**`InnoDB` 限制为每个表 1017 列**。请参见第 14.23 节 “InnoDB 限制”。有关其他存储引擎的信息，请参见[Chapter 15, *Alternative Storage Engines*](https://dev.mysql.com/doc/refman/5.7/en/storage-engines.html).。