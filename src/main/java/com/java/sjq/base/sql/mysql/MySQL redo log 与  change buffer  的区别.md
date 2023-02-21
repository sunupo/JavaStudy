## 0、先回顾一条语句的写入流程是怎样子的？经过了哪些环节？

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/615ed2814b09479b9c7b783485ba74da~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

## 1、为什么要讲 `change buffer` 和 `redo log` 呢？

因为我在看这块相关的东西的时候，第一遍和第二遍都会把 `change buffer` 和 `redo log` 搞混。 `binlog` 一般大家都知道是干嘛的，数据同步嘛。 `redo log` 是干嘛的呢？这是一块内存空间还是磁盘空间？为什么还需要 `change buffer` ？它是为谁承载的呢？这几个东西在初接触时比较容易头晕。

## 2、名词解释1.0

### 2.1、什么是两阶段提交？

我们来看下一条语句执行的细节

![mysql核心剖析系列-两阶段提交](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cdbc81faf13640fd9dd1eb5e7edfe1b4~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

两阶段提交最好的理解就是从字面意思去理解，数据分为两个阶段提交写入。第一次写，第二次确认。

### 2.2、什么是 `redo log` ？

#### 2.2.1、名词解释

这里说了。**[【Mysql核心剖析系列】MVCC是怎么保障一致性视图的？](https://link.juejin.cn/?target=https%3A%2F%2Fmp.weixin.qq.com%2Fs%2Fxas_fp0gqpVoIuT0JZw3-g "https://mp.weixin.qq.com/s/xas_fp0gqpVoIuT0JZw3-g")**

#### 2.2.2、特性

`redo log` 是固定大小的，且是可以设置的，同时是顺序写入模式。有几个关键的配置项：

-   `innodb_log_buffer_zise`。缓冲区大小
-   `innodb_log_file_in_group`。文件组数量
-   `innodb_log_file_size`。文件大小

![image-20210902080224041](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7e50ce6f340f4254a5c64d1486da5da0~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

![image-20210902080316236](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c96a38d3ef5245fcb2230b7ad91f04e7~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

`redo log` 的写入还有一个特征就是环形覆盖。`redo log` 的容量总是有限的，如果写满了咋办呢？继续往下看

### 2.3、什么是 `redo log buffer` ？

`redo log buffer` 是承接在 `redo log` 前面的一块内存缓冲区域。引擎写 `redo log` 并不会直接写磁盘，而是写 `redo log buffer`，后期再由 `redo log buffer` 刷到磁盘。这里的 `redo log buffer` 是用户空间的缓冲区，写磁盘之前还经过了一层内核缓冲区（`os buffer`）。写磁盘的流程是这样子的:

![d](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e0ae312ba2f84f728b8d4bd243e477f8~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

**为什么这么做？** 还是为了追求1、写速度；2、同步改为了异步。同时写buffer刷磁盘也支持几种策略（通过参数 `innodb_flush_log_at_trx_commit` 配置）：

1.  延迟写，实时刷。固定每秒一次写 `os buffer` 并同步刷磁盘（由后台线程完成）
2.  实时写，实时刷。每次写 `redo log buffer` 都会 写 `os buffer` 且同步刷磁盘
3.  实时写，延迟刷。每次写 `os buffer` 并每秒一次刷磁盘

## 3、为什么要有两阶段提交？

### 3.1、两阶段提交解决了什么问题？

**`crash safe`**。两阶段、三阶段都是为了解决分布式事务中的数据一致性问题，既然是分布式事务，那肯定是分布式的，如果是单体的，那就用不上这玩意儿。比如说我就起一个 mysql 实例，用不上 `binlog`。 `redo log` 写一次就可以了。数据库崩溃重启后 仅仅用 `redo log` 记录来恢复即可。

两阶段提交的作用是为了保证 `redo log` 跟 `binlog` 数据的一致性（并不是完全一致，会有逻辑判断是往前补还是往后退）。防止数据库崩溃重试恢复过程中的数据不一致。因为**主库是用 `redo log` 来恢复，从库是用 `binlog` 来同步的。**

### 3.2、`redo log` 是怎么保证 `crash safe` 的？

#### 3.2.1、`redo log` 原理

执行 `show engine innodb status` 可以看到 `redo log` 的信息。

-   `Log sequence number` ：当前 `redo log` 的最新 `LSN`
-   `Log flushed up to` ：当前已经刷新到磁盘上 `redo log` 的 `LSN`
-   `Last checkpoint at` ：最后一次检查点的 `LSN`

![image-20210902143232591](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/15a498e5b29a4358aabe5220faa352cd~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

![mysql核心剖析系列-redo log .png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/59d19d6ad957450c963dd1a64726e0aa~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

`redo log` 日志文件默认是两个，可以设置。InnoDB 通过 `Log Sequence Number` , `LSN` 来用于记录日志序号，`LSN` 几乎无处不在。通过它可以定位到日志文件中的位置。

上图有两个点哈，因为 `redo log` 大小是固定的，所以总有写满的时候。那写满了怎么办，总不能直接把数据丢了吧。所有就有了这个类似于环的结构。日志一直写，当写完 `ib_logfile1` 最后一个坑之后，会继续从 `ib_logfile0` 的首位开始写。那原来 `ib_logfile0` 首位的数据是不是得删掉，那么怎么知道要删哪些数据？这就是 `checkpoint` 要做的事儿。 `checkpoint` 表示一个临界点，它后面的数据已经不重要了（已经落到库磁盘了），可以随意处置。 `redo log` 通过这两个点来实现无限续写，但是有个问题：

1.  如果 `checkpoint` 被最新的日志赶上了呢，就是数据还没写到磁盘，可能在 `change buffer` 里，或者在 `buffer pool` 里，但还没落盘。这时候如果被覆盖了，奔溃后是没法恢复数据的。所以当 `checkpoint` 被最新的日志追上的时候，会停下来，推着 `checkpoint` 继续往前走。
2.  可以理解为，如果被赶上了，最新的数据就不允许写了，hang住，先让子弹飞一会儿。

`LSN` 另一个作用是在奔溃恢复时会比较 `redo log` 与 库里数据页的 `LSN` 点位来修复数据。

#### 3.2.2、根据下面这两张图，举几种case：

![mysql核心剖析系列-crash safe.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cdcc7eb383b444318647dbe6be79c9f5~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

![mysql核心剖析系列-crash safe 恢复.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ee4845ab9cfb402d99f8f9226f51555f~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

1.  在 `step 1` 处挂了。 `redo log` 没有提交， `binlog` 也没写；事务回滚，对数据没影响
2.  在 `step 2` 处挂了。这里有2种情况：
    1.  `binlog` 写了 `binlog cache` ，还没刷到磁盘； `redo log` 还没提交。当崩溃恢复时，先判断 `redo log` 的 `pre` 标识，再根据 `xid` 去 `binlog` 里找，发现没有，回滚事务。
    2.  `binlog` 已经刷到磁盘； `redo log` 还没提交。当崩溃恢复时，先判断 `redo log` 的 `pre` 标识，再根据 `xid` 去 `binlog` 里找，发现存在记录，提交事务。 `redo log` commit
3.  在 `step 3` 处挂了。这里也有2种情况：
    1.  `binlog` 已经刷磁盘； `redo log` commit 写了 `redo log buffer` ，还没刷到磁盘；先判断 `redo log` 的 `pre` 标识，再根据 `xid` 去 `binlog` 里找，发现存在记录，提交事务。 `redo log` commit
    2.  `binlog` 已经刷磁盘； `redo log` 刷磁盘。判断 `redo log` 的 `commit` 标识；直接提交事务。

以上的case就是不管在那一层崩溃都能依靠 `redo log` 来保证主从数据一致性以及客户端感知一致性。但是：

**当数据写到 buffer pool 之后，刷盘之前挂了咋整？**

### 3.3、持久化

## 4、名词解释2.0

### 4.1、什么是 `change buffer` ？

`change buffer` 是一块缓冲区，存储的就是最新的数据变更。它主要解决的是 `随机读磁盘IO` 消耗大的问题。为什么是随机读呢？

看着文章第二张图讲。如果没有 `change buffer` ，当有一条更新语句进来对某条数据进行修改时，需要找到这条数据，优先从 `buffer pool` 中找，不存在则从磁盘获取。将数据页从磁盘读入 `buffer pool` 涉及随机 IO 访问，这是数据库中成本最高的操作之一。所以有了这么一块缓冲区之后，针对某些写入或修改操作，直接把数据缓存在 `change buffer` 中。当下次查询的时候再从磁盘读出原始数据，将原始数据和 `change buffer` 中的改动做 merge 之后返回。省去的是写操作时可能涉及到的磁盘IO操作。

`change buffer` 虽然名上是 `buffer`。但其实它是可以持久化的，它持久化的地方默认是 `ibdata1` 共享空间表中（看上文件图）。因为为了保证数据的一致性

**同时， `change buffer` 也是需要写 `redo log` 的。所以 `redo log` 里不仅有针对普通数据页的改动记录，也有 `change buffer` 的记录。**

然后说到 `change buffer` 就不得不说到 `buffer pool` 。顾名思义，缓冲池。我们在做项目工程的时候，遇到高并发的场景一般都会在db前面加一层 redis 扛一波，防止大量请求直接把 db 打挂，那么 redis 在这里充当了缓冲的作用。 `buffer pool` 也是类似，server 层的查请求过来会先打在 `buffer pool` 上，如果 `buffer pool` 不存在对应的数据才会去查磁盘，否则直接取 `buffer pool` 中的数据返回了。

![mysql核心剖析系列-缓冲.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9e08151d73934a3d92645c0c47443813~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

那么 `change buffer` 与 `buffer pool` 是什么关系呢？

如上图。**包含关系**

`change buffer` 是 `buffer pool` 里的一块区域

-   `innodb_change_buffer_max_size` 表示 `change buffer` 最大占 `buffer pool` 的百分比，默认为 25%

![image-20210919170911021](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/caa6e185e5c34a6fa3a7fa8ec2e7ee2c~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

下面看一下 执行一条语句在 `buffer` 这块的细节

![mysql核心剖析系列-缓冲流程.drawio.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8b1c4b8ef542431d987ec4119cf01da9~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.image)

我们来举几种场景：

1.  一条简单的更新语句。`where name = "花一个无所"`
    1.  判断 `buffer pool` 中是否存在这条数据
    2.  若存在则直接更新 `buffer pool`
    3.  否则 add `change buffer`
    4.  写 `redo log`
2.  一条根据唯一健更新的语句。`where uniqId = 7`
    1.  判断 `buffer pool` 中是否存在这条数据
        
    2.  若存在则直接更新 `buffer pool`
        
    3.  否则查磁盘
        
        ```
        如果存在则 load 进 buffer pool
        否则返回错误
        复制代码
        ```
        
    4.  更新 `buffer pool`
        
    5.  写 `redo log`
    
3.  一条简单的查询语句。`select * from name = "花一个无所"`
    1.  判断 `buffer pool` 中是否存在这条数据
        
    2.  如果不存在则查磁盘
        
        ```
        如果存在则 load 进 buffer pool
        复制代码
        ```
        
    3.  判断 `change buffer` 中是否有这条数据
        
        ```
        如果存在则 merge 进 buffer pool
        复制代码
        ```
        
    4.  返回 merge 之后的数据
        

## 5、为什么要有 `change buffer`？

### 5.1、`change buffer` 解决了什么问题？

将数据页从磁盘读入内存中涉及随机 IO 访问，这也是数据库里面成本最高的操作之一，而利用写缓存（ `change buffer` ）可以减少 IO 操作，从而提升数据库性能。

**那为啥唯一索引不能利用 `change buffer` 呢？**

上面流程里面。唯一索引在做 insert 或者 update 的时候，需要先判断索引记录的唯一性，所以肯定要先拿到最新的记录。即会将磁盘数据页加载到内存，然后判断。所以既然都加载到内存了，那我直接操作内存就好了，就不用搭理 `change buffer` 了，不然还得 merge 啥的多麻烦。

### 5.2、 `change buffer` 干掉可不可以？

`change buffer` 本身就是一个可选项。

-   `innodb_change_buffering` 参数用来控制对哪些操作启用 `change buffer` 功能，默认是： `all`。\*\*

**`innodb_change_buffering` 参数拢共有以下几种选择：**

-   all。默认值。开启buffer
-   none。不开启 change buffer
-   inserts。只是开启 buffer insert 操作
-   deletes。只是开启 delete-marking 操作
-   changes。开启 buffer insert 操作和 delete-marking 操作
-   purges。对只是在后台执行的物理删除操作开启 buffer 功能

不开启的影响就是回到了随机读。当 `buffer pool` 中不存在此数据时，写入操作时会先从磁盘读出数据 load 进 `buffer pool` ，再进行下面的操作。每次都是这样，对逻辑及数据准确性没有影响，只是影响性能。

### 5.3、持久化

#### 5.3.1、 `change buffer` 持久化

**`change buffer` 是用来 更新 非主键/唯一索引的二级索引B+树的。`redolog` 是保障 `crash-safe` 的。**

`change buffer` 为啥要持久化？

看看不持久化会有什么影响。

`change buffer` 插入时需要写 `redo log`。当宕机时， `change buffer` 丢失，`redo log` 记录了数据的完整修改记录，恢复时根据 `redo log` 重建 `change buffer` 。感觉不用持久化也可以啊

个人见解：

-   `change buffer` 是有容量限制的。当内存容量用完了落盘来留出空间应对新操作。
-   `redo log` 保证的是原数据的准确性， `change buffer` 保证的是索引页的准确性。落盘是为了数据的一致性

#### 5.3.2、刷脏页

什么叫刷脏页？

某条数据在内存中存在。这时有一个更新语句过来修改内存 然后返回。此时内存里的数据跟磁盘不一致， 内存中的数据就是脏数据。因为数据记录是以页为单位，就称脏页。

将 `buffer pool` 中的脏数据定期的应用到原数据页得到最新结果的过程称为刷脏页。把磁盘数据读到内存，然后 merge `change buffer` 会写磁盘。此时，磁盘和内存里的数据都是新的正确的数据。

有哪几种场景会触发刷脏页？

1.  查数据时。磁盘存在、 `change buffer` 存在 且 `buffer pool` 存在
2.  后台定期刷
3.  数据库关闭

## 6、引用

[极客时间《MySQL实战45讲》](https://link.juejin.cn/?target=https%3A%2F%2Ftime.geekbang.org%2Fcolumn%2Fintro%2F139 "https://time.geekbang.org/column/intro/139")

[数据库内核月报](https://link.juejin.cn/?target=http%3A%2F%2Fmysql.taobao.org%2Fmonthly%2F "http://mysql.taobao.org/monthly/")

[www.modb.pro/db/62466](https://link.juejin.cn/?target=https%3A%2F%2Fwww.modb.pro%2Fdb%2F62466 "https://www.modb.pro/db/62466")