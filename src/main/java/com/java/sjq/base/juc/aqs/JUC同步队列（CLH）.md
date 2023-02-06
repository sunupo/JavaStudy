> [J.U.C|同步队列（CLH）](https://www.jianshu.com/p/6fc0601ffe34)
> 
> [Java中的AQS（二）同步状态的获取与释放](https://blog.csdn.net/yanghan1222/article/details/80248494)

# 1. releaseShared()
> [奈学：reaseShared共享式释放锁](https://zhuanlan.zhihu.com/p/188691999)
1. 调用`tryReleaseShared`尝试释放共享锁，这里必须实现为线程安全。
2. 如果释放了锁，那么调用`doReleaseShared`方法循环后继结点，实现唤醒的传播。
   doReleaseShared方法中，只要头部head不为空，并且头部head、尾部tail不是同一个节点，再并且头部head的登台状态waitStatus为Node.SIGNAL，那么就会释放后继节点 unparkSuccessor。
3. `unparkSuccessor`。如果head.next 为空null或者head.next 状态waitStatus大于0（Node.CANCELED）, unparkSuccessor 会尝试找到（从tail往前找）后继节点中，没有被取消（状态大于0，即状态不为Node.CANCELED）的第一个非空节点 s。找到了就调用 LockSupport.unpark(s.thread) 唤醒 s 持有的线程
   

todo 其他方法学习