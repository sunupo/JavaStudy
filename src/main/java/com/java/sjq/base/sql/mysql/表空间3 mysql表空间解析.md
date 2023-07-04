# [mysql表空间解析 - 掘金](https://juejin.cn/post/7048870800169893902)

   

[![](https://p3-passport.byteimg.com/img/user-avatar/6775469a3f7fba9ee76481cf9bd52e83~100x100.awebp)](https://juejin.cn/user/4388906149351639)

2022年01月03日 15:01 ·  阅读 265

## 概述

前面几节，我介绍了行记录，页，索引（B+）的结构，索引的叶子和非叶子节点都是由页构成的，页的大小有16k,可以存储满行记录。那么问题来了，一张表有多个索引，那这些索引又是怎么组织起来的呢，答案就是我们今天要讲的表空间，表空间真是描述了一张表怎么合理存储多个索引的，也就是表数据（索引即数据）。

## 表空间结构解析

### 1.表空间整体结构

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c3912b74d42c40a7a9616cea2f3e0f45~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)

```
1.表空间其实是一个文件，存在mysql安装时候的数据目录下面

2.每创建一张表，就会生成一个.idb文件，这个文件就是表空间文件

3.一个表文件中分为很多extend(区)，每255个区是一个组

```

### 2.区结构解析

> [MySQL :: MySQL 5.7 Reference Manual :: MySQL Glossary --- MySQL ：： MySQL 5.7 参考手册 ：： MySQL 词汇表](https://dev.mysql.com/doc/refman/5.7/en/glossary.html#glos_extent)

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3e7bb7041d9248269f4a7db3e6e8f5b1~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)

#### 组

一个组 256 区

1. 一个区一般是由 **64** 页组成，**虽然索引的节点都是页组成的**，<u>如果按照16k为单位来分配，那么在磁盘上一个表的数据都是分散的，会导致磁盘旋转频繁，也就是随机IO,所以**按照区来分配空间**，一张表内容是连续存储的，也就是顺序IO,比较快</u>（join表是不是就慢了，不在一个表空间）
2. 表空间**第一个组的第一个分区第一页**是比较特殊的，也就是FSP_HDR类型的页，他存储一个表空间级别的一些信息，还有当前组的所有区（**256**）的属性信息，其他组的第一个分区的第一页的页类型是XDES,他只存储当前组的所有区的属性信息，也就是用来管理当前组的一些统计信息的
3. 表空间**第一个组的第一个分区第二页**是比较特殊的，也就是INODE类型的页，这个页只存储了表空间级别的信息。其它分组的页都没这个页。这个页存什么信息呢，存的是**<u>段</u>**的信息，什么是段？其实是索引有关的信息。

### 3.段结构解析

[MySQL :: MySQL 5.7 Reference Manual :: MySQL Glossary --- segment](https://dev.mysql.com/doc/refman/5.7/en/glossary.html#glos_segment)

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7a79387ad7314ab18d07399c69a534ab~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)1.段

1. **段**是存储在INODE页里面的，段是表空间级别的信息，那么为什么会有段呢？
   1. 表存储数据，按照mysql设计，其实就是存储索引，比如聚簇索引，叶子节点就是表数据了
   2. 索引是由页构成的，如果分配是按照页单元分配，那么B+树上相邻的页节点可能相离的物理距离非常远那么这个时候加载相邻的两个页就要转动磁头了，那么就是所谓的随机IO,访问非常慢，索引按照页来分配空间是不合理的。
   3. 一个分区大小是64*16k,如果按照区来分配空间，那么如果数据很小情况下，直接就以1M分配了又太浪费空间了，所以采用了**混合区和页的分配方式，也就是段。**

2. 看到段结构，它其实数据部分是这些字段。
   1. Fragment Array Entry 0-31是指向页的，也就是数据量小于32页，使用页来存储的。
   2. FREE链表,NOT_FULL链表,FULL链表是指向区的，直接分配区的空间存储表索引数据，当Fragment Array Entry已经使用完后。

### 4.索引和段关系图解析

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/48d0bd3ee5ba46e38565cb4c247c0b2e~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)

```
1.一个索引会分配两个段，叶子节点和非叶子节点是分开存储的。

2.使用两个段来存储，是想要压缩叶子节点和非叶子节点，它们分别相邻页的距离
  从而达到顺序IO的目的。这里有个原则是，叶子节点只会访问相邻的叶子节点，不会
  访问到非叶子节点，所以分开存储达到更好的顺序IO效果
复制代码
```

5.段和表空间关系图解析

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c5afdde7f69c43eb97eea760e9d316bd~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)

```
1.段是一个索引的开始的地方，每当一个插入一条数据，先找到段，然后查找段的存储情况
  如果数据量还未满32页，那么继续使用碎片区插入数据，否则直接申请单个区插入数据
  
2.段完全展示了数据库分配空间的一些细节，可以看到在设计上，为了节省空间使用碎片区，
  为了顺序IO,大量数据之后采用区作为分配空间策略，叶子和非叶子节点分段储存
复制代码
```

6.整体结构图

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d1db03809d3a481dbd43cca3b6dd1f26~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)



# [MySQL-段、区、组](https://blog.csdn.net/solo_jm/article/details/118962314)

（从 **页** 引入区，再到 段）

## 区

如果我们有大量的记录，那么表空间中页的数量就会非常的多，为了更好的管理这些页，设计者又提出了区的概念。对于16KB的页来说，连续的64个页（连续的）就是一个区，也就是说一个区的大小为1MB。（256个区被划分成一个组）

## 为什么需要区

通过页其实已经形成了完整的功能，我们查询数据时这样沿着[双向链表](https://so.csdn.net/so/search?q=%E5%8F%8C%E5%90%91%E9%93%BE%E8%A1%A8&spm=1001.2101.3001.7020)就可以查到数据，但是页与页之间在物理位置上可能不是连续的，如果相隔太远，那么我们从一个页移动到另一个页的时候，磁盘就要重新定义磁头的位置，产生随机IO，影响性能，所以我们才要引入区的概念，一个区就是物理位置连续的64个页。区是属于某一个段的（或者是混合）。

## 什么是段

正常情况下，我们检索都是在叶子节点的双向链表进行的，也就是说我们会把区进行区分，如果不区分把叶子节点和非叶子结点混在一起，那么效果就会打大折扣，所以对于一个索引B+树来说，我们区别对待叶子节点的区和非叶子节点的区，并**把存放叶子节点的区的集合称为一个段**，**把存放非叶子节点区的集合也称为一个段**，<u>**所以一个索引会生成两个段：叶子节点段和非叶子节点段。**</u>

## 碎片区

默认情况下，假如我们**新建一个索引就会生成两个段（叶子节点段和非叶子节点段），而一个段中至少包含一个区，也就是需要==2MB==的空间**，假如我们这个表压根没有多少数据，那么一次就要申请2MB的空间明显是浪费的。为了解决这个问题，设计者提出了碎片区的概念，碎片区中的页可能属于不同的段，也可以用于不同的目的，至于如何控制应不应该给一个段申请专属的区，会进行以下控制：

1.  刚开始向表插入数据，都是从某一个碎片区以页为单位来分配存储空间。
2.  当一个段占用的空间达到了32个碎片区的页之后，就会开始给这个段申请专属的区。

## 区分类

我们现在知道表空间是由若干个区组成的，这些区可以分成以下的4中类型：

1.  FREE：没有用到这个区的任何一个页面。
2.  FREE\_FRAG：有剩余空闲页的碎片区。
3.  FULL\_FRAG：没有剩余空闲页的碎片区。
4.  FSEG：专属某一个段的区。

### XDES Entry

为了管理各种各样的区，设计者提成了一个XDES Entry结构。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/img_convert/9d61f102865ef37678fd8cf5540d3de0.png)

1.  Segment ID：每一个段都有自己的编号，如果一个XDES Entry属于一个段，就是所在段的编号，如果是碎片区，则这个属性没有意义。
2.  State：四种类型的标识。
3.  Page State Bitmap：16字节，128位，每两位对应一个区中的页。第一位标识是否空闲，第二位没有用到。
4.  List Node：若干个XDES Entry形成链表。

### XDES Entry链表

当我们需要某一个类型的XDES Entry时候，如何快速的拿到，这就跟XDES Entry形成的链表有关了，对于属于表的三种类型 FREE，FREE\_FRAG，FULL\_FRAG，会生成三个链表：

1.  当我们插入一条新记录时，如果FREE\_FRAG链表不为空，在FREE\_FRAG链表中拿到一个XDES Entry并取得一个页添加数据。如果FREE\_FRAG链表为空，在FREE链表拿到一个XDES Entry并取得一个页添加数据，之后把XDES Entry移动到FREE\_FRAG链表里。
2.  如果使用FREE\_FRAG链表中的XDES Entry是发现满了，就把XDES Entry移动到FULL\_FRAG链表里。

### FSEG区

我们前面说了FREE，FREE\_FRAG，FULL\_FRAG三种类型的链表，这三种类型是针对碎片区的，而对于段专属的区类型FSEG，也会形成三种类型的链表，这三个链表属于段，存在段结构中。

1.  FREE：属于同一个段的空闲FSEG区会形成这个链表。
2.  NOT\_FULL：属于同一个段的还有空闲页的FSEG区会形成这个链表。
3.  FULL：属于同一个段的没有空闲页的FSEG区会形成这个链表。

### 段结构

段不是一个物理上连续的空间，而是一个逻辑上的概念，一个段包含若干零散的页面还有自己专属的FSEG区，这些FSEG区形成FREE, NOT\_FULL, FULL三种链表保存在段上，像XDES Entry一样，设计者也给段定义了一个结构INODE Entry。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/img_convert/4895900961f16ce4dacaa022bcff5871.png)

1.  Segment ID：该段（INODE Entry结构）的编号ID。
2.  NOT\_FULL\_N\_USED：NOT\_FULL链表中使用了多少个页面。
3.  List Base Node：是INODE Entry给FSEG链表定义的一个结构，通过这个结构可以直接找到链头，链尾。
4.  Magic Number：表示这个INODE Entry是否已经被初始化。
5.  Fragment Array Entry：前面说过只有使用了32个碎片区的页之后，才会开始申请专属的FSEG区，而32个碎片页就是靠这个属性来定位的。  
    _**段是一些零散页和完整区的结合。**_

## 区存放在哪

XDES Entry保存了有关区的消息，大小为40字节，而我们说一个区大小是1MB，由64个页组成，16KB \* 64 = 1024KB 刚好1MB，也就是没有额外的40字节来存储XDES Entry了，那这些XDES Entry到底是存放在哪的？  
前面我们有说过组的概念，组由256个区组成。表空间由组组成，组中包含了XDES Entry等信息。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/img_convert/3fa4928d75c99010c28768dc84ef7ea5.png)  
以上我们可以理解为一个表空间的组成，有组1，组2，我们之前所说的各种信息，都会保存在每一组的第一个区中的前面几个页里面。而作为表空间的第一个组，则要另外保存与表相关的信息。

-   表空间第一个组第一个区：第一页FSP\_HDR页，第二页IBUF\_BITMAP页，第三页INODE页
-   其他组第一个区：第一页XDES页，第二页IBUF\_BITMAP页

## FSP\_HDR

![在这里插入图片描述](https://img-blog.csdnimg.cn/img_convert/b2a81ff984c3f1fa38bcb497ed604174.png)

-   File Space Header：存储表相关的信息，比如我们之前说的属于表的三个链表（FREE，FREE\_FRAG，FULL\_FRAG）就会存储在这个区域。
-   XDES Entry 0 - XDES Entry 255：该组下的256个XDES Entry结构。

## XDES

与FSP\_HDR页一样，只是没有File Space Header。  
![在这里插入图片描述](https://img-blog.csdnimg.cn/img_convert/7c8b1e7d6aa34640944b866a13665a80.png)

## IBUF\_BITMAP

当发生记录的增删改时，聚餐索引会发生相应的变化，如果也二级索引的B+树也要发生相应的调整，而这些对应的页往往都不在内存中，如果要修改必须发生磁盘IO，把对应的二级索引页也给读到内存中进行修改，这样就增加了IO的次数，影响性能。  
IBUF\_BITMAP页的作用就是缓存这些操作，等到下一次MySQL刚好把这些要修改的页读取到内存的时候，顺便修改。

## INODE

我们之前分析过，一个索引有两个段，一个段会对应INODE Entry结构，这个页就是用来保存这个表空间所有的INODE Entry结构的。

