[盘点MySQL中的各种锁](https://mp.weixin.qq.com/s/zlw4C5cQuZ27JsaaSIIydA)

## 前言

当多个线程并发访问某个数据的时候，尤其是针对一些敏感的数据（比如订单、金额等），我们就需要保证这个数据在任何时刻最多只有一个线程在访问，保证数据的完整性和一致性。在开发过程中加锁是为了保证数据的一致性，这个思想在数据库领域中同样很重要。MySQL中也存在各种各样的锁，本文对此做了一个盘点总结。

**重点△**：<u>在不同的事务隔离级别下，什么时候加什么锁？什么时候释放锁？</u>

## 一、按属性区分

MySQL根据加锁的属性分为共享锁和排他锁，主要是为了最大程度保证的高并发和安全性。

### 1. 共享锁

共享锁也成为读锁，针对同一份数据，多个事务的读操作可以同时进行而不会互相影响，相互不阻塞的。

-   通过下面命令加共享锁
    

```
SELECT...LOCK IN SHARE MODE#或SELECT...FOR SHARE;#(8.0新增语法)
```

### 2. 排他锁

排他锁也叫写锁，当一个事务对一份数据执行写入，即加上排他锁后，其他事务对同一份数据进行读写操作会阻塞，直到前一个事务提交。

-   通过下面的命令加排他锁
    

```
SELECT ... FOR UPDATE;
```

-   `DELETE`、`UPDATE`、`INSERT`等操作也相当于加排他锁
    

**共享锁和排他锁是否会发生阻塞如下图所示：**

|| **共享锁** | **排他锁** |
| --- | ---- | ---- |
| **共享锁** | 不阻塞 | 阻塞 |
| **排他锁** | 阻塞 | 阻塞 |

## 二、按锁粒度区分

为了尽可能提高数据库的并发度，每次锁定的数据范围越小越好理论上每次只锁定当前操作的数据的方案会得到最大的并发度，但是管理锁是很耗资源的事情（涉及获取、检查、释放锁等动作）。因此数据库系统需要在高并发响应和系统性能两方面进行平衡，这样就产生了“锁粒度”的概念。

### 行级锁

行级锁是锁住行，粒度小，性能较高，但是行级锁只在存储引擎层实现，行级锁分为3种，记录锁、间隙锁和临键锁。

假如下面的一个表`test_lock`:

| **id** | **a** | **b** |
| ------ | ----- | ----- |
| 0      | 0     | 0     |
| 4      | 4     | 4     |
| 8      | 8     | 8     |
| 16     | 16    | 16    |
| 32     | 32    | 32    |

-   id是主键索引
    
-   a是普通索引
    
-   b是普通列

>| 类型                                                         | 说明                                                         |
>| ------------------------------------------------------------ | ------------------------------------------------------------ |
>|                                                              | 唯一索引、二级索引，等值查询、范围查询，记录存在、记录不存在 |
>| **记录锁** **（Record Locks）**                              | 唯一索引进行等值查询时，且查询的记录是存在的时候，会加记录锁。 |
>| 间隙锁（Gap Locks）                                          | **唯一索引进行等值查询**时，当查询记录**不存在**时，会加间隙锁。<br />唯一索引使用范围查询的时候，会加间隙锁。<br />普通索引等值查询时，如果记录存在，会额外添加一个间隙锁。<br />普通索引等值查询时，如果记录不存在，会加一个间隙锁。 |
>| **临键锁（Next-Key Locks）**<br />**加锁都是按照临键锁加锁，<br />但是会根据一定的规律退化为记录锁和间隙锁** | **唯一索引等值查询：**<br /> 当查询的记录是存在的，临键锁会退化成「记录锁」。<br /> 当查询的记录是不存在的，临键锁 会退化成「间隙锁」。<br /><br />**非唯一索引等值查询：**<br />当查询的记录存在时，除了会加 <u>临键锁外，还额外加间隙锁</u>，也就是会加两把锁。 <br />当查询的记录不存在时，只会加 <u>临键锁，然后会退化为间隙锁</u>，也就是只会加一把锁。<br /><br />**非唯一索引和主键索引的范围查询的加锁规则不同之处在于：**<br /> 唯一索引在满足一些条件的时候，临键锁退化为间隙锁和记录锁。<br /> 非唯一索引范围查询，临键锁不会退化为间隙锁和记录锁。 |

#### 1. **记录锁** **（Record Locks）**

记录锁是仅仅锁住一条记录，锁的粒度最小。

##### **什么时候会加记录锁？**

当用唯一索引进行等值查询时，且查询的记录是存在的时候，会加记录锁。

| **会话1**| **会话2** | **会话3** |
| ---- | ----- | ---- |
| begin;select \* from test\_lock where id = 16 for update; |     |    |
 |   |update test\_lock set a = 100 where id = 16;(阻塞) | |
 | | | insert into test\_lock value(9, 9, 9);(正常) |

-   会话1对`id=16`记录加了行锁
    
-   会话2阻塞，无法对这条记录进行修改操作
    
-   会话3正常插入
    

#### 2. **间隙锁（Gap Locks）**

大家还记得并发事务中会出现"幻读"的问题吗？就是事务期间，其他事务添加一条数据，再次读取突然多出一条记录。为了解决这样的问题，我们是不是可以对一段区间的数据加锁，加上锁以后，其他事务添加数据时必须阻塞。像这样的锁就叫做间隙锁，即锁定一个区间，左开右开。

**什么情况会加** **间隙锁** **？**

-   在用**唯一索引进行等值查询**时，当查询记录**不存在**时，会加间隙锁。
    

```
select * from test_lock where id = 10 for update;
```

id=10位于8到16区间，由于10这条记录不存在，所以加的间隙锁，锁定`(8, 16)`的区间。

-   唯一索引使用范围查询的时候，会加间隙锁。
    

```
select * from test_lock where id <10 and id> 8 for update;
```

`id <10 and id> 8`是一个范围查询，会锁定范围`（8，16）`。

-   普通索引等值查询时，如果记录存在，会额外添加一个间隙锁。
    

```
select * from test_lock where a = 8 for update;
```

由于`a=8`记录存在，会对范围`（4，8]`**添加临键锁,**这个后面会提到，同时额外向下遍历到第一个不符合条件的值才能停止，因此间隙锁的范围是`(8,16)`。

-   普通索引等值查询时，如果记录不存在，会加一个间隙锁。
    

```
select * from test_lock where a = 10 for update;
```

此种情况锁定的范围为`(8,16)`

#### 3. **临键锁（Next-Key Locks）**

如果想要同时集合上面的记录锁和间隙锁，也就是既想锁住某条记录，又想阻止其他事务在该记录前边的间隙插入新记录，所以InnoDB就提出了临键锁（`Next-Key Locks`），默认锁定的范围是左开右闭。**`InnoDB`存储引擎默认的锁单位就是临键锁（`Next-Key Locks`)**，怎么理解呢？

也就是锁**加锁都是按照临键锁加锁，但是会根据一定的规律退化为记录锁和间隙锁**。具体规律如下：

**唯一索引等值查询：**

-   当查询的记录是存在的，临键锁会退化成「记录锁」。
    
-   当查询的记录是不存在的，临键锁 会退化成「间隙锁」。
    

**非唯一索引等值查询：**

-   当查询的记录存在时，除了会加 临键锁外，还额外加间隙锁，也就是会加两把锁。
    
-   当查询的记录不存在时，只会加 临键锁，然后会退化为间隙锁，也就是只会加一把锁。
    

**非唯一索引和主键索引的范围查询的加锁规则不同之处在于：**

-   唯一索引在满足一些条件的时候，临键锁退化为间隙锁和记录锁。
    
-   非唯一索引范围查询，临键锁不会退化为间隙锁和记录锁。
    

**InnoDB存储引擎中，如果一个表查询或者更新没有走索引，这时候还会创建行级锁吗？答案是不会，这时候会升级为表级锁。**

### 页级锁（BDB引擎）

页级锁是 MySQL 中锁定粒度介于行级锁和表级锁中间的一种锁。表级锁速度快，但冲突多，行级冲突少，但速度慢。因此，采取了折衷的页级锁，一次锁定相邻的一组记录。`BDB (BerkeleyDB)`存储引擎 支持页级锁。

**特点**

-   开销和加锁时间界于表锁和行锁之间
    
-   会出现死锁
    
-   锁定粒度界于表锁和行锁之间，并发度一般
    

### 表级锁

表锁会锁定整张表，它是MySQL中最基本的锁策略，并不依赖于存储引擎，表锁是开销最小的策略。因为表级锁一次会将整个表锁定，所以可以很好的避免死锁问题。但是表锁的并发度很差，那表锁都有哪几种呢？

#### 1. **表级别的S锁、X锁**

##### 加锁（手动）

-   `LOCK TABLES t READ` ：InnoDB存储引擎会对表 t 加表级别的 S锁 。
    
-   `LOCK TABLES t WRITE`：InnoDB存储引擎会对表 t 加表级别的 X锁 。

##### 解锁（手动）

- unlock tables ：解锁所有锁表。

表级别的S锁和X锁，只会在一些特殊情况下使用，比如崩溃恢复过程中用到。

#### 2. **意向锁**（IX，IS）

##### 加锁（自动）

- 一个事务给一个数据行加共享锁时，必须先获得表的 IS 锁。

- 一个事务给一个数据行加排他锁时，必须先获得该表的 IX 锁。

##### 解锁（自动）

- 应该是怎么加的就怎么释放。

意向锁也是一种表锁，表示某个事务正在锁定一行或者将要锁定一行，表明一个意图。它不与行级锁冲突。**那它究竟有啥作用？**

意向锁是在当事务加表锁时发挥作用。比如一个事务想要对表加排他锁，如果没有意向锁的话，那么该事务在加锁前需要判断当前表的每一行是否已经加了锁，如果表很大，遍历每行进行判断需要耗费大量的时间。如果使用意向锁的话，那么加表锁前，只需要判断当前表是否有意向锁即可，这样加快了对表锁的处理速度。

意向锁分为两种：

-   意向共享锁（intention shared lock, IS）：事务有意向对表中的某些行加共享锁（S锁）
    
-   意向排他锁（intention exclusive lock, IX）：事务有意向对表中的某些行加排他锁（X锁）

> 意向锁是有存储引擎自己的维护的，用户无法手动操作意向锁，在为数据行加共享/排它锁之前，InnoDB 会先获取该数据行所载数据表对应的意向锁。
>
> 意向锁的并发性：意向锁不会与行锁的共享/排它锁互斥，正因为如此，意向锁并不会影响到多个事务对不同数据行加排它锁时的并发性（不然直接用普通的表锁就行了）.IX、IS是表级锁，不会和行级的X，S锁发生冲突，只会和表级的X，S发生冲突。
>
> - 任意 IS/IX 锁之间都是兼容的，因为它们只表示想要对表加锁，而不是真正加锁；
> - 这里兼容关系针对的是表级锁，而表级的 IX 锁和行级的 X 锁兼容，两个事务可以对两个数据行加 X 锁。

#### 3. **自增锁（AUTO-INC LOCK）**

##### 加锁（自动）

我们都知道在使用创建表的时候有自增主键`AUTO_INCREMENT`属性，那它是怎么实现自增的呢？

`AUTO-INC`锁是当向使用含有`AUTO_INCREMENT`列的表中插入数据时需要获取的一种特殊的表级锁，在执行插入语句时就在表级别加一个`AUTO-INC`锁，然后为每条待插入记录的`AUTO_INCREMENT`修饰的列分配递增的值，在该语句执行结束后，再把`AUTO-INC`锁释放掉。

##### 解锁（自动）

当向使用含有`AUTO_INCREMENT`列的表中插入数据执行结束后，自动释放锁。

#### 4. **元数据锁（MDL锁）**

元数据锁可以用来保证读写的正确性。比如，如果一个查询正在遍历一个表中的数据，而执行期间另一个线程对这个 表结构做变更 ，增加了一列，那么查询线程拿到的结果跟表结构对不上，肯定是不行的。

##### 加锁（自动）

-   当对一个表做增删改查操作的时候，加 MDL读锁；
    
-   当要对表做结构变更操作的时候，加 MDL 写锁。
    

MDL读锁和读锁之间可以共享兼容，读锁和写锁之间不兼容，会互相阻塞。

> 对表进行ALTER TABLE、DROP TABLE这类的DDL语句时，其他事务对这个表并发执行增删改查就会发生阻塞。这个过程其实是通过Server层使用一种称为**元数据锁**来实现的

### 全局锁

全局锁就是对 整个数据库实例 加锁。当你需要让整个库处于 只读状态 的时候，可以使用这个命令，之后 其他线程的以下语句会被阻塞：数据更新语句（数据的增删改）、数据定义语句（包括建表、修改表结 构等）和更新类事务的提交语句。

全局锁的典型使用场是做全库逻辑备份。

```
## 全局锁命令flush tables with read lock
```

## 三、按加锁的态度区分

从对待锁的态度来看锁的话，可以将锁分成乐观锁和悲观锁，就像人看待一个事情的态度一样，有的人很乐观，有的人很悲观，这是一种设计思想。

### 悲观锁

悲观锁是总以最坏的情况假设，比如操作一条数据，总认为也有其他线程要拿这条数据，那就给这条数据上排他锁，让其他事务或者线程阻塞，类似于Java中 synchronized 和 ReentrantLock 等独占锁的思想。

**适用场景：**

悲观锁适**写操作多**的场景，因为写的操作具有 排它性 。采用悲观锁的方式，可以在数据库层 面阻止其他事务对该数据的操作权限，防止读 - 写和写 - 写的冲突。

**实现思路：**

以秒杀商品为例，为了防止超卖，需要加锁。

```
#第1步：for update 方式查出商品库存select quantity from items where id 1001 for update;#第2步：如果库存大于0，则根据商品信息生产订单insert into orders (item_id)values(1001);#第3步：修改商品的库存，um表示购买数量update items set quantity quantity-num where id 1001;
```

`select····for update`是MySQL中悲观锁。此时在items表中，id为1001的那条数据就被我们锁定了，其他的要执行`select quantity from items where id=1001 for update;`语句的事务必须等本次事务提交之后才能执行。这样我们可以保证每次事务能拿到最新的库存数量，从而不会超卖，但是这样的性能很差。

### 乐观锁

乐观锁认为一个事务发生并发的概率很小，就不加通过数据库加锁实现，因为加锁性能比较差，而是通过程序实现，那如何数据没有被其他事务修改了呢？会在更新数据的时候判断数据的版本或者时间戳是否发生变化。

**实现思路：**

1.  **版本号机制实现乐观锁**
    

-   数据表中新增一个version字段
    
-   更新前读取出version字段
    
-   进行业务逻辑操作，更新数据， `UPDATE ... SET version=version+1 WHERE version=version`，版本+1
    
-   如果有其他线程更新了数据，那么上面的修改不成功，返回值为0，表示已经被人更新了，我们可以根据0去做业务操作
    

2.  **时间戳机制实现乐观锁**
    

时间戳和版本号机制一样，也是在更新提交的时候，将当前数据的时间戳和更新之前取得的时间戳进行 比较，如果两者一致则更新成功，否则就是版本冲突。

**适用场景：**

乐观锁适合**读操作多**的场景，相对来说写的操作比较少。它的优点在于 程序实现 ，不存在死锁问题。



## 四、不同事务隔离级别下的加锁情况：

| 事务隔离级别 | 读         | 加锁、解锁时间                                               | 写           | 加锁、解锁时间                                               |
| ------------ | ---------- | ------------------------------------------------------------ | ------------ | ------------------------------------------------------------ |
| 读未提交  RU | X          | X                                                            | 行级共享锁   | 修改的时候加锁<br />修改结束解锁<br />                       |
| 读已提交 RC  | 行级共享锁 | 读到时才加锁<br />读完该行，立即释放<br />（二阶封锁协议，解决脏读，防止丢失修改） | 行级排他锁   | 更新瞬间加锁<br />事物结束解锁<br />（一级封锁协议，防止丢失修改） |
| 可重复读 RR  | 行级共享锁 | 读取瞬间加锁<br />事物结束释放<br />（三级封锁协议，解决不可重复读，脏读，丢失修改） | 行级排他锁   | 更新瞬间加锁<br />事物结束解锁                               |
| 串行化       | 表级共享锁 | 读取瞬间加锁<br />事务结束解锁                               | 表级共排它锁 | 更新瞬间加锁<br />事务结束解锁<br />                         |

## 五、封锁协议

### 一级封锁协议

一级封锁协议：事务T在修改数据A之前必须对其加X锁，直到事务结束才释放。事务结束包括正常结束(Commit)和非正常结束(RollBack)。

一级封锁协议可防止**丢失修改**。

> 使用一级封锁协议解决了**覆盖丢失**问题。事务T1在读A进行修改之前先对A加X锁，当T2再请求对A加X锁时被拒绝，T2只能等待T1释放A上的锁后T2获得A上的X锁，这时它读取的A已经是T1修改后的15，再按照此值进行计算，将结果值A=14写入磁盘。这样就避免了丢失T1的更新。
>
> ————————————————
> 版权声明：本文为CSDN博主「曲首向皓月」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
> 原文链接：https://blog.csdn.net/weixin_36182568/article/details/114891489

### 二级封锁协议

二级封锁协议：一级封锁协议加上事务T在读取数据A之前必须先对其加S锁，读完后即可释放S锁。

二级封锁协议除防止了**丢失修改**，还进一步防止了读**“脏”数据**。

>  使用二级封锁协议解决了脏读问题。事务T1在读C进行修改之前先对C加X锁,修改其值后写回磁盘。这时T2请求在C上加S锁，因为T1在C上已经加了X锁，所以T2只能等待。T1因为某种原因被撤销，C恢复原值100。T1释放C上的X锁后T2获得C上的S锁，读C=100。这样就避免了读“脏”数据。
> ————————————————
> 版权声明：本文为CSDN博主「曲首向皓月」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
> 原文链接：https://blog.csdn.net/weixin_36182568/article/details/114891489

### 三级封锁协议

三级封锁协议：一级封锁协议加上事务T在读取数据A之前必须先对其加S锁，直到事务结束才释放。

三级封锁协议除防止了丢失修改和读“脏”数据，还进一步防止了不可重复读。

>  使用三级封锁协议解决了不可重复读问题。事务T1在读取数据A和数据B之前对其加S锁，其他事务只能再对A、B加S锁，不能加X锁，这样其他事务只能读取A、B,而不能更改A、B。这时T2请求在B上加X锁，因为T1已经在B上加了S锁，所以T2只能等待。T1为了验算结果再次读取A、B的值，因为其他事务无法修改A、B的值，所以结果仍然为150，即可重复读。此时T1释放A、B上的S锁，T2才获得B上的X锁。这样就避免了不可重复读。
> ————————————————
> 版权声明：本文为CSDN博主「曲首向皓月」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
> 原文链接：https://blog.csdn.net/weixin_36182568/article/details/114891489

## 六、活锁与死锁

封锁可能会引起活锁和死锁。

### 活锁

#### 内容

如果事务T1封锁了数据R，事务T2又请求封锁数据R，于是T2等待。事务T3也请求封锁R，当事务T1释放了数据R上的封锁之后系统首先批准了事务T3的封锁请求，T2仍然等待。然后T4又申请封锁R，当T3释放了R的封锁之后系统又批准了T4的封锁请求。T2有可能一直等待下去，这就是活锁。

#### 活锁预防

**避免活锁**的方法就是**先来先服务**的策略。当多个事务请求对同一数据对象封锁时，封锁子系统按照请求的先后对事务排队。数据对象上的锁一旦释放就批准申请队列中的第一个事务获得锁。

### 死锁

#### 内容

如果事务T1封锁了数据R1，事务T2封锁了数据R2,然后T1又请求封锁数据R2，因为T2已经封锁了数据R2，于是T1等待T2释放R2上的锁。接着T2又申请封锁R1，因为因为T1已经封锁了数据R1，T2也只能等待T1释放R1上的锁。这样就出现了T1在等待T2，T2也在等待T1的局面，T1和T2两个事务永远不能结束，形成死锁。
#### 死锁的预防：

##### ①一次封锁法

一次封锁法要求事务必须一次将所有要使用的数据全部加锁，否则不能继续执行。例如上图中的事务T1将数据R1和R2一次加锁，T1就能执行下去，而T2等待。T1执行完成之后释放R1，R2上的锁，T2继续执行。这样就不会产生死锁。

一次封锁法虽然能防止死锁的发生，但是缺点却很明显。一次性将以后要用到的数据加锁，势必扩大了封锁的范围，从而降低了系统的并发度。

##### ②顺序封锁法

顺序封锁法是预先对数据对象规定一个封锁顺序，所有的事务都按照这个顺序实行封锁。

顺序封锁法虽然可以有效避免死锁，但是问题也很明显。第一，数据库系统封锁的数据对象极多，并且随着数据的插入、删除等操作不断变化，要维护这样的资源的封锁顺序非常困难，成本很高。第二，事务的封锁请求可以随着事务的执行动态的确定，因此很难按照规定的顺序实行封锁。

**可见，预防死锁的产生并不是很适合数据库的特点，所以在解决死锁的问题上普遍采用的是诊断并且解除死锁。**

#### 死锁的诊断与解除：

##### ①超时法

如果一个事务的等待时间超过了默认的时间，就认为是产生了死锁。

##### ②等待图法

一旦检测到系统中存在死锁就要设法解除。通常的解决方法是选择一个处理死锁代价最小的事务，将其撤销，释放此事务持有的所有的锁，恢复其所执行的数据修改操作，使得其他事务得以运行下去。

## 七、两段锁协议

所谓的二段锁协议是指所有事务必须分两个阶段对数据进行加锁和解锁操作。

在对任何数据进行读、写操作之前，首先要申请并获得该数据的封锁。

在释放一个封锁之后，事务不在申请和获得其他封锁。

也就是说事务分为两个阶段。第一个阶段是获得封锁，也称为扩展阶段。在这个阶段，事务可以申请获得任何数据项任何类型的锁，但是不能释放任何锁。第二阶段是释放封锁，也称为收缩阶段。在这个阶段，事务可以释放任何数据项上任何类型的封锁，但是不能再申请任何锁。

事务遵守两段锁协议是可串行化调度的充分条件，而不是必要条件。也就是说遵守两段锁协议一定是可串行化调度的，而可串行化调度的不一定是遵守两段锁协议的。

```html
lock-x(A)...lock-s(B)...lock-s(C)...unlock(A)...unlock(C)...unlock(B)  满足两段锁协议，它是可串行化调度。
```

```html
lock-x(A)...unlock(A)...lock-s(B)...unlock(B)...lock-s(C)...unlock(C)  不满足两段锁协议，但它还是可串行化调度。
```

###  两段锁协议和一次封锁法的异同

一次封锁法要求事务必须将要使用的数据全部加锁，否则不能继续执行。因此一次封锁法遵守两段锁协议。

但是两段锁协议并不要求事务将要使用的数据一次全部加锁，**因此两段锁协议可能发生死锁**。

## 八-1、[Mysql 查询锁表](https://blog.csdn.net/qq_34185638/article/details/128777469)

1.查看表是否被锁：  
（1）直接在mysql命令行执行:show engine innodb status。

（2）查看造成死锁的sql语句，分析索引情况，然后优化sql。

（3）然后show processlist,查看造成死锁占用时间长的sql语句。

（4）show status like ‘%lock%’。

2.查看表被锁状态和结束死锁步骤  
（1）查看表被锁状态：show OPEN TABLES where In\_use > 0; 这个语句记录当前锁表状态 。

（2）查询进程：show processlist查询表被锁进程；查询到相应进程killid。

（3）分析锁表的SQL：分析相应SQL，给表加索引，常用字段加索引，表关联字段加索引。

（4）查看正在锁的事物：SELECT \* FROM INFORMATION\_SCHEMA.INNODB\_LOCKS。

（5）查看等待锁的事物：SELECT \* FROM INFORMATION\_SCHEMA.INNODB\_LOCK\_WAITS。

## 八-2、[如何查看mysql死锁 ](https://www.pianshen.com/article/8056142045/)

show OPEN TABLES where In_use > 0; 这个语句记录当前锁表状态 

另外可以打开慢查询日志，

linux下打开需在my.cnf的[mysqld]里面加上以下内容：

```
slow_query_log=TRUE(有些mysql版本是ON)

slow_query_log_file=/usr/local/mysql/slow_query_log.txt

long_query_time=3
```

Windows：

在my.ini配置文件的[mysqld]选项下增加：

```
slow_query_log=TRUE

slow_query_log_file=c:/slow_query_log.txt

long_query_time=3
```



添加完成之后记得一定要重启mysql服务才能生效记录输出。

最后在MySQL客户端中输入命令：

```
show variables like '%quer%'; 核查一哈是否Ok
```



## 总结

本文对MySQL中的锁从不同的维度做了一个整体的区分介绍，对于InnoDB引擎来说，行级锁非常重要，它默认采用临键锁，当然如果这条语句没有走索引，那么它会直接升级成表锁，锁住整个表，导致性能很糟糕。所以合理的创建索引极为重要。

> [【史上最全】MySQL各种锁详解：一文搞懂MySQL的各种锁](https://mp.weixin.qq.com/s/mSWS6nCFadjlSohsK_7iqA)
>
> [(113条消息) mysql的封锁协议\_【眼见为实】数据库并发问题 封锁协议 隔离级别\_曲首向皓月的博客-CSDN博客](https://blog.csdn.net/weixin_36182568/article/details/114891489)
>
> 



