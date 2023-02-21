# 写操作执行过程

如果这条sql是写操作([insert](https://so.csdn.net/so/search?q=insert&spm=1001.2101.3001.7020)、update、delete)，那么大致的过程如下，其中引擎层是属于 InnoDB 存储引擎的，因为InnoDB 是默认的存储引擎，也是主流的，所以这里只说明 InnoDB 的引擎层过程。由于写操作较查询操作更为复杂，所以先看一下写操作的执行图。方便后面解析。

![b1961d6919afc1a5420dfb28b8801d5c.png](https://img-blog.csdnimg.cn/img_convert/b1961d6919afc1a5420dfb28b8801d5c.png)

# 0x01: 组件介绍

## Server层

### 1、连接器

1)负责与客户端的通信，是半双工模式，这就意味着某一固定时刻只能由客户端向服务器请求或者服务器向客户端发送数据，而不能同时进行。

2)验证用户名和密码是否正确(数据库mysql的user表中进行验证)，如果错误返回错误通知(deAcess nied for user 'root'@'localhost'(using password：YES))，如果正确，则会去 mysql 的权限表(mysql中的 user、db、columns\_priv、Host 表，分别存储的是全局级别、数据库级别、表级别、列级别、配合 db 的数据库级别)查询当前用户的权限。

### 2、缓存(Cache)

也称为查询缓存，存储的数据是以键值对的形式进行存储，如果开启了缓存，那么在一条查询sql语句进来时会先判断缓存中是否包含当前的sql语句键值对，如果存在直接将其对应的结果返回，如果不存在再执行后面一系列操作。如果没有开启则直接跳过。

#### 相关操作：

查看缓存配置：show variables like 'have\_query\_cache';

查看是否开启：show variables like 'query\_cache\_type';

查看缓存占用大小：show variables like 'query\_cache\_size';

查看缓存状态信息：show status like 'Qcache%';

![965a4963d3605cc6462664e945067958.png](https://img-blog.csdnimg.cn/img_convert/965a4963d3605cc6462664e945067958.png)

#### 相关参数的含义：

![18a28c502810c84269933a66a478735b.png](https://img-blog.csdnimg.cn/img_convert/18a28c502810c84269933a66a478735b.png)

#### 缓存失效场景：

1、查询语句不一致。前后两条查询SQL必须完全一致。

2、查询语句中含有一些不确定的值时，则不会缓存。比如 now()、current\_date()、curdate()、curtime()、rand()、uuid()等。

3、不使用任何表查询。如 select 'A';

4、查询 mysql、information\_schema 或 performance\_schema 数据库中的表时，不会走查询缓存。

5、在存储的函数，触发器或事件的主体内执行的查询。

6、如果表更改，则使用该表的所有高速缓存查询都变为无效并从缓存中删除，这包括使用 MERGE 映射到已更改表的表的查询。一个表可以被许多类型的语句改变，如 insert、update、delete、truncate rable、alter table、drop table、drop database。

通过上面的失效场景可以看出缓存是很容易失效的，所以如果不是查询次数远大于修改次数的话，使用缓存不仅不能提升查询效率还会拉低效率(每次读取后需要向缓存中保存一份，而缓存又容易被清除)。所以在 MYSQL5.6默认是关闭缓存的，并且在 8.0 直接被移除了。当然，如果场景需要用到，还是可以使用的。

#### 开启

在配置文件(linux下是安装目录的cnf文件，windows是安装目录下的ini文件)中，增加配置：query\_cache\_type = 1

关于 query\_type\_type 参数的说明：

指定 **SQL\_NO\_CACHE**：select SQL\_NO\_CACHE \* from student where age >20; SQL\_CACHE 同理。

### 3、分析器

对客户端传来的 sql 进行分析，这将包括预处理与解析过程，并进行关键词的提取、解析，并组成一个解析树。具体的解析词包括但不局限于 select/update/delete/or/in/where/group by/having/count/limit 等，如果分析到语法错误，会直接抛给客户端异常：ERROR:You have an error in your SQL syntax.

比如：select \* from user where userId =1234;

在分析器中就通过语义规则器将select from where这些关键词提取和匹配出来,mysql会自动判断关键词和非关键词，将用户的匹配字段和自定义语句识别出来。这个阶段也会做一些校验:比如校验当前数据库是否存在user表，同时假如User表中不存在userId这个字段同样会报错：unknown column in field list.

### 4、优化器

进入优化器说明sql语句是符合标准语义规则并且可以执行。优化器会根据执行计划选择最优的选择，匹配合适的索引，选择最佳的方案。比如一个典型的例子是这样的：

表T,对A、B、C列建立联合索引(A,B,C)，在进行查询的时候，当sql查询条件是:select xx where B=x and A=x and C=x.很多人会以为是用不到索引的，但其实会用到,虽然索引必须符合最左原则才能使用,但是本质上,`优化器会自动将这条sql优化为:where A=x and B=x and C=X,这种优化会为了底层能够匹配到索引，`同时在这个阶段是自动按照执行计划进行预处理,mysql会计算各个执行方法的最佳时间,最终确定一条执行的sql交给最后的执行器

### 5、执行器

执行器会调用对应的存储引擎执行 sql。主流的是MyISAM 和 Innodb。

![6ac4bb3daf5a3b2fae52f4b5cba2b2d0.png](https://img-blog.csdnimg.cn/img_convert/6ac4bb3daf5a3b2fae52f4b5cba2b2d0.png)

## 存储引擎(InnoDB)层

### 1、undo log 与 MVCC

undo log是 Innodb 引擎专属的日志，是记录每行数据事务执行前的数据。主要作用是用于实现MVCC版本控制，保证事务隔离级别的读已提交和读未提交级别。而 MVCC 相关的可以参考 MySQL中的事务原理和锁机制。

2、redo log 与 Buffer Pool

InnoDB 内部维护了一个缓冲池，用于减少对磁盘数据的直接IO操作，并配合 redo log 来实现异步的落盘，保证程序的高效执行。redo log 大小固定，采用循环写

关于 Buffer Pool详情可查看博客 InnoDB 中的缓冲池(Buffer Pool)。

3、bin log(Server 层)

redo log 因为大小固定，所以不能存储过多的数据，它只能用于未更新的数据落盘，而数据操作的备份恢复、以及主从复制是靠 bin log(如果数据库误删需要还原，那么需要某个时间点的数据备份以及bin log)。5.7默认记录的是修改后的行记录。

在更新到数据页缓存或者 Change Buffer 后，首先进行 redo log 的编写，此时 redo log 处于 prepare 状态，随后再进行 bin log 的编写，等到 bin log 也编写完成后再将 redo log 设置为 commit 状态。这是为了防止数据库宕机导致 bin log 没有将修改记录写入，后面数据恢复、主从复制时数据不一致。当数据库启动后如果发现 redo log 为 prepare 状态，那么就会检查 bin log 与 redo log 最近的记录是否对的上，如果对的上就提交，对不上就进行事务回滚。

三种格式：

1、Row(5.7默认)。记录被修改后的行记录。缺点是占空间大。优点是能保证数据安全，不会发生遗漏。

2、Statement。记录修改的 sql。缺点是在 mysql 集群时可能会导致操作不一致从而使得数据不一致(比如在操作中加入了Now()函数，主从数据库操作的时间不同结果也不同)。优点是占空间小，执行快。

3、Mixed。会针对于操作的 sql 选择使用Row 还是 Statement。缺点是还是可能发生主从不一致的情况。

三个日志的比较(undo、redo、bin)

1、undo log是用于事务的回滚、保证事务隔离级别读已提交、可重复读实现的。redo log是用于对暂不更新到磁盘上的操作进行记录，使得其可以延迟落盘，保证程序的效率。bin log是对数据操作进行备份恢复(并不能依靠 bin log 直接完成数据恢复)。

2、undo log 与 redo log 是存储引擎层的日志，只能在 InnoDB 下使用；而bin log 是 Server 层的日志，可以在任何引擎下使用。

3、redo log 大小有限，超过后会循环写；另外两个大小不会。

4、undo log 记录的是行记录变化前的数据；redo log 记录的是 sql 或者是数据页修改逻辑或 sql(个人理解)；bin log记录的是修改后的行记录(5.7默认)或者sql语句。

0x02: 执行过程

写操作

通过上面的分析，可以很容易地了解开始的更新执行图。这里就不过多阐述了。

读操作

查询的过程和更新比较相似，但是有些不同，主要是来源于他们在查找筛选时的不同，更新因为在查找后会进行更新操作，所以查询这一行为至始至终都在缓冲池中(使用到索引且缓冲池中包含数据对应的数据页)。而查询则更复杂一些。

Where 条件的提取

在 MySQL 5.6开始，引入了一种索引优化策略——索引下推，其本质优化的就是 Where 条件的提取。Where 提取过程是怎样的？用一个例子来说明，首先进行建表，插入记录。

那么执行 select \* from tbl\_test where b >= 2 and b0 and d != 2 and e != 'a'; 在提取时，会将 Where 条件拆分为Index Key(First Key & Last Key)、Index Filter 与 Table Filter。

1、Index Key

用于确定 SQL 查询在索引中的连续范围(起始点 + 终止点)的查询条件，被称之为Index Key；由于一个范围，至少包含一个起始条件与一个终止条件，因此 Index Key 也被拆分为 Index First Key 和 Index Last Key，分别用于定位索引查找的起始点以终止点

Index First Key

用于确定索引查询范围的起始点；提取规则：从索引的第一个键值开始，检查其在 where 条件中是否存在，若存在并且条件是 =、>=，则将对应的条件加入Index First Key之中，继续读取索引的下一个键值，使用同样的提取规则；若存在并且条件是 >，则将对应的条件加入 Index First Key 中，同时终止 Index First Key 的提取；若不存在，同样终止 Index First Key 的提取

针对 SQL：select \* from tbl\_test where b >= 2 and b0 and d != 2 and e != 'a'，应用这个提取规则，提取出来的 Index First Key 为 b >= 2, c > ，由于 c 的条件为 >，提取结束

Index Last Key

用于确定索引查询范围的终止点，与 Index First Key 正好相反；提取规则：从索引的第一个键值开始，检查其在 where 条件中是否存在，若存在并且条件是 =、

针对 SQL：select \* from tbl\_test where b >= 2 and b0 and d != 2 and e != 'a'，应用这个提取规则，提取出来的 Index Last Key为 b

2、Index Filter

在完成 Index Key 的提取之后，我们根据 where 条件固定了索引的查询范围，那么是不是在范围内的每一个索引项都满足 WHERE 条件了 ？很明显 4,,5 ， 2,1,2 均属于范围中，但是又均不满足SQL 的查询条件

所以 Index Filter 用于索引范围确定后，确定 SQL 中还有哪些条件可以使用索引来过滤；提取规则：从索引列的第一列开始，检查其在 where 条件中是否存在，若存在并且 where 条件仅为 =，则跳过第一列继续检查索引下一列，下一索引列采取与索引第一列同样的提取规则；若 where 条件为 >=、>、、

针对 SQL：select \* from tbl\_test where b >= 2 and b0 and d != 2 and e != 'a'，应用这个提取规则，提取出来的 Index Filter 为 c > and d != 2 ，因为索引第一列只包含 >=、

3、Table Filter

这个就比较简单了，where 中不能被索引过滤的条件都归为此中；提取规则：所有不属于索引列的查询条件，均归为 Table Filter 之中

针对 SQL：select \* from tbl\_test where b >= 2 and b0 and d != 2 and e != 'a'，应用这个提取规则，那么 Table Filter 就为 e != 'a'

在5.6 之前，是不分 Table Filter 与 Index Filter 的，这两个条件都直接分配到 Server 层进行筛选。筛选过程是先根据 Index Key 的条件先在引擎层进行初步筛选，然后得到对应的主键值进行回表查询得到初筛的行记录，传入 Server 层进行后续的筛选，在 Server 层的筛选因为没有用到索引所以会进行全表扫描。而索引下推的优化就是将 Index Filter 的条件下推到引擎层，在使用 Index First Key 与 Index Last Key 进行筛选时，就带上 Index Filter 的条件再次筛选，以此来过滤掉不符合条件的记录对应的主键值，减少回表的次数，同时发给 Server 层的记录也会更少，全表扫描筛选的效率也会变高。下面是未使用索引下推和使用索引下推的示意图。

![f8651694dbbceedf7fdbcce6b84cb9f9.png](https://img-blog.csdnimg.cn/img_convert/f8651694dbbceedf7fdbcce6b84cb9f9.png)

![eed616995e3d8703fa7eb7bcd92777b7.png](https://img-blog.csdnimg.cn/img_convert/eed616995e3d8703fa7eb7bcd92777b7.png)

从上面的分析来看，查询的流程图大致可以用下面这张图来概括

![22bcba6d2b65e3889e959cae9919e57b.png](https://img-blog.csdnimg.cn/img_convert/22bcba6d2b65e3889e959cae9919e57b.png)

这里要注意的是如果在一开始没有用到索引，会依次将磁盘上的数据页读取到缓冲池中进行查询。