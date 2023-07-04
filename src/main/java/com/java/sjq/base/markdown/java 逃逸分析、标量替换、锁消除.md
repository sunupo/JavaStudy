[(140条消息) Java对象逃逸及逃逸分析\_java逃逸分析\_所学即分享的博客-CSDN博客](https://blog.csdn.net/m0_72106802/article/details/126879788)

___

## 前言

随着 JIT 编译器的发展与逃逸分析技术逐渐成熟，栈上分配、标量替换优化技术将会导致一些微妙的变化，所有的对象都分配到堆上也渐渐变得不那么“绝对”了。

在Java虚拟机中，对象是在Java堆中分配内存的，这是一个普遍的常识。但是，有一种特殊情况，那就是如果经过逃逸分析（Escape Analysis）后发现，一个对象并没有逃逸出方法的话，那么就可能被优化成栈上分配。这样就无需在堆上分配内存，也无须进行垃圾回收了。这也是最常见的堆外存储技术。

逃逸分析技术到现在还不是很成熟，虽然经过逃逸分析可以做标量替换、栈上分配、锁消除。但是逃逸分析自身也是需要进行一系列复杂的分析的，这其实也是一个相对耗时的过程。

## 一、对象逃逸是什么？

### 1.1 概念

对象逃逸是指当我们在某个方法里创建了一个对象，这个对象除了被这个方法引用，还在方法体之外被其它的变量引用。这样的后果是当这个方法执行完毕后，GC无法回收这个对象，就被称为对象逃逸了。逃逸的对象的内存在堆中，未逃逸的对象内存分配在栈中。

### 1.2 代码分析

```
public stringBuffer append(String apple,String pear){   StringBuffer buffer=new StringBuffer();   buffer.append(apple);   buffer.append(pear);return buffer;}
```

这种写法直接返回的是对象，用处就是被别的变量所引用，会造成对象逃逸，从而增加了GC的压力，引发STW(stop the world)现象，不推荐这样写。我们可以做修改如下代码：

```
public string append(String apple,String pear){   StringBuffer buffer=new StringBuffer();   buffer.append(apple);   buffer.append(pear);return buffer.toString();}
```

这种写法就避免了对象逃逸，从而减小了GC的压力。下面这种写法就是在方法内创建了一个对象，没有被外界方法所引用，称为未逃逸对象。

```
public void create(int x,int y) {     Point p1= new Point(x,y);     p1=null; } 
```

## 二、逃逸分析(一种分析算法)

### 1.什么是逃逸分析

逃逸分析一种数据分析算法，基于此算法可以有效减少 Java 对象在堆内存中的分配。Hotspot 虚拟机的编译器能够分析出一个新对象的引用范围，然后决定是否要将这个对象分配到堆上。例如：

-   当一个对象在方法中被定义后，对象只在方法内部使用，则认为没有发生逃逸
-   当一个对象在方法中被定义后，它被外部方法所引用，则认为发生逃逸

### 2.代码优化实践

使用逃逸分析，编译器可以对代码做如下优化：

-   栈上分配：将堆分配转化为栈分配。如果一个对象在方法内创建，要使指向该对象的引用不会发生逃逸，对象可能是栈上分配的候选。
-   同步省略：又称之为同步锁消除，如果一个对象被发现只有一个线程被访问到，那么对于这个对象的操作可以不考虑同步。
-   分离对象或标量替换：有的对象可能不需要作为一个连续的内存结构存在也可以被访问到，那么对象的部分（或全部）可以不存储在内存，而是存储在 CPU 寄存器中。

**2.1栈上分配**

```
public class ObjectStackAllocationTests {public static void main(String[] args) throws InterruptedException {long start = System.currentTimeMillis();for (int i = 0; i < 10000000; i++) {            alloc();        }long end = System.currentTimeMillis();        System.out.println("花费的时间为： " + (end - start) + " ms");        TimeUnit.MINUTES.sleep(5);    }private static void alloc() {byte[] data = new byte[10];    }}
```

这里我们在运行时配置一下属性，设置了内存大小、逃逸分析和记录GC：

第一步，在main方法运行绿色小三角点击右键，选择最后一个按钮。

![](https://img-blog.csdnimg.cn/1024eb348fba46e091395284d6592cb2.png)

第二步，添加配置属性 -Xmx128m -Xms128m -XX:+DoEscapeAnalysis -XX:+PrintGC 

![](https://img-blog.csdnimg.cn/da2b047acd86497f9c4b1e3f28e518b6.png)

我们来看一下运行效果：

![](https://img-blog.csdnimg.cn/72f771ca567a480a9e6dc8c9af32a874.png)

我们会发现花费时间为9ms且没有调用GC，如果把配置属性的逃逸分析去掉再来看运行结果：

![](https://img-blog.csdnimg.cn/3e16992295114c81a462ee30f1feb28f.png)

这时会发现花费的时间更多了，且调用了GC。

### 2.2 同步锁消除

我们知道线程同步是靠牺牲性能来保证数据的正确性，这个过程的代价会非常高。程序的并发行和性能都会降低。JVM 的 JIT 编译器可以借助逃逸分析来判断同步块所使用的锁对象是否只能够被一个线程应用？假如是，那么 JIT 编译器在编译这个同步块的时候就会取消对这部分代码上加的锁。这个取消同步的过程就叫同步省略，也叫锁消除。例如：

```
public class SynchronizedLockTest {     public void lock() {    Object obj= new Object();          synchronized(obj) {                System.out.println(obj);    }   } } 
```

### 2.3 标量替换分析

所谓的标量（scalar）一般指的是一个无法再分解成更小数据的数据。例如，Java中的原始数据类型就是标量。相对的，那些还可以分解的数据叫做聚合量（Aggregate），Java 中的对象就是聚合量，因为他可以分解成其他聚合量和标量。在JIT阶段，如果经过逃逸分析，发现一个对象不会被外界访问的话，那么经过JIT优化，就会把这个对象分解成若干个变量来代替。这个过程就是标量替换。例如：

我们在2.1栈上分配的基础上更改一下配置：-XX:+EliminateAllocations 

```
public class ObjectStackAllocationTests {public static void main(String args[]) {         long start = System.currentTimeMillis();         for (int i = 0; i < 10000000; i++) {                         alloc();    }long end = System.currentTimeMillis();        System.out.println("花费的时间为： " + (end - start) + " ms");    }private static void alloc() {         Point point = new Point(1,2);    }static class Point {         private int x;         private int y;public Point(int x,int y){             this.x=x;             this.y=y;        }    }
```

![](https://img-blog.csdnimg.cn/7e81d0bf25374b4dafae52ae8833a9cf.png)

![](https://img-blog.csdnimg.cn/bf13142010394c59abbac8d8226be052.png)

alloc方法内部的Point对象是一个聚合量，这个聚合量经过逃逸分析后，发现他并没有逃逸，就被替换成两个标量了。那么标量替换有什么好处呢？就是可以大大减少堆内存的占用。因为一旦不需要创建对象了，那么就不再需要分配堆内存了。标量替换为栈上分配提供了很好的基础。

## 总结

本次介绍了对象逃逸的概念以及对象逃逸案例分析和代码优化，小伙伴们学会了吗？为了提高我们代码的质量，一定要规范写代码哦！