[innodb insert buffer 插入缓冲区的理解 - zuoxingyu - 博客园](https://www.cnblogs.com/zuoxingyu/p/3761461.html#:~:text=%E6%8F%92%E5%85%A5%E7%BC%93%E5%86%B2%2C%E5%B9%B6%E4%B8%8D%E6%98%AF%E7%BC%93%E5%AD%98%E7%9A%84%E4%B8%80%E9%83%A8%E5%88%86%2C%E8%80%8C%E6%98%AF%E7%89%A9%E7%90%86%E9%A1%B5%2C%E5%AF%B9%E4%BA%8E%E9%9D%9E%E8%81%9A%E9%9B%86%E7%B4%A2%E5%BC%95%E7%9A%84%E6%8F%92%E5%85%A5%E6%88%96%E6%9B%B4%E6%96%B0%E6%93%8D%E4%BD%9C%2C%E4%B8%8D%E6%98%AF%E6%AF%8F%E4%B8%80%E6%AC%A1%E7%9B%B4%E6%8E%A5%E6%8F%92%E5%85%A5%E7%B4%A2%E5%BC%95%E9%A1%B5.%E8%80%8C%E6%98%AF%E5%85%88%E5%88%A4%E6%96%AD%E6%8F%92%E5%85%A5%E7%9A%84%E9%9D%9E%E8%81%9A%E9%9B%86%E7%B4%A2%E5%BC%95%E9%A1%B5%E6%98%AF%E5%90%A6%E5%9C%A8%E7%BC%93%E5%86%B2%E6%B1%A0%E4%B8%AD.%E5%A6%82%E6%9E%9C%E5%9C%A8%2C%E5%88%99%E7%9B%B4%E6%8E%A5%E6%8F%92%E5%85%A5%2C%E5%A6%82%E6%9E%9C%E4%B8%8D%E5%86%8D%2C%E5%88%99%E5%85%88%E6%94%BE%E5%85%A5%E4%B8%80%E4%B8%AA%E6%8F%92%E5%85%A5%E7%BC%93%E5%86%B2%E5%8C%BA%E4%B8%AD.%E7%84%B6%E5%90%8E%E5%86%8D%E4%BB%A5%E4%B8%80%E5%AE%9A%E7%9A%84%E9%A2%91%E7%8E%87%E6%89%A7%E8%A1%8C%E6%8F%92%E5%85%A5%E7%BC%93%E5%86%B2%E5%92%8C%E9%9D%9E%E8%81%9A%E9%9B%86%E7%B4%A2%E5%BC%95%E9%A1%B5%E5%AD%90%E8%8A%82%E7%82%B9%E7%9A%84%E5%90%88%E5%B9%B6%E6%93%8D%E4%BD%9C.%20%E5%A6%82%E6%9E%9Cmerges%2Fmerged%E7%9A%84%E5%80%BC%E7%AD%89%E4%BA%8E3%2F1%2C%E5%88%99%E4%BB%A3%E8%A1%A8%E6%8F%92%E5%85%A5%E7%BC%93%E5%86%B2%E5%AF%B9%E4%BA%8E%E9%9D%9E%E8%81%9A%E9%9B%86%E7%B4%A2%E5%BC%95%E9%A1%B5%E7%9A%84IO%E8%AF%B7%E6%B1%82%E5%A4%A7%E7%BA%A6%E9%99%8D%E4%BD%8E%E4%BA%863%E5%80%8D.,%E5%8F%AF%E4%BB%A5%E8%BF%99%E6%A0%B7%E7%90%86%E8%A7%A3%EF%BC%8C%E5%9C%A8%E5%B9%B3%E6%97%B6%E6%AD%A3%E5%B8%B8%E4%B8%9A%E5%8A%A1%E4%B8%8B%EF%BC%8C%E9%9C%80%E8%A6%81%E5%90%88%E5%B9%B6%E7%9A%84%E4%BA%8C%E7%BA%A7%E7%B4%A2%E5%BC%95%E5%9F%BA%E6%9C%AC%E6%B2%A1%E6%9C%89%EF%BC%8C%E5%9C%A8%E5%81%9A%E6%89%B9%E9%87%8F%E5%A4%A7%E5%88%A0%E9%99%A4%E7%9A%84%E6%97%B6%E5%80%99%EF%BC%8C%E4%BA%A7%E7%94%9F%E4%BA%86%E5%BE%88%E5%A4%9A%E9%9C%80%E8%A6%81%E5%90%88%E5%B9%B6%E7%9A%84%E4%BA%8C%E7%BA%A7%E7%B4%A2%E5%BC%95%E6%94%B9%E5%8F%98%E3%80%82.%20)

今天在做一个大业务的数据删除时，看到下面的性能曲线图

![](https://images0.cnblogs.com/i/451151/201405/301559395095196.jpg)

在删除动作开始之后，insert buffer 大小增加到140。对于这些状态参数的说明

# InnoDB Insert Buffer

插入缓冲,并不是缓存的一部分,而是物理页,对于非聚集索引的插入或更新操作,不是每一次直接插入索引页.而是先判断插入的非聚集索引页是否在缓冲池中.如果在,则直接插入,如果不再,则先放入一个插入缓冲区中.然后再以一定的频率执行插入缓冲和非聚集索引页子节点的合并操作.  
使用条件:非聚集索引,非唯一

1.  Ibuf Inserts  
    插入的记录数
2.  Ibuf Merged  
    合并的页的数量
3.  Ibuf Merges  
    合并的次数

如果merges/merged的值等于3/1,则代表插入缓冲对于非聚集索引页的IO请求大约降低了3倍

```
InnoDB Insert Buffer Usage
Ibuf Cell Count
分段大小
Ibuf Used Cells
插入缓冲区的大小
Ibuf Free Cells
"自由列表"的长度
```

![复制代码](https://common.cnblogs.com/images/copycode.gif)

可以这样理解，在平时正常业务下，需要合并的二级索引基本没有，在做批量大删除的时候，产生了很多需要合并的二级索引改变。

看看合并操作节省了多少IO请求,(1034310+3)/113909=9.08,

![复制代码](https://common.cnblogs.com/images/copycode.gif)

```
-------------------------------------
INSERT BUFFER AND ADAPTIVE HASH INDEX
-------------------------------------
Ibuf: size 1, free list len 134, seg size 136, 113909 merges
merged operations:
 insert 3, delete mark 2319764, delete 1034310
discarded operations:
 insert 0, delete mark 0, delete 0
Hash table size 288996893, node heap has 304687 buffer(s)
1923.58 hash searches/s, 1806.60 non-hash searches/s
```

![复制代码](https://common.cnblogs.com/images/copycode.gif)

摘录一段朋友博客上对于insert buffer的说明

![复制代码](https://common.cnblogs.com/images/copycode.gif)

```
一，插入缓冲（Insert Buffer/Change Buffer）：提升插入性能

      只对于非聚集索引（非唯一）的插入和更新有效，对于每一次的插入不是写到索引页中，而是先判断插入的非聚集索引页是否在缓冲池中，如果在则直接插入；若不在，则先放到Insert Buffer 中，再按照一定的频率进行合并操作。这样通常能将多个插入合并到一个操作中，提升插入性能。使用插入缓冲的条件：

* 非聚集索引

* 非唯一

插入缓冲最大使用空间为1/2的缓冲池大小，不能调整大小，在plugin innodb中，升级成了Change Buffer。不仅对insert，对update、delete都有效。其参数是：

innodb_change_buffering，设置的值有：inserts、deletes、purges、changes（inserts和deletes）、all（默认）、none。

可以通过参数控制其使用的大小：

innodb_change_buffer_max_size，默认是25，即缓冲池的1/4。最大可设置为50。在5.6中被引入。

上面提过在一定频率下进行合并，那所谓的频率是什么条件？

1）辅助索引页被读取到缓冲池中。正常的select先检查Insert Buffer是否有该非聚集索引页存在，若有则合并插入。

2）辅助索引页没有可用空间。空间小于1/32页的大小，则会强制合并操作。

3）Master Thread 每秒和每10秒的合并操作。
```

![复制代码](https://common.cnblogs.com/images/copycode.gif)

innodb buffer pool 包含的数据页类型有：索引页，数据页，undo页，插入缓冲（insert buffer),自适应哈希索引，innodb存储是锁信息，数据字典信息等，结构图如下

![](https://images0.cnblogs.com/i/451151/201405/301617560411166.jpg)

# 有几个问题需要回答

1：为什么会有insert buffer,insert buffer能帮我们解决什么问题？

2：insert buffer有什么限制，为什么会有这些限制？

## 先说第一个问题。

举个现实中的例子来做说明，我们去图书馆还书，对应图书馆来说，他是做了insert(增加)操作，管理员在1小时内接受了100本书，这时候他有2种做法把还回来的书归位到书架上

1）每还回来一本书，根据这本书的编码（书柜区-排-号）把书送回架上

2）暂时不做归位操作，先放到柜面上，等不忙的时候，再把这些书按照书柜区-排-号先排好，然后一次性归位

用方法1，管理员需要进出（IO）藏书区100次，不停的登高爬低完成图书归位操作，累死累活，效率很差。

用方法2，管理员只需要进出（IO）藏书区1次，对同一个位置的书，不管多少，都只要爬一次楼梯，大大减轻了管理员的工作量。

所以图书馆都是按照方法2来做还书动作的。但是你要说，我的图书馆就20本书，1个0.5米的架子，方法2和1管理起来都很方便，这种情况不在我们讨论的范围。当数据量非常小的时候，就不存在效率问题了。

关系数据库在处理插入操作的时候，处理的方法和上面类似，每一次插入都相当于还一本书，它也需要一个柜台来保存插入的数据，然后分类归档，在不忙的时候做批量的归位。这个柜台就是insert buffer.

我想这就是为什么会有insert buffer，更多的是处于性能优化的考虑。

再说第二个问题，有什么限制：“只对于非聚集索引（非唯一）的插入和更新有效”

## 为什么对于非聚集索引（非唯一）的插入和更新有效?

还是用还书的例子来说，还一本书A到图书馆，管理员要判断一下这本书是不是唯一的，他在柜台上是看不到的，必须爬到指定位置去确认，这个过程其实已经产生了一次IO操作，相当于没有节省任何操作。

所以这个buffer只能处理非唯一的插入，不要求判断是否唯一。聚集索引就不用说了，它肯定是唯一的，mysql现在还只能通过主键聚集。

在MYSQL里面，insert buffer的大小在代码里面设定的最大可以到整个innodb buffer pool size的50%。这其实是不科学的，能够想象一下一个100平米的图书馆，有50平米是做退书的柜台是什么样子的吗？

前面说到管理员图书归位的时候，他会选择在“不忙的时候”再去做，优先处理前台退书操作，这个在MYSQL里面是这样体现的：

1）每1秒，如果IO次数小于5，合并插入缓冲。

2）每10秒，IO次数小于200，合并最多5个插入缓冲。