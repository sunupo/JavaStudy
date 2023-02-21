> [ThreadLocal弱引用与内存泄漏分析](https://zhuanlan.zhihu.com/p/91579723#:~:text=1.%E4%B8%BA%E4%BB%80%E4%B9%88ThreadLocalMap%E4%BD%BF%E7%94%A8%E5%BC%B1%E5%BC%95%E7%94%A8%E5%AD%98%E5%82%A8ThreadLocal%EF%BC%9F,%E5%81%87%E5%A6%82%E4%BD%BF%E7%94%A8%E5%BC%BA%E5%BC%95%E7%94%A8%EF%BC%8C%E5%BD%93ThreadLocal%E4%B8%8D%E5%86%8D%E4%BD%BF%E7%94%A8%E9%9C%80%E8%A6%81%E5%9B%9E%E6%94%B6%E6%97%B6%EF%BC%8C%E5%8F%91%E7%8E%B0%E6%9F%90%E4%B8%AA%E7%BA%BF%E7%A8%8B%E4%B8%ADThreadLocalMap%E5%AD%98%E5%9C%A8%E8%AF%A5ThreadLocal%E7%9A%84%E5%BC%BA%E5%BC%95%E7%94%A8%EF%BC%8C%E6%97%A0%E6%B3%95%E5%9B%9E%E6%94%B6%EF%BC%8C%E9%80%A0%E6%88%90%E5%86%85%E5%AD%98%E6%B3%84%E6%BC%8F%E3%80%82%20%E5%9B%A0%E6%AD%A4%EF%BC%8C%E4%BD%BF%E7%94%A8%E5%BC%B1%E5%BC%95%E7%94%A8%E5%8F%AF%E4%BB%A5%E9%98%B2%E6%AD%A2%E9%95%BF%E6%9C%9F%E5%AD%98%E5%9C%A8%E7%9A%84%E7%BA%BF%E7%A8%8B%EF%BC%88%E9%80%9A%E5%B8%B8%E4%BD%BF%E7%94%A8%E4%BA%86%E7%BA%BF%E7%A8%8B%E6%B1%A0%EF%BC%89%E5%AF%BC%E8%87%B4ThreadLocal%E6%97%A0%E6%B3%95%E5%9B%9E%E6%94%B6%E9%80%A0%E6%88%90%E5%86%85%E5%AD%98%E6%B3%84%E6%BC%8F%E3%80%82)

ThreadLocal中的弱引用

1.为什么ThreadLocalMap使用弱引用存储ThreadLocal？

假如使用强引用，当ThreadLocal不再使用需要回收时，发现某个线程中ThreadLocalMap存在该ThreadLocal的强引用，无法回收，造成内存泄漏。

因此，使用弱引用可以防止长期存在的线程（通常使用了线程池）导致ThreadLocal无法回收造成内存泄漏。

2.那通常说的ThreadLocal内存泄漏是如何引起的呢？

我们注意到Entry对象中，虽然Key(ThreadLocal)是通过弱引用引入的，但是value即变量值本身是通过强引用引入。

这就导致，假如不作任何处理，由于ThreadLocalMap和线程的生命周期是一致的，当线程资源长期不释放，即使ThreadLocal本身由于弱引用机制已经回收掉了，但value还是驻留在线程的ThreadLocalMap的Entry中。即存在key为null，但value却有值的无效Entry。导致内存泄漏。

但实际上，ThreadLocal内部已经为我们做了一定的防止内存泄漏的工作。

即如下方法：expungeStaleEntry

上述方法的作用是擦除某个下标的Entry（置为null，可以回收），同时检测整个Entry[]表中对key为null的Entry一并擦除，重新调整索引。

该方法，在每次调用ThreadLocal的get、set、remove方法时都会执行，即ThreadLocal内部已经帮我们做了对key为null的Entry的清理工作。

但是该工作是有触发条件的，需要调用相应方法，假如我们使用完之后不做任何处理是不会触发的。