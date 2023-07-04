# CREATE unique index index_name ON table(column(8));
- 可以写 CREATE index index_name ON table(column(8));
- 可以写 CREATE unique index index_name ON table(column(8)); // 如果值重复则会创建索引失败
- 不可以写 CREATE unique index_name ON table(column(8)); // 不能省略 关键字 index
- 不可以写 CREATE index  ON user(username(8)); // 不能省略索引名字
- 不能创建两个索引名称相同的索引，即使索引字段的长度不同。
- 警告。创建两个索引名称不同，但实际上是相同的索引（只是名字不同）会警告。 （17:10:46	ALTER table user ADD UNIQUE (username(6))	0 row(s) affected, 1 warning(s): 1831 Duplicate index 'userName_8' defined on the table 'mybatis.user'. This is deprecated and will be disallowed in a future release. Records: 0  Duplicates: 0  Warnings: 1	0.015 sec）
- 可以在同一列创建多个名字不同，实际内容也不同的索引。例如：
  - CREATE index index_name ON table(column(8));
  - CREATE index index_name ON table(column(7));

# ALTER table user ADD UNIQUE index username_index_name(username(10));
- 可以写 ALTER table user ADD        index username_index_name(username(10));
- 可以写 ALTER table user ADD        index                    (username(10));
- 可以写 ALTER table user ADD UNIQUE index username_index_name(username(10));
- 可以写 ALTER table user ADD UNIQUE index                    (username(10));
- 可以写 ALTER table user ADD UNIQUE                          (username(10));
- 不能创建两个索引名称相同的索引，即使索引字段的长度不同。
- 不写索引名字，默认名字为 column 字段名，
  - 重复运行 不写索引名字的命令。索引名字按这种方式递增：username_index_name，username_index_name_1,username_index_name_2,……
  - 重复运行不告警。使用默认名字创建两个相同索引，不会警告。
  - 重复运行不告警。使用默认名字创建两个不同索引，当然更不会警告。



# 创建表的时候直接指定
可以省略索引名字，默认字段名字
```mysql
CREATE TABLE mytable(

ID INT NOT NULL,

username VARCHAR(16) NOT NULL,

INDEX [indexName] (username(length))

);  
```
C
# 删除索引的语法
不能省略索引名字
```mysql
DROP INDEX [indexName] ON mytable; 
```

# 联合索引
```mysql
 create index union_index_username_address on user(username, address); 

```

```mysql
show  index  from user;
```
发现有两条名字相同key_name的索引，但是seq_in_index 不同

### 删除索引
```mysql
drop index userName_9 on user;
alter table user drop index userName_10;
```