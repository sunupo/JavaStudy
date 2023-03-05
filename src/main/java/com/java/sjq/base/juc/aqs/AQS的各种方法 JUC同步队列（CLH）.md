> [J.U.C|同步队列（CLH）](https://www.jianshu.com/p/6fc0601ffe34)
>
> [Java中的AQS（二）同步状态的获取与释放](https://blog.csdn.net/yanghan1222/article/details/80248494)
>
> [(122条消息) 说一下AQS的原理？\_aqs唤醒节点\_蜀州凯哥的博客-CSDN博客](https://blog.csdn.net/m0_72088858/article/details/126407567)

等待队列节点类。

等待队列是“CLH”（Craig、Landin 和 Hagersten）锁定队列的变体。</br
CLH 锁通常用于旋转锁。我们改为使用它们来阻止同步器，但使用相同的基本策略，即在其节点的前身中保存有关线程的一些控制信息。</br
**每个节点中的“状态”字段跟踪线程是否应阻塞。节点在其前置任务release时发出signal信号**（**或者前置任务获取锁失败**）。</br>
否则，队列的每个节点都充当保存单个等待线程的特定通知样式监视器。但是，状态字段并不控制是否授予线程锁lock等。</br>
**线程可能会尝试获取它是否在队列中的第一个。但成为第一并不能保证成功;它只赋予了竞争的权利(竞争失败通知下一个节点)**。因此，当前发布的竞争者线程可能需要重新等待。</br>

**要排队进入 CLH 锁，请以原子方式将其拼接为新的尾巴。**
**要取消排队，您只需设置 head 字段。（设置为head 那么将会是接下来第一个去竞争锁）**

```
* <pre>
*      +------+  prev +-----+       +-----+
* head |      | <---- |     | <---- |     |  tail
*      +------+       +-----+       +-----+
* </pre>
```

插入 CLH 队列只需要在“尾部”上进行单个原子操作，因此有一个从未排队到排队的简单原子分界点。
同样，出列仅涉及更新“头”。但是，节点需要更多的工作来确定它们的继任者是谁，部分原因是为了处理由于超时和中断而可能取消的问题。
“prev”链接（未在原始 CLH 锁中使用）主要用于处理取消。如果节点被取消，其后继节点（通常）将重新链接到未取消的前置节点。
有关自旋锁情况下类似力学的解释，请参阅Scott和Scherer在 http://www.cs.rochester.edu/u/scott/synchronization/

我们还使用“下一步”链接来实现阻塞机制。每个节点的线程 ID 都保存在其自己的节点中，因此前置节点通过遍历下一个链接来确定它是哪个线程来指示下一个节点唤醒。确定继任者必须避免与新排队的节点竞争，以设置其前身的“下一个”字段。必要时，当节点的后继节点显示为空时，通过从原子更新的“尾巴”向后检查来解决此问题。（或者，换句话说，下一个链接是一种优化，因此我们通常不需要向后扫描。
取消给基本算法带来了一些保守主义。由于我们必须轮询取消其他节点，因此我们可能会忽略被取消的节点是在我们前面还是后面。这个问题的处理方式是，在取消后总是不停放继任者，使他们能够稳定在新的前任上，除非我们能够确定一个未取消的前任将承担这一责任。
CLH 队列需要一个虚拟标头节点才能开始使用。但是我们不会在施工上创造它们，因为如果从来没有争用，那将是浪费精力。相反，构造节点，并在第一次争用时设置头和尾指针。
**等待条件的线程使用相同的节点，但使用其他链接**。条件只需要链接简单（非并发）链接队列中的节点，因为它们仅在独占时访问。等待时，节点将插入到条件队列中。发出信号后，节点被转移到主队列。状态字段的特殊值用于标记节点所在的队列。
感谢Dave Dice，Mark Moir，Victor Luchangco，Bill Scherer和Michael Scott，以及JSR-166专家组的成员，对本课程的设计提出了有用的想法，讨论和批评。

[AQS（AbstractQueuedSynchronizer）源码解析](https://juejin.cn/post/7197668180842938423)

# 1. releaseShared()
> [奈学：reaseShared共享式释放锁](https://zhuanlan.zhihu.com/p/188691999)
1. 调用`tryReleaseShared`尝试释放共享锁，这里必须实现为线程安全。
2. 如果释放了锁，那么调用`doReleaseShared`方法循环后继结点，实现唤醒的传播。
   doReleaseShared方法中，只要头部head不为空，并且头部head、尾部tail不是同一个节点，
   再并且头部head的等待状态waitStatus为Node.SIGNAL，那么就会释放后继节点 unparkSuccessor。
3. `unparkSuccessor`。如果head.next 为空null或者head.next 状态waitStatus大于0（Node.CANCELED）, 
   unparkSuccessor 会尝试找到（从tail往前找）后继节点中，没有被取消（状态大于0，即状态不为Node.CANCELED）的第一个非空节点 s。
   找到了就调用 LockSupport.unpark(s.thread) 唤醒 s 持有的线程

# 2. addWaiter

快速入队不用循环cas，成功的前提是 tail 不为null。

否则用enq入队，循环cas入队。

```java
private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // Try the fast path of enq; backup to full enq on failure
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }
```

## enq

第一次 enq()
head==tail 表示没有阻塞线程。
然后 for 循环发现 Node t = tail，t不为null
于是插入第一个阻塞的 node1
这个时候 head.next 等于 node1，同时 node1 == tail。

```java
 
    private Node enq(final Node node) {
        // 一直for循环，直到插入Node成功为止
        for (;;) {
            //拿到AQS指向的尾节点
            Node t = tail;
            if (t == null) {
                // CAS设置首节点，如果设置不成功，那么又会来到下一次循环进行设置，设置的过程CAS原子性
                // 注意，这里不是目前线程所在节点，而是new了一个新的空节点
                //这里头部已经指向了空节点，而尾部任然为空节点，造成队列h != t，但是head.next = null的场景，也就是看起来大于一个节点实际上只有一个节点的假象
                if (compareAndSetHead(new Node()))
                    //设置成功，AQS指向的头部变成了新的空节点，开始下一次循环
                    tail = head;
            } else {
                //最后一次循环，AQS的尾部已经有尾部节点了，那么来到else这一边
                //将尾部节点赋值给
                node.prev = t;
                // CAS设置尾结点
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
                //此后的尾节点有两种，
                // 1. 一种的包含着线程的尾节点(compareAndSetTail成功)，
                // 2. 还有一种是队中只有一个的空节点（compareAndSetTail失败）,再次进入for循环设置compareAndSetTail
            }
        }
    }
```






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
子类实现。以 `ReentrantReadWriteLock.java` 中的 Sync 实现举例
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
**当前线程为CLH中的第一个阻塞线程才会尝试去获取锁。获取成功则更新head。**

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

## setHead()

```java
private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }
```



## shouldParkAfterFailedAcquire

acquireQueued 中`if (p == head && tryAcquire(arg))`如果是「前驱节点」是头节点并获取得到锁，则把当前节点设置为头结点`setHead(node);`，并且将前驱节点置空（实际上就是原有的头节点已经释放锁了）

没获取得到锁，`shouldParkAfterFailedAcquire(p, node)`则判断前驱节点的状态`pred.waitStatus`。

是 SIGNAL（-1），返回true继续判断是否被中断`parkAndCheckInterrupt()`
是 Canceled（1），则向前遍历把当前节点链接到合法的前驱节点，返回false不用再判断中断了
是其他状态（0，-2，-3），**使用CAS将状态设置为SIGNAL**，返回false不用再判断中断了

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /*
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             */
            return true;
        if (ws > 0) {
            /*
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```



# 6. acquireInterruptibly  与 doAcquireInterruptibly 支持中断
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

## doAcquireInterruptibly

【Lock的方法】lockInterruptibly() -> 【AQS】acquireInterruptibly() -> 【AQS】doAcquireInterruptibly()

### 与 acquireQUeued 区别：

- acquireQUeued 是记录中断标志位，在acquireQUeued 循环中记录的是中断标志位，是返回调用acquireQUeued 的acquire方法后用自我中断作为补偿。

- doAcquireInterruptibly 是可以中断的。体现在:
  - doAcquireInterruptibly 循环中抛出了中断异常`InterruptedException`而不是就中断标志, 最终由 `lockInterruptibly ` 抛出中断异常。
  -  acquireInterruptibly 进行了中断标志位检查`Thread.interrupted()`,如果有中断就继续抛出 `InterruptedException`，最终也由 `lockInterruptibly ` 抛出中断异常。

```java
 public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
  }
```



# 7. unparkSuccessor (node)

`unparkSuccessor`。

- 如果`当前 node` 的 ws < 0，那么把node的**ws通过cas 置为0.**
- 把node的下一个节点`Node s = node.next;`暂时当做**准备唤醒的节点**
- 如果下一个节点 `s`为空或者已经取消`（ws>0）`从尾部 tail 向前找没有取消`(ws<=0)`的节点，当做新的**准备唤醒的节点**。
- 如果最终**准备唤醒的节点**存在，就unpark唤醒它。

```java
private void unparkSuccessor(Node node) {
        /*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         */
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        /*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         */
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }
```



## 哪里调用 unparkSuccessor

release -> unparkSuccessor
releaseShared -> tryReleaseShared -> doReleaseShared -> unparkSuccessor
cancelAcquire -> unparkSuccessor

# 8. cancelAcquire

- 从当前node往前遍历找到没有取消`canceled`的先驱节点（waitstatus状态不大于0的祖先节点pre）。
- 清空当前节点node的 `thread`，并把 `waitStatus`状态设为 `canceled`。
- 如果当前节点是 tail节点，并且 cas 设置 pre 为新的节点成功，
  - 直接移除当前节点，然后用cas把 pre.next 设为空
- 否则{
  - 如果【当前节点不是tail节点】，再如果【pre结点不为head】and 【【pre的后继需要signal（当前pre的ws为signal）】or 【如果不需要后继（pre.ws<0） ，那么设置为需要后继（设置pre的ws为signal）】】and【pre的thread不为空】 and 【node.next不为空 node.next.ws<=0】，那么通过cas设置 pre.next= node.next
  - 否则唤醒node的后继节点。
- GC}

```java
 private void cancelAcquire(Node node) {
        // Ignore if node doesn't exist
        if (node == null)
            return;

        // 清空当前节点node的thread
        node.thread = null;

        // Skip cancelled predecessors
        //从当前node往前遍历找到waitstatus状态不大于0的祖先节点pre。
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
       // 用于cas 设置pre的next
        Node predNext = pred.next;

        // 把waitstatus状态设为canceled
        node.waitStatus = Node.CANCELLED;

        if (node == tail && compareAndSetTail(node, pred)) {
            // 如果当前节点是 tail节点，直接移除当前节点，然后用cas把pre.next设为空
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if (pred != head && // 【pre结点不为head】
                // [[【pre的后继需要signal（当前pre的ws为signal=-1）】or 【如果不需要后继（pre.ws<0） ，那么设置为需要后继（设置pre的ws为signal）]]
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) && // 
                // 【pre的thread不为空】
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0) // 【node.next不为空 node.next.ws<=0】（node.next 不为空也不是canceled状态）
                    compareAndSetNext(pred, predNext, next);  //通过cas设置 pre.next= node.next
            } else {
                // if失败：包括node是head的后继节点的情况下也会走到这里。
                unparkSuccessor(node); //唤醒 node的后继节点
            }

            node.next = node; // help GC
        }
    }
```





# ----------------------

# ConditionObject 是AQS内部类。

```
public class AQS.ConditionObject implements Condition,
```

[condition]signal() -> [condition]doSignal()-> [AQS]transferForSignal() -> [AQS]enq(node);  将节点从条件队列传输到同步队列

# ReadWriteLock 也是分公平和非公平

## 公平锁 FairSync

```java
/**
 * Fair version of Sync
 */
static final class FairSync extends Sync {
    private static final long serialVersionUID = -2274990926593161451L;
    final boolean writerShouldBlock() {
        return hasQueuedPredecessors();
    }
    final boolean readerShouldBlock() {
        return hasQueuedPredecessors();
    }
}
```

### hasQueuedPredecessors

读、写通过 `hasQueuedPredecessors` 判断是否阻塞。

```java



/**
查询是否有任何线程等待获取的时间超过当前线程。
此方法的调用等效于（但可能比以下方法更有效）：
getFirstQueuedThread() != Thread.currentThread() &&  hasQueuedThreads()
*/
public final boolean hasQueuedPredecessors() {
    // The correctness of this depends on head being initialized
    // before tail and on head.next being accurate if the current
    // thread is first in queue.
    Node t = tail; // Read fields in reverse initialization order
    Node h = head;
    Node s;
    return h != t &&
        ((s = h.next) == null || s.thread != Thread.currentThread()); // 没看明白
}
```

> [(122条消息) AQS——hasQueuedPredecessors方法\_Aristocrat l的博客-CSDN博客](https://blog.csdn.net/qq_57205114/article/details/124083129)

#### hasQueuedPredecessors 返回 false

- ### 第一种：h != t 为 false

  - 这种情况就是队列为空或者队列只有一个元素的情况。（队列为空很好理解，但是什么时候队列只有一个元素呢？当线程一在执行时，线程二创建了队列，当线程一执行完后唤醒acquireQueue中的线程二，线程二CAS获取了锁，将自己设置为第一个节点并置空，并将head和tail指针都指向自己，此时队列中只有一个元素。）

- ### 第二种：**h  !=  t 返回 true，(s = h.next) == null  返回 false 并且 s.thread  !=  Thread.currentThread() 也返回 false**

  - 前者返回true。意思就是队列长度大于一（绝大多数情况下是这样）。

    - 第一次enq入队，CAS设置尾结点成功。其它线程读取到head.next 不为空。

    - 第一次enq入队，CAS设置尾结点失败。**其它线程读取到 head.next 都为空。**

    - 第一次enq入队，还没有运行到CAS设置尾结点。**其它线程读取到 head.next 为空。**

      > 其它线程读取到 head.next 为空，这个时候其他线程需要排队，因为当前线程肯定是先进来排队的。
      >
      > 例子1。第一个线程写锁， 第二个线程写锁获取不到锁入队enq，第三个线程写（读）锁因为线程一释放锁就顺利进入tryAcquire（tryAcquireShared）进行 writeShouldBlock（readerShouldBlock）的判断。发现 `(s = h.next) == null `为  true（第二个线程），说明第三个线程需要阻塞。
      >
      > 例子2。第一个线程读锁， 第二个线程写锁获取不到锁入队enq，第三个线程写（读）锁因为线程一释放锁就顺利进入tryAcquire（

  - 后者返回false。意味着头结点的下一个节点不为空（弥补了前者的极少数情况）。

    - 第一次enq入队，CAS设置尾结点失败），也就是队列长度大于1。后面一句也就是当前执行的线程就是头部空节点下一个节点的线程。
      



#### 哪里调用 hasQueuedPredecessor

<img src="AQS%E7%9A%84%E5%90%84%E7%A7%8D%E6%96%B9%E6%B3%95%20JUC%E5%90%8C%E6%AD%A5%E9%98%9F%E5%88%97%EF%BC%88CLH%EF%BC%89.assets/image-20230303141424318.png" alt="image-20230303141424318" style="zoom:50%;" />

jdk 源码可以看到以下几种情况。

- ReentrantLock 的 FairLock 的 tryRequire
- ReentrantReadWriteLock 的 FairSync 的 readerShouldBlock（被tryAcquireShared调用）
- ReentrantReadWriteLock 的 FairSync 的 writerShouldBlock（被tryAcquire调用）
- Semaphore 的 FairSync 的 tryAcquireShared



## 非公平锁

读：`apparentlyFirstQueuedIsExclusive` 判断一下。作为避免无限期 writer 饥饿的启发式方法，如果存在waiting writer 的线程暂时在队列的头部head，则阻塞。 这只是一个概率效应，因为在可用的 reader（65535个）没有耗尽的情况下，如果其它reader后面有一个等待的writer，则新的reader不会阻塞。

```java
final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }
```





```java

static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;
        final boolean writerShouldBlock() {
            return false; // writers can always barge
        }
        final boolean readerShouldBlock() {
            /* As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             */
            return apparentlyFirstQueuedIsExclusive();
        }
    }
```



