例子：
```mysql
START TRANSACTION;
CREATE TABLE mybatis.your_table (column1 int(32));
rollback;
```
即使 rollback 之后， 新建的表也不会删除。
