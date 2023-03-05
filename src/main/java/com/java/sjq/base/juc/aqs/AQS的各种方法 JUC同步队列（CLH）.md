> [J.U.C|同步队列（CLH）](https://www.jianshu.com/p/6fc0601ffe34)
> 
> [Java中的AQS（二）同步状态的获取与释放](https://blog.csdn.net/yanghan1222/article/details/80248494)

# 1. releaseShared()
> [奈学：reaseShared共享式释放锁](https://zhuanlan.zhihu.com/p/188691999)
1. 调用`tryReleaseShared`尝试释放共享锁，这里必须实现为线程安全。
2. 如果释放了锁，那么调用`doReleaseShared`方法循环后继结点，实现唤醒的传播。
   doReleaseShared方法中，只要头部head不为空，并且头部head、尾部tail不是同一个节点，
   再并且头部head的登台状态waitStatus为Node.SIGNAL，那么就会释放后继节点 unparkSuccessor。
3. `unparkSuccessor`。如果head.next 为空null或者head.next 状态waitStatus大于0（Node.CANCELED）, 
   unparkSuccessor 会尝试找到（从tail往前找）后继节点中，没有被取消（状态大于0，即状态不为Node.CANCELED）的第一个非空节点 s。
   找到了就调用 LockSupport.unpark(s.thread) 唤醒 s 持有的线程
   

todo 其他方法学习

等待队列节点类。

等待队列是“CLH”（Craig、Landin 和 Hagersten）锁定队列的变体。</br
CLH 锁通常用于旋转锁。我们改为使用它们来阻止同步器，但使用相同的基本策略，即在其节点的前身中保存有关线程的一些控制信息。</br
每个节点中的“状态”字段跟踪线程是否应阻塞。节点在其前置任务release时发出signal信号。</br>
否则，队列的每个节点都充当保存单个等待线程的特定通知样式监视器。但是，状态字段并不控制是否授予线程锁lock等。</br>
线程可能会尝试获取它是否在队列中的第一个。但成为第一并不能保证成功;它只赋予了竞争的权利(竞争失败通知下一个节点)。因此，当前发布的竞争者线程可能需要重新等待。</br>

要排队进入 CLH 锁，请以原子方式将其拼接为新的尾巴。
要取消排队，您只需设置 head 字段。
+------+  prev +-----+       +-----+
head |      | <---- |     | <---- |     |  tail
+------+       +-----+       +-----+

插入 CLH 队列只需要在“尾部”上进行单个原子操作，因此有一个从未排队到排队的简单原子分界点。同样，出列仅涉及更新“头”。但是，节点需要更多的工作来确定它们的继任者是谁，部分原因是为了处理由于超时和中断而可能取消的问题。
“prev”链接（未在原始 CLH 锁中使用）主要用于处理取消。如果节点被取消，其后继节点（通常）将重新链接到未取消的前置节点。有关自旋锁情况下类似力学的解释，请参阅Scott和Scherer在 http://www.cs.rochester.edu/u/scott/synchronization/
我们还使用“下一步”链接来实现阻塞机制。每个节点的线程 ID 都保存在其自己的节点中，因此前置节点通过遍历下一个链接来确定它是哪个线程来指示下一个节点唤醒。确定继任者必须避免与新排队的节点竞争，以设置其前身的“下一个”字段。必要时，当节点的后继节点显示为空时，通过从原子更新的“尾巴”向后检查来解决此问题。（或者，换句话说，下一个链接是一种优化，因此我们通常不需要向后扫描。
取消给基本算法带来了一些保守主义。由于我们必须轮询取消其他节点，因此我们可能会忽略被取消的节点是在我们前面还是后面。这个问题的处理方式是，在取消后总是不停放继任者，使他们能够稳定在新的前任上，除非我们能够确定一个未取消的前任将承担这一责任。
CLH 队列需要一个虚拟标头节点才能开始使用。但是我们不会在施工上创造它们，因为如果从来没有争用，那将是浪费精力。相反，构造节点，并在第一次争用时设置头和尾指针。
等待条件的线程使用相同的节点，但使用其他链接。条件只需要链接简单（非并发）链接队列中的节点，因为它们仅在独占时访问。等待时，节点将插入到条件队列中。发出信号后，节点被转移到主队列。状态字段的特殊值用于标记节点所在的队列。
感谢Dave Dice，Mark Moir，Victor Luchangco，Bill Scherer和Michael Scott，以及JSR-166专家组的成员，对本课程的设计提出了有用的想法，讨论和批评。

[AQS（AbstractQueuedSynchronizer）源码解析](https://juejin.cn/post/7197668180842938423)
# 2. enq
```java
private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
```

第一次 enq()
head==tail 表示没有阻塞线程。
然后 for 循环发现 Node t = tail，t不为null
于是插入第一个阻塞的 node1
这个时候 head.next 等于 node1，同时 node1 == tail。




# 3. acquire
独占式获取同步状态。
acquire方法首先会调用tryAcquire方法尝试获取锁，
如果获取锁失败,首先通过addWaiter将当前线程放入CLH队列中，然后通过acquireQueued方法获取锁，
```java
//不可中断的获取锁
 public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        //对中断做补偿，中断当前线程。因为acquireQueued不支持中断
            selfInterrupt();
    }
```
# 4 tryAcquire
子类实现。以 ReentrantReadWriteLock.java 中的 Sync 实现举例
> [读写锁 - ReentrantReadWriteLock ](https://my.oschina.net/u/2337927/blog/538188)
> 注意： ReentrantReadWrilteLock 里面将 state 这个字段一分为二，高位 16 位表示共享锁的数量，低位 16 位表示独占锁的数量（或者重入数量）
```java
protected final boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = getState();
            int w = exclusiveCount(c);
            if (c != 0) {
                // (Note: if c != 0 and w == 0 then shared count != 0)
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w + exclusiveCount(acquires) > MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // Reentrant acquire
                setState(c + acquires);
                return true;
            }
            if (writerShouldBlock() ||
                !compareAndSetState(c, c + acquires))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }
```
解释：

一、 c!=0 说明有锁了，
1. w==0 成立说明有多个读锁在用了，这时不能获取写锁，因为写锁将导致数据乱掉
2. w！=0 成立 说明当前有写锁，如果该线程进来竞争写锁又不是当前线程的话则不行，他们是独占的，同时如果写锁进来后加起来超过了 65535 (即 16 位都为 1 的情况下再进来一个写锁的话) 则报错，超出了能记录的锁数量限制

二、 c==0 说明没有任何的锁，

`writerShouldBlock() `判断写是不是需要阻塞 (这个是公平锁和非公平锁的阻塞情况)，

- 非公平锁直接返回 false
```java
static final class NonfairSync extends Sync {
    //……
        final boolean writerShouldBlock() {
            return false; // writers can always barge
        }
    }
```

- 公平锁

如果 AQS 队列不为空或者当前线程不在队列头部那么就阻塞队列（即加入到同步队列中自身不断自旋）

后面判断获取写锁，失败直接 false，其他则 true
```java
static final class FairSync extends Sync {
        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }
```
```java
public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }
```
# 5. acquireQueued
当前线程为CLH中的第一个阻塞线程才会尝试去获取锁。获取成功则更新head。
```java
final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
        	//记录中断状态
            boolean interrupted = false;
            //自旋式的获取锁
            for (;;) {
                final Node p = node.predecessor();
                //当前线程为CLH中的第一个阻塞线程才会尝试去获取锁
                if (p == head && tryAcquire(arg)) {
                	//获取成功则更新head
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    //返回中断状态
                    return interrupted;
                }
                //判断中断信息
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
        	//如果获取锁失败，则取消获取锁的操作
            if (failed)
                cancelAcquire(node);
        }
    }
```

acquireQueued方法是无中断的获取锁，该方法有一个布尔类型的返回值， 该值不是表示是否成功获取锁，而是标示当前线程的中断状态， 因为acquireQueued方法是无法响应中断的，需要对中断进行补偿， 这个补偿体现在acquire方法中。

# acquireInterruptibly与 doAcquireInterruptibly 支持中断的获取锁
```java
    //可中断的获取锁
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }
```
acquireInterruptibly 方法获取锁的过程中能够响应中断，主要体现在获取锁之前会判断一下当前线程的中断中断状态，若中断则抛出InterruptedException，然后通过tryAcquire获取锁，获取成功直接返回，获取失败则通过doAcquireInterruptibly获取锁，该方法和acquireQueued最大的区别就是在判断parkAndCheckInterrupt后，acquireQueued仅仅记录中断状态，parkAndCheckInterrupt则会抛出异常。

```java
    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    //抛出异常，响应中断
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

```



# unparkSuccessor 什么时候被调用。

release -> unparkSuccessor
releaseShared -> tryReleaseShared -> doReleaseShared -> unparkSuccessor
cancelAcquire -> unparkSuccessor