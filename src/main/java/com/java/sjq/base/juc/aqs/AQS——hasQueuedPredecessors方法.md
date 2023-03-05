[(122条消息) AQS——hasQueuedPredecessors方法\_Aristocrat l的博客-CSDN博客](https://blog.csdn.net/qq_57205114/article/details/124083129)

# hasQueuedPredecessors方法的名称是：是否拥有前一个队列元素。换言之：用不用排队。返回false：不用排队。返回true：乖乖排队去。

### **h != t返回true，(s = h.next) == null返回false以及s.thread !Thread.currentThread()返回false**

返回false的情况就很简单了，总共有两种

# 返回false：

### 第一种：h != t为false

        这种情况就是队列为空或者队列只有一个元素的情况。

### 队列为空很好理解，但是什么时候队列只有一个元素呢？

        当线程一在执行时，线程二创建了队列，当线程一执行完后唤醒acquireQueue中的线程二，线程二CAS获取了锁，将自己设置为第一个节点并置空，并将head和tail指针都指向自己，此时队列中只有一个元素。
    
        第一种情况就是排队买票，刚过去发现窗口没人排队，直接去排不入队。

### 第二种：**h != t返回true，但是(s = h.next) == null返回false并且s.thread != Thread.currentThread()也返回false**

        前者返回true意思就是队列长度大于一（绝大多数情况下是这样，但是还有及其少数情况，下文做解释）。不过前者返回什么无所谓只要后者返回false即可。后者返回false，意味着头结点的下一个节点不为空（弥补了前者的极少数情况），也就是队列长度大于1。后面一句也就是当前执行的线程就是头部空节点下一个节点的线程。
    
        第二种情况就是排队买票，正好排到了。

# 返回true：

## 情况一： h != t返回true，(s = h.next) == null返回true

这种情况有两种造成的可能：

### 第一种：

        **线程一**正在执行操作拥有锁，而线程二由于没有获取到同步状态（state=1）导致创建队列，在创建队列中执行到将头部指针指向空节点，但是还没执行到将尾部节点指向头结点时，线程三开始获取锁，进入tryAcquire操作时，又恰好线程一执行完了锁，并且将state置为0释放了锁，此时由于state状态为0了，后来的线程三就顺利进入判断语句内部，并且调用了hasQueuedPredecessors判断，出现了这种现象。

```java
     private Node enq(final Node node) {        // 一直for循环，直到插入Node成功为止        for (;;) {            //拿到AQS指向的尾节点            Node t = tail;            if (t == null) {                // CAS设置首节点，如果设置不成功，那么又会来到下一次循环进行设置，设置的过程CAS原子性                // 注意，这里不是目前线程所在节点，而是new了一个新的空节点                                                                //这里头部已经指向了空节点，而尾部任然为空节点，造成队列h != t，但是head.next = null的场景，也就是看起来大于一个节点实际上只有一个节点的假象                if (compareAndSetHead(new Node()))                    //设置成功，AQS指向的头部变成了新的空节点，开始下一次循环                    tail = head;                                                            } else {                //最后一次循环，AQS的尾部已经有尾部节点了，那么来到else这一边                //将尾部节点赋值给                node.prev = t;                // CAS设置尾结点                if (compareAndSetTail(t, node)) {                    t.next = node;                    return t;                }                //此后的尾节点有两种，一种的包含着线程的尾节点，还有一种是队中只有一个的空节点            }        }    }
```

        这里头部已经指向了空节点，而尾部任然为空节点，造成队列h != t，但是head.next = null的场景，也就是看起来大于一个节点实际上只有一个节点的假象。
    
        这里的理解是，线程一拿到锁在执行；线程二没拿到锁在入队；此时线程一执行完后释放锁，线程三尝试不入队拿锁，但是线程二已经在排队了，所以线程三必须入队，造成返回true的情况。

### 第二种：

        线程一拿到锁，线程二没有，于是创建队列，线程一释放锁，唤醒线程二，线程二拿到锁，将自己设置为队列头部，并且置空，此时h和t都指向线程二的节点，然后此时线程三来了，在没有拿到锁的情况下开始入队，于是执行addWaite[r方](https://so.csdn.net/so/search?q=r%E6%96%B9&spm=1001.2101.3001.7020)法，将线程三节点的prev设置为头结点，将t指向的节点从线程二的空节点指向线程三，此时头部节点还是线程二的空节点，但是t已经指向线程三。下一步就是将首部空节点的next设置为线程三节点，就在这时，线程二也执行完了，释放了锁，又恰好线程四来到tryAcquire，这时state为0无锁，线程四尝试不排队拿锁，此时满足了h != t，且head.next = null的情况，这时线程四就返回了true，表示我必须入队。

```java
private Node addWaiter(Node mode) {        Node node = new Node(Thread.currentThread(), mode);        // Try the fast path of enq; backup to full enq on failure        Node pred = tail;        if (pred != null) {            node.prev = pred;            //线程三没拿到锁，将尾部指向自己            if (compareAndSetTail(pred, node)) {                //此步还没有操作，此时队列h指向空节点，t指向尾节点，但是空节点的next还是null，这时候满足了h ！= t，并且h.next == null的情况。                pred.next = node;                return node;            }                                }        enq(node);        return node;    }
```

        情况一：相当于排队买票，一共站着一个人，当第一个人排队的时候第二个人来了，刚站在后面的时候，第一个人买完了，此时第三个人来了想不排队买票，结果被[AQS](https://so.csdn.net/so/search?q=AQS&spm=1001.2101.3001.7020)撵去排队了。
    
        情况二：两个人排队，第一个人走了，现在一个人排队，第二个人正在办的时候，第三个人来了，第三个人刚来第二个人办完了，此时第四个人想不排队买票，结果被AQS撵去排队了。
    
        两者都是尝试插队时，前一个人刚入队，只是前者创建队列的时候没有空节点，后者是在只有一个空节点的情况下排队，两者都是抗排队的情况。

## 情况二:h != t返回true，(s = h.next) == null返回false，s.thread !=Thread.currentThread()返回true。

        h != t返回true表示队列中至少有两个不同节点存在。(s = h.next) == null返回false表示首节点是有后继节点的，也就是队列的长度大于1。而s.thread != Thread.currentThread()返回true意味着头结点的下一个节点的线程不是当前执行的线程，所以那当前线程排队还没排到，必须入队。
    
        这种情况就是妥妥的不掐点想插队了，队伍排的好好的，第一个人后面的第二个人早就来了，这时第一个人走了，二号都排老半天了，本来就归他排，所以三号被AQS撵去排队了。
    
        情况二相比情况一不同点的想插队的线程来的时候前一个线程是刚排队（正在入队），还是早就排队了（已经入队）。