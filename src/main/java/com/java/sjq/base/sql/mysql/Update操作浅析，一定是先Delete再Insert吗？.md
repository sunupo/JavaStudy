[(108条消息) Update操作浅析，一定是先Delete再Insert吗？\_weixin\_34187822的博客-CSDN博客](https://blog.csdn.net/weixin_34187822/article/details/93817002)

**Update****操作一定是先Delete再Insert吗？**

Update在数据库中的执行是怎么样的？“Update操作是先把数据删除，然后再插入数据”。在网上看了很多也都是这么认为的。但在查阅到一些不同看法的时候我进行了一些验证，发现还有其它的情况。

这里我分三种情况来讲：

1、更改没有索引列的字段，更改前和更改后的[字符串长度](https://so.csdn.net/so/search?q=%E5%AD%97%E7%AC%A6%E4%B8%B2%E9%95%BF%E5%BA%A6&spm=1001.2101.3001.7020)一样；  
2、更改没有索引列的字段，更改后比更改前的[字符串](https://so.csdn.net/so/search?q=%E5%AD%97%E7%AC%A6%E4%B8%B2&spm=1001.2101.3001.7020)长；  
3、更改聚集索引字段。

先准备好数据，以便后面进行对比。

一、创建表、索引和数据：

```
--创建表MyTable1
IF EXISTS(SELECT * FROM sysobjects WHERE [name]='MyTable1' AND [type]='u')
    DROP TABLE MyTable1
GO
CREATE TABLE MyTable1
(
    ID     INT,
    SName  VARCHAR(20)  
);
--创建索引
CREATE UNIQUE CLUSTERED INDEX IX_ID ON MyTable1(ID);
 
INSERT INTO MyTable1 values( 1,'aaaa')
INSERT INTO MyTable1 values( 2,'bbbb')
INSERT INTO MyTable1 values( 3,'cccc')
GO
 
SELECT * FROM MyTable1 mt
GO
 
--创建表MyTable2
IF EXISTS(SELECT * FROM sysobjects WHERE [name]='MyTable2' AND [type]='u')
    DROP TABLE MyTable2
GO
CREATE TABLE MyTable2
(
    ID     INT,
    SName  VARCHAR(20)  
);
--创建索引
CREATE UNIQUE CLUSTERED INDEX IX_ID ON MyTable2(ID);
 
INSERT INTO MyTable2 VALUES ( 1,'aaaa')
INSERT INTO MyTable2 VALUES ( 2,'bbbb')
INSERT INTO MyTable2 VALUES ( 3,'cccc')
 
SELECT * FROM MyTable2 mt
```

二、查看数据库的ID号以及两个表对应的ID

```
--查看数据库的ID号以及两个表对应的ID
select db_id() AS '数据库ID',object_id('MyTable1')as '表MyTable1_ID',object_id('MyTable2')as '表MyTable2_ID'
```

查询结果如下：

![](https://images0.cnblogs.com/blog/37413/201408/212113442379575.jpg)

三、查看数据页的页码

```
--查看数据页的ID
DBCC extentinfo(6,213575799)--这里是刚刚查出来的数据库的ID，是表MyTable1的ID
DBCC extentinfo(6,229575856)--这里是刚刚查出来的数据库的ID，是表MyTable2的ID
```

查询结果如下：

![](https://images0.cnblogs.com/blog/37413/201408/212114432378734.jpg)

**表示MyTable1的数据存储在第45页，MyTable2的数据存储在第94页。**

四、查看2个表所在页面上每条记录的存储情况：

```
DBCC traceon(3604) WITH NO_INFOMSGS              --打开跟踪
DBCC IND('TestDB','MyTable1',0)               --列出所有页和索引。
                                              --参数说明，：数据库名；：表名；：索引的ID，表示堆，-1 表示显示所有索引和IAMs, -2表示只显示IAMs
 
DBCC PAGE(TestDB,1,45,1)                  --查看数据页和索引
                                             --参数说明，：数据库名；：数据页文件文件组编号；：数据页ID；：数据显示类型（，）
```

语句执行后我们得到下面的结果：

1.  MyTable1的Row – Offset：  
    
    ```
    OFFSET TABLE:
     
    Row - Offset                        
    2 (0x2) - 134 (0x86)                
    1 (0x1) - 115 (0x73)                
    0 (0x0) - 96 (0x60)  
    ```
    

      2、MyTable2的Row – Offset：

```
OFFSET TABLE:
 
Row - Offset                        
2 (0x2) - 134 (0x86)                 
1 (0x1) - 115 (0x73)                
0 (0x0) - 96 (0x60) 
```

**可以看到两个表的存储在数据库中数据页的位置是一样的。这是因为一个页只能放一个对象。**

# 五、下面我们来看第一种情况：更改没有索引列的字段，更改前和更改后的字符串长度一样；

```
UPDATE MyTable1 SET  SName = 'dddd' WHERE ID=2
OFFSET TABLE:
```

```
Row - Offset                        
2 (0x2) - 134 (0x86)                
1 (0x1) - 115 (0x73)                
0 (0x0) - 96 (0x60)     
```

## **发现他的存储位置没有发生改变。**

再来看MyTable2

```
--先删除后插入
DELETE FROM MyTable2 WHERE ID=2
INSERT INTO MyTable2(ID,SName)VALUES(2,   'dddd')
```

```
OFFSET TABLE:
 
Row - Offset                        
2 (0x2) - 134 (0x86)                
1 (0x1) - 153 (0x99)                
0 (0x0) - 96 (0x60)     
```

表MyTable2的存储发生变化了，原先在115和134之间存储的是第二条记录，现在这条记录却存储到了153个字节以后了，而原来115和134之间什么也没存储，这样这里就形成了内部碎片。对于这种update后数据的存储位置不发生变化的更新称为现场更新，如果位置发生了改变就称为非现场更新。

**所以对于这种情形来说：****update****操作并不是先****delete****后****insert****的。**

# 六、下面我们再来测试第二种情况：更改没有索引列的字段，更改后比更改前的字符串长；

先更新表MyTable1，再查看数据页的存储情况：

```
UPDATE MyTable1 SET SName='aaaaaa' WHERE ID=2
DBCC PAGE(TestDB,1,45,1)
```

存储结果如下：

```
OFFSET TABLE:
 
Row - Offset                        
2 (0x2) - 134 (0x86)                
1 (0x1) - 153 (0x99)                
0 (0x0) - 96 (0x60)  
```

## **这时我们看到他的存储和先Delete再Insert一样了。**

# 七、我们再来看第三种情况：更改聚集索引字段

为了避免对数据库的操作影响查看的难度，再执行一下创建表的语句。

数据更新之前的结果如下：

```
MyTable1：
OFFSET TABLE:
 
Row - Offset                        
2 (0x2) - 134 (0x86)                
1 (0x1) - 115 (0x73)                
0 (0x0) - 96 (0x60)  
```

```
MyTable2：
OFFSET TABLE:
 
Row - Offset                        
2 (0x2) - 134 (0x86)                
1 (0x1) - 115 (0x73)                
0 (0x0) - 96 (0x60)         
```

先对表MyTable1操作：更新ID（即更新聚集索引列）

```
UPDATE MyTable1 SET ID = 0 WHERE ID=2
DBCC PAGE(TestDB,1,94,1)   --数据页的位置已经发生改变
```

存储结果如下：

```
OFFSET TABLE:
 
Row - Offset                        
2 (0x2) - 134 (0x86)                
1 (0x1) - 153 (0x99)                
0 (0x0) - 96 (0x60)   
```

再来对MyTable2操作：

```
DELETE FROM MyTable2 WHERE ID = 2
INSERT INTO MyTable2(ID,SName)VALUES(2,   'bbbb')
DBCC PAGE(TestDB,1,126,1)  --数据页的位置已经发生改变
```

存储结果如下：

```
OFFSET TABLE:
 
Row - Offset                        
2 (0x2) - 134 (0x86)                
1 (0x1) - 153 (0x99)                
0 (0x0) - 96 (0x60)  
```

## 发现此时upadte为非现场更新，数据的存储位置已经发生了改变，和我们所想的先Delete再[Insert](https://so.csdn.net/so/search?q=Insert&spm=1001.2101.3001.7020)是一样的。

## 其实在更改聚集索引键列的时候，也可能发生现场更新。

比如有3条记录分别为1、2、5，我们把其中的2更改为了3，由于3是在1和5之间的数字，所以在更改为3后，这条记录还是会存储在1和5之间，所以就是现场更新了。

转载于:https://www.cnblogs.com/liuxinhuahao/p/3928116.html