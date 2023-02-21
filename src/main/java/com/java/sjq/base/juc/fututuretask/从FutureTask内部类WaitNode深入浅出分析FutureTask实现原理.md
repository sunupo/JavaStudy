[从FutureTask内部类WaitNode深入浅出分析FutureTask实现原理](https://blog.csdn.net/weixin_34320159/article/details/92101748)

```java
/**
* Awaits completion or aborts on interrupt or timeout.
*
* [@param](https://my.oschina.net/u/2303379) timed true if use timed waits
* [@param](https://my.oschina.net/u/2303379) nanos time to wait, if timed
* [@return](https://my.oschina.net/u/556800) state upon completion
*/
private int awaitDone(boolean timed, long nanos)
    throws InterruptedException {
    final long deadline = timed ? System.nanoTime() + nanos : 0L;
    WaitNode q = null;
    boolean queued = false;
   //自旋
    for (;;) {
        //调用get的线程被中断，将其对应waiterNode移除，同时抛出异常，interrupted会重置中断状态
        if (Thread.interrupted()) {
            removeWaiter(q);
            throw new InterruptedException();
        }
        int s = state;
        //FutureTask中run方法执行，即callable任务执行完，返回执行的状态
        if (s > COMPLETING) {
            if (q != null)
                q.thread = null;
            return s;
        }
        //FutureTask执行处于中间状态，获取结果的线程将cup执行机会让给真正要执行FutureTask类run方法的线程
        else if (s == COMPLETING) // cannot time out yet
            Thread.yield();
        else if (q == null)
        //将要获取结果的线程封装成对应的WaitNode，用于后面构建阻塞waiters栈
            q = new WaitNode();
        else if (!queued)
            //构建阻塞waiters栈
            queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                 q.next = waiters, q);
        else if (timed) {
            nanos = deadline - System.nanoTime();
            if (nanos <= 0L) {
                removeWaiter(q);
                return state;
            }
            LockSupport.parkNanos(this, nanos);
        }
        else
            //阻塞当前获取FutureTask类执行结果的线程
            LockSupport.park(this);
    }
}
```

[FutureTask.get(timeOut)执行原理浅析](https://blog.csdn.net/cuiwjava/article/details/108186030)

在分析实现前，我们先想下如果让我们实现一个类似FutureTask的功能，我们会如何做？因为需要获取执行结果，需要一个Object对象来存执行结果。任务执行时间不可控性，我们需要一个变量表示执行状态。其他线程会调用get方法获取结果，在没达到超时的时候需要将线程阻塞或挂起。

因此需要一个队列类似的结构存储等待该结果的线程信息，这样在任务执行线程完成后就可以唤醒这些阻塞或挂起的线程，得到结果。