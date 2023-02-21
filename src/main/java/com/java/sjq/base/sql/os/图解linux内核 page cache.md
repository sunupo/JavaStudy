> [Linux---回写机制](https://www.cnblogs.com/r1chie/p/10818039.html)
>
> [【page cache】简介](https://blog.csdn.net/CoolBoySilverBullet/article/details/121747994)

[【图解Linux内核】Page Cache - 腾讯云开发者社区-腾讯云](https://cloud.tencent.com/developer/article/1890131)

在资深开发的日常，经常能遇见和Page Cache相关场景：

-   [服务器](https://cloud.tencent.com/product/cvm?from=10680)的load飙高
-   服务器的I/O吞吐飙高
-   业务响应时延出现大的毛刺
-   业务平均访问时延明显增加。

这些问题，很可能是由于Page Cache管理不到位引起的，因为Page Cache管理不当除了会增加系统I/O吞吐外，还会引起业务性能抖动。

这类问题出现后，开发人员往往束手无策，究其原因在于他们对Page Cache的理解仅仅停留在概念上，并不清楚Page Cache如何和应用、系统关联起来，对它引发的问题自然会束手无策了。

认识Page Cache最简单的方式，就是用数据说话，通过具体的数据你会更加深入地理解Page Cache的本质。 为什么需要Page Cache，Page Cache的产生和回收是什么样的。

最好具备一些Linux编程的基础，比如，如何打开一个文件；如何读写一个文件；如何关闭一个文件等等。

## 什么是Page Cache？

Page Cache到底是属于内核还是属于用户？

![](https://ask.qcloudimg.com/http-save/yehe-1752328/e5052400b7a4954d101c7d26abc24202.png?imageView2/2/w/1620)

红色的地方就是Page Cache，Page Cache是内核管理的内存，它属于内核。

## 怎么观察Page Cache

在Linux上直接查看Page Cache的方式：

-   /proc/meminfo
-   free
-   /proc/vmstat 命令

内容其实是一致的。拿/proc/meminfo命令举例

```
$ cat /proc/meminfo
...
Buffers:            1224 kB
Cached:           111472 kB
SwapCached:        36364 kB
Active:          6224232 kB
Inactive:         979432 kB
Active(anon):    6173036 kB
Inactive(anon):   927932 kB
Active(file):      51196 kB
Inactive(file):    51500 kB
...
Shmem:             10000 kB
...
SReclaimable:      43532 kB
...
```

可得出公式（等式两边之和都是112696 KB）：

```
Buffers + Cached + SwapCached = 
Active(file) + Inactive(file) + Shmem + SwapCached
```

等式两边的内容就是Page Cache。两边都有SwapCached，因为它也是Page Cache一部分。 因Buffers更依赖于内核实现，在不同内核版本它的含义可能不一，而等式右边和应用程序的关系更直接，分析等式右边。

Page Cache中，Active(file)+Inactive(file)是File-backed page（与文件对应的内存页）。平时用的mmap()内存映射方式和buffered I/O来消耗的内存就属于这部分，在生产环境也最容易产生问题。

SwapCached是在打开Swap分区后，把Inactive(anon)+Active(anon)这两项里的匿名页给交换到磁盘（swap out），然后再读入到内存（swap in）后分配的内存。 由于读入到内存后原来的Swap File还在，所以SwapCached也可以认为是File-backed page，即属于Page Cache。这是为了减少I/O。

![](https://ask.qcloudimg.com/http-save/yehe-1752328/f596d1f325a947302b4248b80080a2d7.png?imageView2/2/w/1620)

SwapCached只在Swap分区打开时才有，推荐生产环境关闭Swap分区，因为Swap过程产生的I/O容易引起性能抖动。

Page Cache中的Shmem指匿名共享映射这种方式分配的内存（free命令中shared这一项），比如tmpfs（临时文件系统），这部分在生产环境问题较少，不过多关注。

很多同学喜欢用free查看系统中有多少Page Cache，根据buff/cache判断存在多少Page Cache。free也是通过解析/proc/meminfo得出这些统计数据的。

free的buff/cache是什么呢？

```
$ free -k
              total        used        free      shared  buff/cache   available
Mem:        7926580     7277960      492392       10000      156228      430680
Swap:       8224764      380748     7844016
```

通过procfs源码里面的proc/sysinfo.c，可以发现buff/cache包括

```
buff/cache = Buffers + Cached + SReclaimable
```

一定要考虑到这些数据是动态变化的，而且执行命令本身也会带来内存开销，所以这个等式未必会严格相等，不过你不必怀疑它的正确性。

看到free命令中的buff/cache是由Buffers、Cached和SReclaimable这三项组成的，它强调的是内存的可回收性，即可以被回收的内存会统计在这一项。

SReclaimable是可以被回收的内核内存，包括dentry和inode等。

掌握了Page Cache具体由哪些部分构成之后，在它引发一些问题时，你就能够知道需要去观察什么。 比如应用本身消耗内存（RSS）不多的情况下，整个系统的内存使用率还是很高，那不妨去排查下是不是Shmem(共享内存)消耗了太多内存。

如果不用内核管理的Page Cache，那有两种思路来进行处理：

-   应用程序维护自己的Cache做更加细粒度的控制，比如[MySQL](https://cloud.tencent.com/product/cdb?from=10680)就是这样做的，MySQL Buffer Pool实现复杂度很高。
-   直接使用Direct I/O绕过Page Cache，不使用Cache了，省的去管它了。

## 为什么需要Page Cache？

标准I/O和内存映射会先把数据写入到Page Cache，这样做会通过减少I/O次数来提升读写效率。我们看一个具体的例子。首先，我们来生成一个1G大小的新文件，然后把Page Cache清空，确保文件内容不在内存中，以此来比较第一次读文件和第二次读文件耗时的差异。具体的流程如下。

先生成一个1G的文件：

$dd if=/dev/zero of=/home/yafang/test/dd.out bs=4096 count=$((1024\*256))

其次，清空Page Cache，需要先执行一下sync来将脏页（第二节课，我会解释一下什么是脏页）同步到磁盘再去drop cache。

```
$ sync && echo 3 > /proc/sys/vm/drop_caches
```

第一次读取文件的耗时如下：

```
$ time cat /home/yafang/test/dd.out &> /dev/null
real0m5.733s
user0m0.003s
sys0m0.213s
```

再次读取文件的耗时如下：

```
$ time cat /home/yafang/test/dd.out &> /dev/null 
real0m0.132s
user0m0.001s
sys0m0.130s
```

通过这样详细的过程你可以看到，第二次读取文件的耗时远小于第一次的耗时，这是因为第一次是从磁盘来读取的内容，磁盘I/O是比较耗时的，而第二次读取的时候由于文件内容已经在第一次读取时被读到内存了，所以是直接从内存读取的数据，内存相比磁盘速度是快很多的。这就是Page Cache存在的意义：减少I/O，提升应用的I/O速度。

所以，如果你不想为了很细致地管理内存而增加应用程序的复杂度，那你还是乖乖使用内核管理的Page Cache，它是ROI(投入产出比)相对较高的一个方案。

Page Cache的不足之处也是有的，这个不足之处主要体现在，它对应用程序太过于透明，以至于应用程序很难有好方法来控制它。

Page Cache对应用提升I/O效率而言是一个投入产出比较高的方案，所以它的存在还是有必要的。

本文分享自作者个人站点/博客：https://blog.csdn.net/qq\_33589510复制

如有侵权，请联系

cloudcommunity@tencent.com

删除。