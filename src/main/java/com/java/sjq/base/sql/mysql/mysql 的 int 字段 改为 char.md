[如何将mysql字段类型从INT更改为VARCHAR会影响以前存储为INT的数据 |](https://qa.1r1g.com/sf/ask/522592101/)

> 我需要将生产数据库中的字段从INT更新为VARCHAR.
>
> 当前INT字段中的信息是敏感的,我想确保在更改表类型时不会无意中更改它或销毁它.例如,用户可能在此列中存储了INT 123456,并且我希望确保在转换为VARCHAR后准确存储.
>
> 实现此目的的推荐流程是什么？
>
> 使用这样的东西时有什么我需要担心的吗？
>
> ```
> ALTER TABLE table_sample CHANGE col_sample col_sample VARCHAR;
> ```
>
> 在此先感谢您的帮助.

[mu \*\_\*ort](https://qa.1r1g.com/sf/users/33590441/ "mu *_*ort") 54

在更改列类型之前,让MySQL服务器进入严格模式,并确保`varchar(n)`列的大小足以`n`在转换为字符串时保存所有整数.如果您没有处于严格模式,那么MySQL将[默默地截断您的数据以适合您的字符串大小](http://dev.mysql.com/doc/refman/5.6/en/char.html):

> 如果未启用严格SQL模式并且您为超出列的最大长度的值`CHAR`或`VARCHAR`列分配值,则会截断该值以适合并生成警告.对于非空格字符的截断,可以导致发生错误(而不是警告),并通过使用严格的SQL模式来禁止插入值.

但如果你先进入[严格模式](http://dev.mysql.com/doc/refman/5.6/en/server-sql-mode.html#sqlmode_strict_all_tables):

```
mysql> set sql_mode = 'STRICT_ALL_TABLES';
mysql> alter table table_sample change col_sample col_sample varchar(6);
```

你会得到一个很好的错误信息,如下所示:

```
ERROR 1406 (22001): Data too long for column 'col_sample' at row ...
```

如果你的整数不适合你的整数`varchar`.

当然,在尝试更改表之前,您将获得数据库的全新验证备份.并通过_验证_我的意思是,你已经成功恢复您的备份到测试数据库.

___



[mysql修改自增长主键int类型为char类型示例][mysql修改自增长主键int类型为char类型示例\_MySQL-mysql教程-PHP中文网](https://www.php.cn/mysql-tutorials-63773.html)

原来有一个表中的主键是int自增长类型，  
因为业务变化需要把int改成char类型的主键。同时因为原来的表中已经存在了数据，不能删除表重建，只能修改表结构。  

- 首先去掉自增长属性：  
  alter table table\_name change indexid indexid int;  
- 然后去掉主键：  
  ALTER TABLE table\_name DROP primary key;  
- 修改表结构为char类型：  
  alter table table\_name change indexid indexid char(18);  
- 最后重新添加主键：  
  alter table table\_name add primary key(indexid);