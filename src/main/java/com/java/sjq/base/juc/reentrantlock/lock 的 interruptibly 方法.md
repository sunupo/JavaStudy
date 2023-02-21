[ Lock的lockInterruptibly()方法\_az44yao的博客-CSDN博客\_lockinterruptibly](https://blog.csdn.net/az44yao/article/details/105134306)

lockInterruptibly()方法能够中断等待获取锁的线程。当两个线程同时通过lock.lockInterruptibly()获取某个锁时，假若此时线程A获取到了锁，而线程B只有等待，那么对线程B调用threadB.interrupt()方法能够中断线程B的等待过程。

lockInterruptibly() 中这一段代码
```java
 public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }
```

```java
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }
```
如果不被中断，那么线程将会 tryAcquire。所以后文直接调用 lock.unlock是没问题的。