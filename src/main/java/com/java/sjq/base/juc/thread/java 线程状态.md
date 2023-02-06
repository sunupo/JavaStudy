https://www.cnblogs.com/yangwen0228/p/7128683.html
java 代码定义了`[六种状态](Java SE 9 Enum Thread.State)`。加入对象锁部分的状态：`等待队列状态`，`锁池状态`。
![img.png](img.png)

图片中的阻塞状态包括BLOCKED、WAITING 和 TIME_WAITING 状态。

Blocked 状态。等待监视器锁定的线程的线程状态。处于阻塞状态的线程正在等待监视器锁进入同步块/方法，或者在调用 Object.wait后重新输入同步块/方法。

WAITING 状态。等待线程的线程状态。由于调用以下方法之一，线程处于等待状态：
- Object.wait 无超时
- Thread.join 无超时
- LockSupport.park

TIMED_WAITING 状态。具有指定等待时间的等待线程的线程状态。线程处于定时等待状态，原因是使用指定的正等待时间调用以下方法之一：
- Thread.sleep
- Object.wait 带超时
- Thread.join 带超时
- LockSupport.parkNanos
- LockSupport.parkUntil