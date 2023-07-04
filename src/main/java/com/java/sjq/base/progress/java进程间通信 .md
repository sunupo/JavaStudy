[java进程间通信 - 懒惰的星期六 - 博客园](https://www.cnblogs.com/sunupo/p/13568239.html)
# 1.java进程间通信

1\. Runtime+.Process可以启动新的进程

```
Runtime rt = Runtime.getRuntime();
Process exec = rt.exec("shutdown -a");
exec.waitFor();  //当前进程阻塞，直到调用的进程运行结束。        System.out.println("exec.exitValue()="+exec.exitValue());  //正常结束时,子进程的返回值为0
```

2.ProcessBuilder+Process

[![复制代码](https://www.cnblogs.com//common.cnblogs.com/images/copycode.gif)](javascript:void(0); "复制代码")

```
ProcessBuilder pb = new ProcessBuilder("javac", "-d",".","src/testExcel/D.java");
try {
    Process p = pb.start();
    p.waitFor();
    System.out.println(p.exitValue());
} catch (Exception e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
}
```

[![复制代码](https://www.cnblogs.com//common.cnblogs.com/images/copycode.gif)](javascript:void(0); "复制代码")

3.父进程获取子进程的返回值

```
exec2.waitFor();直到调用的进程运行结束，否则可能获取到的输出不完整。
```

[![复制代码](https://www.cnblogs.com//common.cnblogs.com/images/copycode.gif)](javascript:void(0); "复制代码")

```
Runtime rt = Runtime.getRuntime();
Process exec2 = rt.exec("java testExcel/D");  // 运行D.class对应的字节码文件
exec2.waitFor();
System.out.println("exec2.exitValue()="+exec2.exitValue());
OutputStream outputStream = exec2.getOutputStream();
InputStream inputStream = exec2.getInputStream();
BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream));
while(reader.ready())
    System.out.println("reader.readLine()"+reader.readLine());
```

[(122条消息) Java 进程间通信总结\_java进程间通信\_一笑悬命的博客-CSDN博客](https://blog.csdn.net/manchesterutd/article/details/120263259)

Java 进程间通信总结

总结Java常见的进程间[通信方式](https://so.csdn.net/so/search?q=%E9%80%9A%E4%BF%A1%E6%96%B9%E5%BC%8F&spm=1001.2101.3001.7020)，方便面试总结。内容均来源于网络，感谢大家的分享

目录

### 文章目录

-   -   [链接](https://blog.csdn.net/manchesterutd/article/details/120263259#_9)
-   [管道(PIPE)](https://blog.csdn.net/manchesterutd/article/details/120263259#PIPE_15)
-   [Semaphore(信号量)](https://blog.csdn.net/manchesterutd/article/details/120263259#Semaphore_37)
-   [MessageQueue](https://blog.csdn.net/manchesterutd/article/details/120263259#MessageQueue_50)
-   [共享内存(SharedMemory)](https://blog.csdn.net/manchesterutd/article/details/120263259#SharedMemory_67)
-   [socket](https://blog.csdn.net/manchesterutd/article/details/120263259#socket_104)

## 链接

https://blog.csdn.net/u011490072/article/details/89508714 （Java进程之间通信方式&线程之间通信的方式）  
https://www.jianshu.com/p/e1416f026c3d （Java IO 之 管道流 原理分析）  
https://blog.csdn.net/ds19980228/article/details/89094882 （Java中的[信号量](https://so.csdn.net/so/search?q=%E4%BF%A1%E5%8F%B7%E9%87%8F&spm=1001.2101.3001.7020)（Semaphore））  
https://blog.csdn.net/JAVABBQWE/article/details/119009229  
https://me.tongleer.com/post-46.html

## 管道(PIPE)

转载https://www.jianshu.com/p/e1416f026c3d

管道流是用来在多个线程之间进行信息传递的Java流。  
管道流分为字节流管道流和字符管道流。  
字节管道流：PipedOutputStream 和 PipedInputStream。  
字符管道流：PipedWriter 和 PipedReader。  
PipedOutputStream、PipedWriter 是写入者/生产者/发送者；  
PipedInputStream、PipedReader 是读取者/消费者/接收者。  
字符管道流原理跟字节管道流一样，只不过底层一个是 byte 数组存储 一个是 char 数组存储的。

java的管道输入与输出实际上使用的是一个循环缓冲数来实现的。输入流PipedInputStream从这个循环缓冲数组中读数据，输出流PipedOutputStream往这个循环缓冲数组中写入数据。当这个缓冲数组已满的时候，输出流PipedOutputStream所在的线程将阻塞；当这个缓冲数组为空的时候，输入流PipedInputStream所在的线程将阻塞。

在使用管道流之前，需要注意以下要点：

. 管道流仅用于多个线程之间传递信息，若用在同一个线程中可能会造成死锁；  
管道流的输入输出是成对的，一个输出流只能对应一个输入流，使用构造函数或者connect函数进行连接；  
一对管道流包含一个缓冲区，其默认值为1024个字节，若要改变缓冲区大小，可以使用带有参数的构造函数；  
管道的读写操作是互相阻塞的，当缓冲区为空时，读操作阻塞；当缓冲区满时，写操作阻塞；  
管道依附于线程，因此若线程结束，则虽然管道流对象还在，仍然会报错“read dead end”；  
管道流的读取方法与普通流不同，只有输出流正确close时，输出流才能读到-1值。

## [Semaphore](https://so.csdn.net/so/search?q=Semaphore&spm=1001.2101.3001.7020)(信号量)

转载 ：  
https://blog.csdn.net/weixin\_29371981/article/details/114112743 Java中的信号量（Semaphore）  
https://www.cnblogs.com/skywang12345/p/3534050.html（Java多线程系列–“JUC锁”11之 Semaphore信号量的原理和示例）  
Semaphore用于管理信号量，在并发编程中，可以控制返访问同步代码的线程数量。  
Semaphore在实例化时传入一个int值，也就是指明信号数量。主要方法有两个：acquire()和release()。  
acquire()用于请求信号，每调用一次，信号量便少一个。release()用于释放信号，调用一次信号量加一个。  
信号量用完以后，后续使用acquire()方法请求信号的线程便会加**入阻塞队列挂起**。

Semaphore对于信号量的控制是基于AQS(AbstractQueuedSynchronizer)来做的。Semaphore有一个内部类Sync继承了AQS。而且Semaphore中还有两个内部类FairSync和NonfairSync继承Sync，也就是说Semaphore有公平锁和非公平锁之分。以下是Semaphore中内部类的结构：

![在这里插入图片描述](https://img-blog.csdnimg.cn/474ffacfbe114f43a495dd7ada71297f.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LiA56yR5oKs5ZG9,size_20,color_FFFFFF,t_70,g_se,x_16)

## MessageQueue

转载：  
https://blog.csdn.net/JAVABBQWE/article/details/119009229  
https://me.tongleer.com/post-46.html （【Java】目前常用的消息队列(Message Queue)对比）

1、生产者（Producer）： 负责产生消息；  
2、消费者（Consumer）： 负责消费消息；  
3、消息（Message）： 在应用间传送的数据。消息可以非常简单，比如只包含文本字符串，也可以更复杂，可能包含嵌入对象；  
4、消息队列（Message Queue）： 一种应用间的通信方式，消息发送后可以立即返回，由消息系统来确保消息的可靠传递。消息发布者只管把消息发布到 MQ 中而不用管谁来取，消息使用者只管从 MQ 中取消息而不管是谁发布的。这样发布者和使用者都不用知道对方的存在。  
5、消息代理（Message Broker）： 负责存储/转发消息，转发分为推和拉两种。  
拉是指Consumer主动从Message Broker获取消息；  
推是指Message Broker主动将Consumer感兴趣的消息推送给Consumer。  
1、消息至多被消费一次  
该场景是最容易满足的，特点是整个消息队列吞吐量大，实现简单。适合能容忍丢消息，消息重复消费的任务。  
2、消息至少被消费一次  
适合不能容忍丢消息，但允许重复消费的任务。

## 共享[内存](https://so.csdn.net/so/search?q=%E5%86%85%E5%AD%98&spm=1001.2101.3001.7020)(SharedMemory)

转载  
https://www.cnblogs.com/swbzmx/p/5992247.html

在jdk1.4中提供的类MappedByteBuffer为我们实现共享内存提供了较好的方法。该缓冲区实际上是一个磁盘文件的内存映像。二者的变化将保持同步，即内存数据发生变化会立刻反映到磁盘文件中，这样会有效的保证共享内存的实现。  
**MappedByteBuffer**  
Java 的 MappedByteBuffer 总是与某个物理文件相关的，因为不管你是从 FileInputStream、FileOutputStream 还是 RandomAccessFile 得来的 FileChannel，再 map() 得到的内存映射文件 MappedByteBuffer，如果在构造 FileInputStream、FileOutputStream、RandomAccessFile 对象时不指定物理文件便会有 FileNotFoundException 异常。  
所以 Java NIO 来实现共享内存的办法就是让不同进程的内存映射文件关联到同一个物理文件，因为 MappedByteBuffer 能让内存与文件即时的同步内容。  
用图来理解 Java NIO 的“共享内存”的实现原理：  
![在这里插入图片描述](https://img-blog.csdnimg.cn/7993438790d249db979b662bb3da64e2.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LiA56yR5oKs5ZG9,size_20,color_FFFFFF,t_70,g_se,x_16)

将共享内存和磁盘文件建立联系的是文件通道类：FileChannel。该类的加入是JDK为了统一对外部设备（文件、网络接口等）的访问方法，并且加强了多线程对同一文件进行存取的安全性。例如读写操作统一成read和write。这里只是用它来建立共享内存用，它建立了共享内存和磁盘文件之间的一个通道。

打开一个文件建立一个文件通道可以用RandomAccessFile类中的方法getChannel。该方法将直接返回一个文件通道。该文件通道由于对应的文件设为随机存取文件，一方面可以进行读写两种操作，另一方面使用它不会破坏映像文件的内容（如果用FileOutputStream直接打开一个映像文件会将该文件的大小置为0，当然数据会全部丢失）。这里，如果用 FileOutputStream和FileInputStream则不能理想的实现共享内存的要求，因为这两个类同时实现自由的读写操作要困难得多。

```
//   获得一个只读的随机存取文件对象 
RandomAccessFile   RAFile   =   new   RandomAccessFile(filename,"r");   
//   获得相应的文件通道 
FileChannel   fc   =   RAFile.getChannel();   
//   取得文件的实际大小，以便映像到共享内存 
int   size   =   (int)fc.size();   
//   获得共享内存缓冲区，该共享内存只读 
MappedByteBuffer   mapBuf   =   fc.map(FileChannel.MAP_RO,0,size);   
//   获得一个可读写的随机存取文件对象 
RAFile   =   new   RandomAccessFile(filename,"rw");   
//   获得相应的文件通道 
fc   =   RAFile.getChannel();   
//   取得文件的实际大小，以便映像到共享内存 
size   =   (int)fc.size();   
//   获得共享内存缓冲区，该共享内存可读写 
mapBuf   =   fc.map(FileChannel.MAP_RW,0,size);   
//   获取头部消息：存取权限 
mode   =   mapBuf.getInt();  
```

## [socket](https://so.csdn.net/so/search?q=socket&spm=1001.2101.3001.7020)

转载  
https://blog.csdn.net/u014209205/article/details/80461122  
https://www.jianshu.com/p/cde27461c226

![我们将的socket属于传输层](https://img-blog.csdnimg.cn/5bdbb75d07994893a4efb3f0b57df1ab.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBA5LiA56yR5oKs5ZG9,size_20,color_FFFFFF,t_70,g_se,x_16)





[Java 网络编程 | 菜鸟教程](https://www.runoob.com/java/java-networking.html)