[LockSupport.park() 阻塞住的线程如何唤醒](https://blog.csdn.net/mo_ing/article/details/120651364)
## 一：LockSupport.unpark()唤醒线程：
LockSupport.unpark()只会唤醒该线程一次。下次用LockSupport.park()再次阻塞住的时候，需要再次调用LockSupport.unpark()唤醒该线程。 
简言之，利用LockSupport.unpark()唤醒线程，该方法需要与LockSupport.park()方法成对出现， 每次park都有对应的unpark。 

## 二： 中断。
（不清除终端标志位，LockSupport.park()发现有中断标志就不会阻塞了） 
注释的那行有什么作用呢？ 
这是因为调用 t0.interrupt() 方法发出一个中断时，t0线程上面会一个中断状态（打上一个中断标记）。 
当发出的中断唤醒了 LockSupport.park() 方法阻塞住的线程时，park方法发现 t0线程有这个中断状态， 知道该线程是需要被中断处理的，则下一次调用 LockSupport.park() 则不会再被阻塞了。 
如果发出了中断后，唤醒了 LockSupport.park() 阻塞的线程，下一次遇到 LockSupport.park() 还想被阻塞住， 
就需要先调用 Thread.interrupted() 方法来清除中断状态，这样下一次遇到 LockSupport.park() 方法的时候，线程便会被阻塞住。