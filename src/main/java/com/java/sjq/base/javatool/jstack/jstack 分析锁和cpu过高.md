[(122条消息) jstack命令解析\_fightingD&W的博客-CSDN博客](https://blog.csdn.net/weixin_44588186/article/details/124680586)

## 前言

如果有一天，你的Java程序长时间停顿，也许是它病了，需要用[jstack](https://so.csdn.net/so/search?q=jstack&spm=1001.2101.3001.7020)拍个片子分析分析，才能诊断具体什么病症，是死锁综合征，还是死循环等其他病症，本文我们一起来学习jstack命令~

-   jstack 的功能
-   jstack用法
-   线程状态等基础回顾
-   实战案例1：jstack 分析死锁
-   实战案例2：jstack 分析CPU 过高

## jstack 的功能

jstack是JVM自带的[Java堆栈](https://so.csdn.net/so/search?q=Java%E5%A0%86%E6%A0%88&spm=1001.2101.3001.7020)跟踪工具，它用于打印出给定的java进程ID、core file、远程调试服务的Java堆栈信息.

```
jstack prints Java stack traces of Java threads for a given Java process orcore file or a remote debug server. 复制代码
```

> -   jstack命令用于生成虚拟机当前时刻的线程快照。
> -   线程快照是当前虚拟机内每一条线程正在执行的方法堆栈的集合，生成线程快照的主要目的是定位线程出现长时间停顿的原因， 如线程间死锁、死循环、请求外部资源导致的长时间等待等问题。
> -   线程出现停顿的时候通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么资源。
> -   如果java程序崩溃生成core文件，jstack工具可以用来获得core文件的java stack和native stack的信息，从而可以轻松地知道java程序是如何崩溃和在程序何处发生问题。
> -   另外，jstack工具还可以附属到正在运行的java程序中，看到当时运行的java程序的java stack和native stack的信息, 如果现在运行的java程序呈现hung的状态，jstack是非常有用的。

## jstack用法

**jstack 命令格式如下**

```
jstack [ option ] pid jstack [ option ] executable core jstack [ option ] [server-id@]remote-hostname-or-IP 复制代码
```

-   executable Java executable from which the core dump was produced.(可能是产生core dump的java可执行程序)
-   core 将被打印信息的core dump文件
-   remote-hostname-or-IP 远程debug服务的主机名或ip
-   server-id 唯一id,假如一台主机上多个远程debug服务

**最常用的是**

```
 [option] <pid>  // 打印某个进程的堆栈信息复制代码
```

**option参数说明如下：**

![](https://img-blog.csdnimg.cn/img_convert/0269b6d4353b99cff9cc3bf41b78366c.webp?x-oss-process=image/format,png)

| 选项 | 作用 |
| --- | --- |
| \-F | 当正常输出的请求不被响应时，强制输出线程堆栈 |
| \-m | 如果调用到本地方法的话，可以显示C/C++的堆栈 |
| \-l | 除堆栈外，显示关于锁的附加信息，在发生死锁时可以用jstack -l pid来观察锁持有情况 |

## 线程状态等基础回顾

### 线程状态简介

jstack用于生成线程快照的，我们分析线程的情况，需要复习一下线程状态吧，拿小凳子坐好，复习一下啦~

![](https://img-blog.csdnimg.cn/img_convert/d7d57d5f7ac54c7630369f7fa95a8972.webp?x-oss-process=image/format,png)

**Java语言定义了6种线程池状态：**

-   New：创建后尚未启动的线程处于这种状态，不会出现在Dump中。
-   RUNNABLE：包括Running和Ready。线程开启start（）方法，会进入该状态，在虚拟机内执行的。
-   Waiting：无限的等待另一个线程的特定操作。
-   Timed Waiting：有时限的等待另一个线程的特定操作。
-   阻塞（Blocked）：在程序等待进入同步区域的时候，线程将进入这种状态，在等待监视器锁。
-   结束（Terminated）：已终止线程的线程状态，线程已经结束执行。

**Dump文件的线程状态一般其实就以下3种：**

-   RUNNABLE，线程处于执行中
-   BLOCKED，线程被阻塞
-   WAITING，线程正在等待

### Monitor 监视锁

因为Java程序一般都是多线程运行的，Java多线程跟监视锁环环相扣，所以我们分析线程状态时，也需要回顾一下Monitor监视锁知识。

有关于线程同步关键字Synchronized与监视锁的爱恨情仇，有兴趣的伙伴可以看一下我这篇文章 [Synchronized解析——如果你愿意一层一层剥开我的心](https://juejin.cn/post/6844903918653145102#heading-18 "Synchronized解析——如果你愿意一层一层剥开我的心")

Monitor的工作原理图如下：

![](https://img-blog.csdnimg.cn/img_convert/c7d882d1d22fd2d73d33a876e7c24225.webp?x-oss-process=image/format,png)

-   线程想要获取monitor,首先会进入Entry Set队列，它是Waiting Thread，线程状态是Waiting for monitor entry。
-   当某个线程成功获取对象的monitor后,进入Owner区域，它就是Active Thread。
-   如果线程调用了wait()方法，则会进入Wait Set队列，它会释放monitor锁，它也是Waiting Thread，线程状态in Object.wait()
-   如果其他线程调用 notify() / notifyAll() ，会唤醒Wait Set中的某个线程，该线程再次尝试获取monitor锁，成功即进入Owner区域。

### Dump 文件分析关注重点

-   runnable，线程处于执行中
-   deadlock，死锁（重点关注）
-   blocked，线程被阻塞 （重点关注）
-   Parked，停止
-   locked，对象加锁
-   waiting，线程正在等待
-   waiting to lock 等待上锁
-   Object.wait()，对象等待中
-   waiting for monitor entry 等待获取监视器（重点关注）
-   Waiting on condition，等待资源（重点关注），最常见的情况是线程在等待网络的读写

## 实战案例1：jstack 分析死锁问题

-   什么是死锁？
-   如何用jstack排查死锁？

### 什么是死锁？

![](https://img-blog.csdnimg.cn/img_convert/63085106cb2310b08a31af01bf5b1bdb.webp?x-oss-process=image/format,png)

死锁是指两个或两个以上的线程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力作用，它们都将无法进行下去。

### 如何用如何用jstack排查死锁问题

先来看一段会产生死锁的Java程序，源码如下：

```
/** class DeathLockTest {ivate static Lock lock1 = new ReentrantLock();ivate static Lock lock2 = new ReentrantLock();blic static void deathLock() {  Thread t1 = new Thread() {      @Override      public void run() {          try {              lock1.lock();              System.out.println(Thread.currentThread().getName() + " get the lock1");              Thread.sleep(1000);              lock2.lock();              System.out.println(Thread.currentThread().getName() + " get the lock2");          } catch (InterruptedException e) {              e.printStackTrace();          }      }  };  Thread t2 = new Thread() {      @Override      public void run() {          try {              lock2.lock();              System.out.println(Thread.currentThread().getName() + " get the lock2");              Thread.sleep(1000);              lock1.lock();              System.out.println(Thread.currentThread().getName() + " get the lock1");          } catch (InterruptedException e) {              e.printStackTrace();          }      }  };  //设置线程名字，方便分析堆栈信息  t1.setName("mythread-jay");  t2.setName("mythread-tianluo");  t1.start();  t2.start();}blic static void main(String[] args) {  deathLock();}}复制代码
```

运行结果：

![](https://img-blog.csdnimg.cn/img_convert/a051c6bd3d3280acbd02b13239ad5628.webp?x-oss-process=image/format,png)

显然，线程jay和线程tianluo都是只执行到一半，就陷入了阻塞等待状态~

### jstack排查Java死锁步骤

-   在终端中输入jsp查看当前运行的java程序
-   使用 jstack -l pid 查看线程堆栈信息
-   分析堆栈信息

### 在终端中输入jsp查看当前运行的java程序

![](https://img-blog.csdnimg.cn/img_convert/ba63d4aaf157615e1b329fcb67c4a003.webp?x-oss-process=image/format,png)

通过使用 jps 命令获取需要监控的进程的pid，我们找到了`23780 DeathLockTest`

### 使用 jstack -l pid 查看线程堆栈信息

![](https://img-blog.csdnimg.cn/img_convert/79b51c41c1a6a1348e13831a45958bd4.webp?x-oss-process=image/format,png)

由上图，可以清晰看到**死锁**信息：

-   mythread-tianluo 等待这个锁 “0x00000000d61ae3a0”，这个锁是由于mythread-jay线程持有。
-   mythread-jay线程等待这个锁“0x00000000d61ae3d0”,这个锁是由mythread-tianluo 线程持有。

### 还原死锁真相

![](https://img-blog.csdnimg.cn/img_convert/4779b42863a4f91d84ce36ce354b3d82.webp?x-oss-process=image/format,png)

**“mythread-tianluo"线程堆栈信息分析如下：**

-   mythread-tianluo的线程处于等待（waiting）状态，持有“0x00000000d61ae3d0”锁，等待“0x00000000d61ae3a0”的锁

**“mythread-jay"线程堆栈信息分析如下：**

-   mythread-tianluo的线程处于等待（waiting）状态，持有“0x00000000d61ae3a0”锁，等待“0x00000000d61ae3d0”的锁

![](https://img-blog.csdnimg.cn/img_convert/61db883ba2632660920e666267439dad.webp?x-oss-process=image/format,png)

## 实战案例2：jstack 分析CPU过高问题

来个导致CPU过高的demo程序，一个死循环，哈哈~

```
/** class JstackCase {rivate static ExecutorService executorService = Executors.newFixedThreadPool(5);blic static void main(String[] args) {  Task task1 = new Task();  Task task2 = new Task();  executorService.execute(task1);  executorService.execute(task2);}blic static Object lock = new Object();atic class Task implements Runnable{  public void run() {      synchronized (lock){          long sum = 0L;          while (true){              sum += 1;          }      }  }}}复制代码
```

### jstack 分析CPU过高步骤

-   1.  top
-   1.  top -Hp pid
-   1.  jstack pid
-   1.  jstack -l \[PID\] >/tmp/log.txt
-   1.  分析堆栈信息

### 1.top

在服务器上，我们可以通过top命令查看各个进程的cpu使用情况，它默认是按cpu使用率由高到低排序的

![](https://img-blog.csdnimg.cn/img_convert/bfda501eb2c45f38983df59c23bd8707.webp?x-oss-process=image/format,png)

由上图中，我们可以找出pid为21340的java进程，它占用了最高的cpu资源，凶手就是它，哈哈！

### 2\. top -Hp pid

通过top -Hp 21340可以查看该进程下，各个线程的cpu使用情况，如下：

![](https://img-blog.csdnimg.cn/img_convert/a46863917e187a18be75b0afe4d7a4cb.webp?x-oss-process=image/format,png)

可以发现pid为21350的线程，CPU资源占用最高~，嘻嘻，小本本把它记下来，接下来拿jstack给它拍片子~

### 3\. jstack pid

通过top命令定位到cpu占用率较高的线程之后，接着使用jstack pid命令来查看当前java进程的堆栈状态，`jstack 21350`后，内容如下：

![](https://img-blog.csdnimg.cn/img_convert/5ce6ec55180c2cd55cb6e847ed783339.webp?x-oss-process=image/format,png)

### 4\. jstack -l \[PID\] >/tmp/log.txt

其实，前3个步骤，堆栈信息已经出来啦。但是一般在生成环境，我们可以把这些堆栈信息打到一个文件里，再回头仔细分析哦~

### 5\. 分析堆栈信息

我们把占用cpu资源较高的线程pid（本例子是21350），将该pid转成16进制的值

![](https://img-blog.csdnimg.cn/img_convert/9fb56045b3b08390e41ef3e34647f4ee.webp?x-oss-process=image/format,png)

在thread dump中，每个线程都有一个nid，我们找到对应的nid（5366），发现一直在跑（24行）

![](https://img-blog.csdnimg.cn/img_convert/3dff86dfad7a1dcae7b2e7f8956f0224.webp?x-oss-process=image/format,png)

这个时候，可以去检查代码是否有问题啦~ 当然，也建议隔段时间再执行一次stack命令，再一份获取thread dump，毕竟两次拍片结果（jstack）对比，更准确嘛~


转自：[Java程序员必备：jstack命令解析](https://juejin.cn/post/6844904152850497543 "Java程序员必备：jstack命令解析")
