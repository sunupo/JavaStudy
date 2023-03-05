[AQS（AbstractQueuedSynchronizer）源码解析 - 掘金](https://juejin.cn/post/7197668180842938423)

AbstractQueuedSynchronizer被称为队列同步器，简称为大家熟知的AQS，这个类可以称作concurrent包的基础，该类提供了同步的基本功能。该类包括如下几个核心要素：

-   AQS内部维护一个volatile修饰的state变量，state用于标记锁的状态；
-   AQS通过内部类Node记录当前是哪个线程持有锁；
-   AQS通过LockSupport的park和unPark方法来阻塞和唤醒线程；
-   AQS通过node来维护一个队列，用于保存所有阻塞的线程。

下面通过剖析源码来看看AQS是如何工作的。

# _**AQS概要**_

AQS通过内部类Node记录当前是哪个线程持有锁，Node中有一个前驱节点和一个后继节点，形成一个双向链表，这个链表是一种CLH队列，其中waitStatus表示当前线程的状态，其可能的取值包括以下几种：

-   SIGNAL(-1)，表示后继线程已经或者即将被阻塞，当前线程释放锁或者获取锁失败后需要唤醒后继线程；
-   CANCELLED(1)，表示当前线程因为超时或者中断被取消，这个状态不可以被修改；
-   CONDITION(-2)，当前线程为条件等待，其状态设置0之后才能去竞争锁；
-   PROPAGATE(-3)，表示共享锁释放之后需要传递给后继节点，只有头结点的才会有该状态；
-   0，该状态为初始值，不属于上面任意一种状态。

Node对象中还有一个nextWaiter变量，指向下一个条件等待节点，相当于在CLH队列的基础上维护了一个简单的单链表来关联条件等待的节点。

```java
	static final class Node {

        static final Node SHARED = new Node();

        static final Node EXCLUSIVE = null;

        static final int CANCELLED =  1;

        static final int SIGNAL    = -1;

        static final int CONDITION = -2;
  
        static final int PROPAGATE = -3;

        volatile int waitStatus;

        volatile Node prev;

        volatile Node next;

        volatile Thread thread;

        Node nextWaiter;

        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }
        ...
        构造方法
        ...
        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
```

## enq和addWaiter

Node提供了两种入队列的方法，即enq和addWaiter，enq方法如下所示，当尾节点tail为null时，表明阻塞队列还没有被初始化，通过CAS操作来设置头结点，头结点为new Node()，实际上头结点中没有阻塞的线程，算得上是一个空的节点（注意空节点和null是不一样的），然后进行tail=head操作，这也说明当head=tail的时候，队列中实际上是不存在阻塞线程的，然后将需要入队列的node放入队列尾部，将tail指向node。

```ini
    private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
        	//如果tail为空，说明CLH队列没有被初始化，
            if (t == null) {
            	//初始化CLH队列，将head和tail指向一个new Node()，
            	//此时虽然CLH有一个节点，但是并没有真正意义的阻塞线程
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
            	//将node放入队列尾部，并通过cas将tail指向node
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
```

addWaiter通常表示添加一个条件等待的节点入队列，该方法首先尝试通过CAS操作快速入队列，如果失败则通过调用enq来入队列。

```ini
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        //尝试快速入队列
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        //快速入队列失败则采用enq方入队列
        enq(node);
        return node;
    }


```

## unparkSuccessor

AQS 还提供了唤醒后继节点线程的功能，主要是通过LockSupport来实现的，源码如下所示，

`unparkSuccessor`：如果 node.next 为空 null 或者 node.next 的状态 waitStatus 大于 0（Node.CANCELED）, 
   unparkSuccessor 会尝试找到（从tail往前找）后继节点中，没有被取消（状态大于0，即状态不为Node.CANCELED）的第一个非空节点 s。
   找到了就调用 LockSupport.unpark(s.thread) 唤醒 s 持有的线程


```java
    private void unparkSuccessor(Node node) {
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

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

## _**排他获取锁**_

### acquire

不支持中断的获取锁\\color{green}{不支持中断的获取锁}不支持中断的获取锁

```scss
	//不可中断的获取锁
    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            //对中断做补偿，中断当前线程
            selfInterrupt();
    }
```

acquire方法首先会调用tryAcquire方法尝试获取锁，如果获取锁失败,首先通过addWaiter将当前线程放入CLH队列中，然后通过acquireQueued方法获取锁，acquireQueued方法源码如下所示：

###  acquireQueued

当前线程为CLH中的第一个阻塞线程才会尝试去获取锁。

```ini
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

acquireQueued方法是无中断的获取锁，该方法有一个布尔类型的返回值，该值不是表示是否成功获取锁，而是标示当前线程的中断状态，因为acquireQueued方法是无法响应中断的，需要对中断进行补偿，这个补偿体现在acquire方法中。

```arduino
    //模板方法tryAcquire需要子类进行具体实现
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }
```

### acquireInterruptibly

支持中断的获取锁\\color{green}{支持中断的获取锁}支持中断的获取锁

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

acquireInterruptibly方法获取锁的过程中能够响应中断，主要体现在获取锁之前会判断一下当前线程的中断中断状态，若中断则抛出InterruptedException，然后通过tryAcquire获取锁，获取成功直接返回，获取失败则通过doAcquireInterruptibly获取锁，该方法和acquireQueued最大的区别就是在判断parkAndCheckInterrupt后，acquireQueued仅仅记录中断状态，parkAndCheckInterrupt则会抛出异常。

### doAcquireInterruptibly

```ini
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

### tryAcquireNanos

支持超时时间的获取锁\\color{green}{支持超时时间的获取锁}支持超时时间的获取锁

支持超时时间的获取锁功能

```java
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        //响应中断
        if (Thread.interrupted())
            throw new InterruptedException();
        //首先通过tryAcquire快速获取锁，若失败则调用doAcquireNanos方法
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }
```

从方法tryAcquireNanos的源码可以看出，该方法也是响应中断的，该方法首先调用模板方法tryAcquire快速的获取锁，如果失败则通过doAcquireNanos获取锁，doAcquireNanos中支持超时机制，其源码如下所示：

### doAcquireNanos

```ini
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                //判断如果超时则直接返回false，代表获取锁失败
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

doAcquireNanos方法与acquireQueued方法的区别是每次循环获取锁过程中都会计算deadline和当前时间的差值，如果这个差值小于0，则表示获取锁的操作已经超时，则直接返回false表示获取锁失败。

## _**共享锁获取**_

### acquireShared、acquireSharedInterruptibly和tryAcquireSharedNanos

AQS中不仅支持排他锁的获取，即acquire、acquireInterruptibly和tryAcquireNanos，还提供了共享锁的获取操作方法，包括acquireShared、acquireSharedInterruptibly和tryAcquireSharedNanos，这三个方法源码如下所示：

```java
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

```

共享锁的获取和排他锁的获取方法类似，共享锁调用了不同的模板方法tryAcquireShared，这里介绍一下doAcquireShared方法，其他方法变化的套路和共享锁的使用套路一样，doAcquireShared方法源码如下所示：

只有队列中的第一个阻塞线程才能获取锁。	

```ini
    private void doAcquireShared(int arg) {
    	//当前线程入队列
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            //自旋式的获取锁
            for (;;) {
                final Node p = node.predecessor();
                //只有队列中的第一个阻塞线程才能获取锁	
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        //获取锁成功，补偿中断
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                //通过interrupted记录中断信息
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

doAcquireShared方法没有返回值，与acquireQueued不同的是：

-   doAcquireShared没有返回值，该方法的中断补偿是在方法内完成的，获取锁成功之后，会判断中断信息interrupted的状态，如果为true则调用selfInterrupt()方法中断当前线程；
-   获取锁成功之后不是简单的设置head，而是通过setHeadAndPropagate方法来设置头结点和并且判断后继节点的信息，对后继节点中的线程进行唤醒操作等，setHeadAndPropagate方法源码如下所示：

```ini
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; 
        //设置新的头结点
        setHead(node);
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            //如果后继节点为空或者为SHARED类型的节点,执行doReleaseShared方法
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    private void doReleaseShared() {
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                	//状态为SIGNAL，则唤醒后继节点中的线程
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            
                    unparkSuccessor(h);
                }
                //若状态为0，则设置状态为PROPAGATE
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                
            }
            if (h == head)                  
                break;
        }
    }
```

## _**锁的释放**_

锁的释放也分为释放排他锁和释放共享锁，分别为release方法和releaseShared方法，源码如下所示，

```java
	//释放排他锁
    public final boolean release(int arg) {
    	//释放锁，然后唤醒后继节点的线程
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }


    //释放共享锁
    public final boolean releaseShared(int arg) {
    	//释放锁，然后调用doReleaseShared方法
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }
```

release方法和releaseShared方法分别调用模板方法tryRelease和tryReleaseShared来释放锁，release方法中直接通过调用unparkSuccessor唤醒后继线程，而releaseShared的唤醒操作在doReleaseShared方法中进行。



## _**取消获取锁**_

头部唤醒，尾部重设，中间剔除。

当获取锁失败时，需要进行一些状态清理和变化，cancelAcquire方法就是用来实现这些功能的，其源码如下所示，

```ini
    private void cancelAcquire(Node node) {
       
        if (node == null)
            return;
        //节点线程置为null
        node.thread = null;

        //从CLH队列中清除已经取消的节点（CANCELLED）
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        Node predNext = pred.next;

        node.waitStatus = Node.CANCELLED;

        //判断如果node是尾部节点，则设置尾部节点
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            int ws;
           	//若不是头节点则直接从CLH队列中清除当前节点
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            //若为头结点，则唤醒后继节点中的线程
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }

```

取消获取锁的操作首先将队列中处于CANCELLED状态的节点剔除，然后根据当前节点在CLH队列中的位置进行不同的操作：

-   node在队列尾部，则重新设置CLH队列的尾部节点；
-   node为头结点，唤醒后继节点中的线程；
-   node既不是头结点也不是尾节点，则在CLH中剔除node。

_**总结**_

AQS是整个concurrent包的基础，可重入锁、线程池、信号量（Semaphore）等同步工具类都需要借助AQS来完成，了解AQS是深入学习concurrent包的前提。