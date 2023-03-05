**一. 对象的内存布局**

HotSpot 虚拟机中。对象在内存中存储的布局能够分为三块区域：对象头（[Header](https://so.csdn.net/so/search?q=Header&spm=1001.2101.3001.7020)）、实例数据（Instance Data）和对齐填充（Padding）。
![img_3.png](img_3.png)
**二. 对象头**

JVM 对象头一般占用**两个机器码**，在 32-bit JVM 上占用 8字节， 在 64-bit JVM 上占用 128bit 即 8+8=16 字节（开启指针压缩后占用 4+8=12 字节）

64位机器上。数组对象的对象头占用 24 字节，启用压缩之后占用 16 字节。之所以比普通对象占用内存多是由于须要额外的空间存储数组的长度。

更详细的对象头介绍请參考：[http://blog.csdn.net/wenniuwuren/article/details/50939410](http://blog.csdn.net/wenniuwuren/article/details/50939410)

**三. 实例数据**

原生类型(primitive type)的内存占用例如以下：

<table border="0"><tbody><tr><td>Primitive Type</td><td>Memory Required(bytes)</td></tr><tr><td>boolean&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>1</td></tr><tr><td>byte&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>1</td></tr><tr><td>short&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>2</td></tr><tr><td>char&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>2</td></tr><tr><td>int&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>4</td></tr><tr><td>float&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>4</td></tr><tr><td>long&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>8</td></tr><tr><td>double&nbsp;&nbsp;&nbsp;&nbsp;</td><td>8</td></tr></tbody></table>

引用类型（reference type： Integer）在 32 位系统上每一个占用 4字节(即32bit， 才干管理 2^32=4G 的内存), 在 64 位系统上每一个占用 8字节（开启压缩为 4 bytes）。

**四. 对齐填充**

HotSpot 的对齐方式为 8 字节对齐。不足的须要 Padding 填充对齐， 公式：（对象头 + 实例数据 + padding）% 8 == 0 （0<= padding <8）