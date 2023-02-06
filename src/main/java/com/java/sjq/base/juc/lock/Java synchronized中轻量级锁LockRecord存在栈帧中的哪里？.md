[栈帧中存在局部变量表，操作数栈，动态链接和方法地址，都说synchronized中轻量级锁的LockRecord是在栈帧中开辟的空间，到底存在栈帧中的哪一个结构当中，还是一个新的结构？](https://www.zhihu.com/question/409046921#:~:text=LockRecord%E7%94%A8%E4%BA%8E%E8%BD%BB%E9%87%8F%E7%BA%A7%E9%94%81%E4%BC%98%E5%8C%96%EF%BC%8C%E5%BD%93%E8%A7%A3%E9%87%8A%E5%99%A8%E6%89%A7%E8%A1%8Cmonitorenter%E5%AD%97%E8%8A%82%E7%A0%81%E8%BD%BB%E5%BA%A6%E9%94%81%E4%BD%8F%E4%B8%80%E4%B8%AA%E5%AF%B9%E8%B1%A1%E6%97%B6%EF%BC%8C%E5%B0%B1%E4%BC%9A%E5%9C%A8%E8%8E%B7%E5%8F%96%E9%94%81%E7%9A%84%E7%BA%BF%E7%A8%8B%E7%9A%84%E6%A0%88%E4%B8%8A%E6%98%BE%E5%BC%8F%E6%88%96%E8%80%85%E9%9A%90%E5%BC%8F%E5%88%86%E9%85%8D%E4%B8%80%E4%B8%AALockRecord.%E8%BF%99%E4%B8%AALockRecord%E5%AD%98%E5%82%A8%E9%94%81%E5%AF%B9%E8%B1%A1markword%E7%9A%84%E6%8B%B7%E8%B4%9D%20%28Displaced,Mark%20Word%29%EF%BC%8C%E5%9C%A8%E6%8B%B7%E8%B4%9D%E5%AE%8C%E6%88%90%E5%90%8E%EF%BC%8C%E9%A6%96%E5%85%88%E4%BC%9A%E6%8C%82%E8%B5%B7%E6%8C%81%E6%9C%89%E5%81%8F%E5%90%91%E9%94%81%E7%9A%84%E7%BA%BF%E7%A8%8B%EF%BC%8C%E5%9B%A0%E4%B8%BA%E8%A6%81%E8%BF%9B%E8%A1%8C%E5%B0%9D%E8%AF%95%E4%BF%AE%E6%94%B9%E9%94%81%E8%AE%B0%E5%BD%95%E6%8C%87%E9%92%88%EF%BC%8CMarkWord%E4%BC%9A%E6%9C%89%E5%8F%98%E5%8C%96%EF%BC%8C%E6%89%80%E6%9C%89%E7%BA%BF%E7%A8%8B%E4%BC%9A%E5%88%A9%E7%94%A8CAS%E5%B0%9D%E8%AF%95%E5%B0%86MarkWord%E7%9A%84%E9%94%81%E8%AE%B0%E5%BD%95%E6%8C%87%E9%92%88%E6%94%B9%E4%B8%BA%E6%8C%87%E5%90%91%E8%87%AA%E5%B7%B1%20%28%E7%BA%BF%E7%A8%8B%29%E7%9A%84%E9%94%81%E8%AE%B0%E5%BD%95%EF%BC%8C%E7%84%B6%E5%90%8Elockrecord%E7%9A%84owner%E6%8C%87%E5%90%91%E5%AF%B9%E8%B1%A1%E7%9A%84markword%EF%BC%8C%E4%BF%AE%E6%94%B9%E6%88%90%E5%8A%9F%E7%9A%84%E7%BA%BF%E7%A8%8B%E5%B0%86%E8%8E%B7%E5%BE%97%E8%BD%BB%E9%87%8F%E7%BA%A7%E9%94%81%E3%80%82)
> LockRecord用于轻量级锁优化， 当解释器执行monitorenter字节码轻度锁住一个对象时， 就会在获取锁的线程的栈上显式或者隐式分配一个LockRecord.
>
> 这个LockRecord`存储锁对象markword的拷贝(Displaced Mark Word)`，**在拷贝完成后，首先会`挂起`持有偏向锁的线程，因为要进行尝试修改锁记录指针**，MarkWord会有变化，**所有线程会利用CAS尝试将MarkWord的锁记录指针改为指向自己(线程)的锁记录，然后lockrecord的owner指向对象的markword**，修改成功的线程将获得轻量级锁。失败则线程升级为`重量级锁。`**释放时会检查markword中的lockrecord指针是否指向自己(获得锁的线程lockrecord)，使用原子的CAS将Displaced Mark Word替换回对象头，如果成功，则表示没有竞争发生，如果替换失败则升级为`重量级锁`**。整个过程中，LockRecord是一个线程内独享的存储，每一个线程都有一个可用Monitor Record列表。

### [JVM MarkWord与Lock Record && 锁](https://blog.csdn.net/slslslyxz/article/details/106363990)

-   [MarkWord](#MarkWord_2)
-   [Lock Record](#Lock_Record_10)
-   [偏向锁](#_12)
-   [轻量级锁](#_21)
-   [重量级锁](#_62)
-   -   [Windows中的Mutex](#WindowsMutex_64)
-   [Linux中的Mutex](#LinuxMutex_69)

# MarkWord

翻译过来就是标记字，既然是标记，那么就肯定是用来记录一些信息，`x86下4字节，x64下8字节`，后面紧跟着klass pointer，目前jvm基本都是x64，我们都知道在x86下地址是4字节大小，在x64下地址是8字节大小，但JAVA默认情况下开启了指针压缩，将8字节指针压缩成4字节，但是内存对齐机制导致无论如何，在x64下这俩加起来后占用的空间都是16字节。

还有OopPointer(就是类中还有类成员，比如String xxxx,这种对象的指针，这玩意不在Markword中，只是顺嘴提一句)，同样默认开启了指针压缩，在x64下同样也是4字节。这和C中不同。

这里要说一句的是，GC回收的年龄为什么默认是15（Java1.8 PSPO）,因为在markword中age只有4bits，而4bits最大也只能是15。

关于MarkWord具体结构就不画个图了，网上有很多资料，虽然有些是x86有些x64，但是总的来说结构不变，无非是hashcode区域大小变化了。

# Lock Record

LockRecord在获取轻量级锁时才会有用，有对象markword的拷贝(Displaced Mark Word)，里面owner指向对象的markword，并将对象的markword的锁记录指针指向获得锁线程的LockRecord.。

# 偏向锁

默认情况下偏向锁开关是开启的，也可以手动关闭，MarkWord的最后2个bit存储着锁标志，0b01为无锁状态或偏向锁状态，0b00为轻量级锁，0b10为重量级锁，0b11为GC标记

`无锁状态和偏向锁状态是相同的，因为仅2bit也没办法再区分了，并且偏向锁只是逻辑上的锁，是一个概念，所以没有将它归入锁标志位，那么如何区分他是否处于偏向锁呢？在倒数第三个bit就是做这个事的。为0b1的情况下，就是处于偏向锁状态了`

<u>偏向锁干的事就是为了保证线程重入的情况下，可以不触发CAS或Mutex锁</u>，由MarkWord中的ThreadID判断是否是当前线程(比如递归调用)，如果是那么跳过获取锁的过程。

`偏向锁不会主动释放，也就是说它不会在工作完成后给你恢复ThreadID等等信息，只有在其他线程来竞争时，才会被释放。因为偏向锁主要做的事就是为了可重入。`

# 轻量级锁

轻量级锁也就是自旋锁，利用CAS尝试获取锁。如果你确定某个方法同一时间确实会有一堆线程访问，而且工作时间还挺长，那么我建议直接用重量级锁，不要使用synchronized，因为在CAS过程中，CPU是不会进行线程切换的，这就导致CAS失败的情况下他会浪费CPU的分片时间，都用来干这个事了。  
轻量级锁是在发现`2个不同线程在竞争时`才会由`偏向锁升级为轻量级锁`。  
说到轻量级锁就要说到`LockRecord`。这里重点说一下`为什么需要LockRecord`.

有些人可能会想，利用CAS交换ThreadID尝试获取自旋锁不就行了？还需要LockRecord做什么？上面说了，偏向锁是不会主动释放的，至于为什么不主动释放咱也不清楚，咱也不敢问，你无法通过ThreadID知道当前是否还在工作还是工作完成了。  
其实为什么不主动释放是因为，如果没有竞争关系，轻量级锁的cas根本不会浪费时间，直接就会成功，所以没有必要去对偏向锁的flag和threadid进行修改。

> LockRecord是线程私有的(TLS)，说人话就是每个线程有自己的一份锁记录，在创建完锁记录的空间后，会将当前对象的MarkWord拷贝到锁记录中(Displaced Mark Word)。这是为什么呢？

> 因为当前对象的MarkWord的结构会有所变化，不再是存着hashcode等信息，将会`出现一个指向LockRecord的指针，指向锁记录`。  
> 每个线程有自己的锁记录，那么`指向哪个线程中的锁记录`呢？

> -   加锁:  
      >     在拷贝完成后，首先会`挂起`持有偏向锁的线程，因为要进行尝试修改锁记录指针，MarkWord会有变化，`所有线程会利用CAS尝试将MarkWord的锁记录指针改为指向自己(线程)的锁记录`，然后lockrecord的owner指向对象的markword，修改成功的线程将获得轻量级锁。失败则线程升级为重量级锁。

> -   释放：  
      >     释放时会检查markword中的lockrecord指针是否指向自己(获得锁的线程lockrecord)，使用原子的CAS将Displaced Mark Word替换回对象头，如果成功，则表示没有竞争发生，如果替换失败则升级为重量级锁。

> 上面说了，偏向锁是为了可重入性，并不解决竞争问题，轻量级锁是在有偏向锁的情况下尝试获取锁，最好的情况就是获取偏向锁的线程已经完成了工作，轻量级锁是第二个线程进来时获取的，那么在释放时优先考虑并没有升级到重量级锁，如果有那么释放时CAS会失败，因为其他线程升级成为重量级锁后`markword`会被改为`膨胀状态`，此时这个获取到轻量级锁的线程释放时也会膨胀为重量级锁，看下面的代码。

```cpp
  markOop dhw = lock->displaced_header();
  .....
  mark = object->mark() ;
  .....
  //释放时cas成功就返回 如果失败说明其他线程已经膨胀成重量级锁了
  if ((markOop) Atomic::cmpxchg_ptr (dhw, object->mark_addr(), mark) == mark) {
        TEVENT (fast_exit: release stacklock) ;
        return;
  }
  //继续向下走就会释放且膨胀为重量级锁 下次此线程再竞争，就是重量级锁的竞争了
  ObjectSynchronizer::inflate(THREAD, object)->exit (true, THREAD) ;




123456789101112131415
```

如果这个本来获取到轻量级锁的线程释放时不去将自己也升级到重量级锁释放，那么其他重量级锁的线程mutex就会没有人去通知，关于mutex继续往下看，我最早接触mutex应该是《windows核心编程》这本书所讲，还讲了私有命名空间，边界对内核对象的影响，下面只是粗略写一下，因为这文章毕竟是java专栏下的，写的太复杂普通java小伙伴看不懂。

# 重量级锁

重量级锁jvm也是使用native code，也就是系统中的互斥体了，Windows和[Linux](https://so.csdn.net/so/search?q=Linux&spm=1001.2101.3001.7020)中都是mutex内核对象。

## Windows中的Mutex

Windows的Mutex对象可以通过用户层API CreateMutex获取(真正的内核层操作需要驱动，获取的对象其实都是属于内核层的mutex，因为这是windows公开的API)，mutex和event对象很像，都有一个触发状态，CreateMutex 第二个参数为是否被占用，也就是锁的初始状态，如果为false，说明没有被使用，处于触发状态。  
创建后使用WaitForSingleObject来等待对象触发，当WaitForSingleObject等待的对象触发后将被设为未触发状态，其他没有等待到触发事件的线程就会被挂起，并且等待固定时长，这个时间是WaitForSingleObject第二个参数，当调用ReleaseMutex后，Mutex对象又会被设置为触发状态，其他线程就会获取到锁了。CloseHandle关闭内核对象句柄就会解除当前进程与Mutex的关系。不过Mutex不只是锁那么简单。  
当没有在私有命名空间中（默认的命名空间）创建了一个名为"test"的互斥体却没有释放，其他进程在同样的命名空间下无法创建同名互斥体，也就是说它可以做到进程的互斥。

## Linux中的Mutex

linux所有和多线程有关的API 基本都是pthread开头，说点无关紧要的玩意，事实上在linux中使用C写程序的情况下，基本很少有人使用线程，因为在linux中线程其实也可以看作进程，如果拿linux的线程和windows的线程比，linux的线程更像是伪线程。

Linux中的mutex\_t实际上是一个count为1的信号量，但NPTL的pthread mutex有些不同。  
初始化一个互斥体：pthread\_mutex\_init()  
加锁：pthread\_mutex\_lock()/pthread\_mutex\_trylock()  
释放：pthread\_mutex\_unlock()  
注销互斥体：pthread\_mutex\_destory()

简单地说就是在默认锁类型情况下，锁结构中的锁标志int \_\_lock，初始化为0，加锁时先判断锁标志是否为0，如果是则置为1，如果不是则调用\_\_lll\_lock\_wait，里面实际上在while中cas将锁标志置为2继续调用lll\_futex\_timed\_wait。