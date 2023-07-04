[(122条消息) HashMap的扩容——resize()方法\_hashmap resize\_Morning sunshine的博客-CSDN博客](https://blog.csdn.net/qq_44750696/article/details/125264681)

- 如果初始化hashmap的时候用户传入了容量参数：

因为构造方法中阈值threshold初始化为 2的幂，resize()中判断oldStr=threshold>0, 所以 新的容量newCap = oldThr 就是2 的幂。新的阈值newThr = 新的容量newCap*装载因子。新的容量newCap用来初始化Node数组。

- 如果初始化hashmap什么参数都没：

构造方法中不会设置threshold。
resize() 方法中判断为oldThr=threshold=0，就会运行到 newCap = DEFAULT_INITIAL_CAPACITY; newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);


大于
```java
 if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
        treeifyBin(tab, hash);
        break;
 // 满足这个条件的时候，已经9 个节点了。所以大于8

```
当链表的值超过8则会转红黑树(1.8新增)

当链表的值小于6则会从红黑树转回链表

