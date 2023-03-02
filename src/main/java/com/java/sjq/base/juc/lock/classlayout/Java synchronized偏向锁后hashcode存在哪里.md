[Java synchronized偏向锁后hashcode存在哪里？\_51CTO博客\_java锁synchronized原理](https://blog.51cto.com/u_15303040/5252003)

©著作权归作者所有：来自51CTO博客作者猪哥66的原创作品，请联系作者获取转载授权，否则将追究法律责任

今天的文章从下面这张图片开始，这张图片Java开发们应该很熟悉了![Java synchronized偏向锁后hashcode存在哪里？_java](Java%20synchronized%E5%81%8F%E5%90%91%E9%94%81%E5%90%8Ehashcode%E5%AD%98%E5%9C%A8%E5%93%AA%E9%87%8C.assets/resize,m_fixed,w_1184.webp "在这里插入图片描述")

我们都知道无锁状态是对象头是有位置存储hashcode的，而变为偏向锁状态是没有位置存储hashcode的，今天我们来通过实现验证这个问题：**当锁状态为偏向锁的时候，hashcode存到哪里去了？**

**先说结论：**

1.  jdk8偏向锁是默认开启，但是是有延时的，可通过参数：` -XX:BiasedLockingStartupDelay=0`关闭延时。
2.  hashcode是懒加载，在调用hashCode方法后才会保存在对象头中。
3.  当对象头中没有hashcode时，对象头锁的状态是 可偏向（ biasable，101，且无线程id）。
4.  如果在同步代码块之前调用hashCode方法，则对象头中会有hashcode，且锁状态是 不可偏向（0 01），这时候再执行同步代码块，锁直接是 轻量级锁（thin lock，00）。
5.  如果是在同步代码块中执行hashcode，则锁是从 偏向锁 直接膨胀为 重量级锁。

## 1、hashcode是啥时候存进对象头中？

根据下图我们可知，hashcode并不是对象实例化后就创建，而是在调用默认的hasCode方法时才会放进对象头。

![Java synchronized偏向锁后hashcode存在哪里？_同步代码块_02](Java%20synchronized%E5%81%8F%E5%90%91%E9%94%81%E5%90%8Ehashcode%E5%AD%98%E5%9C%A8%E5%93%AA%E9%87%8C.assets/resize,m_fixed,w_1184-1677765701419-1.webp "在这里插入图片描述")

第一次打印的对象头中我们发现对象头中mark word值为16进制的5，转为2进制就是101，且后面的状态显示为biasable，也就是可偏向，注意区分可偏向和已偏向：可偏向表示还么有synchronized锁，已偏向表示有线程访问锁。

第二次打印对象头中已经存在hashcode，value为0x00000039a054a501，转换为2进制为：11100110100000010101001010010100000001，最后三位也就是0 01，这就表示不可偏向，也就说当出现synchronized锁不会进行偏向，真是如此吗？我们验证一下！

## 2、存在hashcode后，出现synchronized会是什么锁？（thin）

根据下图我们可以清晰的看到，当已存在hashcode再执行同步代码，则会直接进入轻量级锁，原因还是上面的结论，有hashcode后将锁设置为 不可偏向，那肯定就直接上轻量级锁咯。

![Java synchronized偏向锁后hashcode存在哪里？_面试_03](Java%20synchronized%E5%81%8F%E5%90%91%E9%94%81%E5%90%8Ehashcode%E5%AD%98%E5%9C%A8%E5%93%AA%E9%87%8C.assets/resize,m_fixed,w_1184-1677765701419-2.webp "在这里插入图片描述")

## 3、如果锁状态是 已偏向，再计算hashcode会怎样？（fat）

前面两种情况锁状态都是 可偏向 状态，如果此时锁状态是 已经进入偏向状态呢？是会进行锁升级嘛？

根据下图我们可以看到，当hashCode方法处于synchronized代码块中时，**锁直接升级为重量级锁**。

![Java synchronized偏向锁后hashcode存在哪里？_懒加载_04](Java%20synchronized%E5%81%8F%E5%90%91%E9%94%81%E5%90%8Ehashcode%E5%AD%98%E5%9C%A8%E5%93%AA%E9%87%8C.assets/resize,m_fixed,w_1184-1677765701419-3.webp "在这里插入图片描述")

至于为什么直接升级为重量级锁而不是轻量级锁，这个原因不得而知。

猪哥猜想可能无线程竞争状态下，偏向锁升级为重量级锁消耗的资源比轻量级锁消耗的资源少。

同时欢迎知道原因的同学能够留言告知，也欢迎大家说出自己的猜想？没准以后会根据你的方案优化呢！

## 4、总结

1.  jdk8偏向锁是默认开启，但是是有延时的，可通过参数：` -XX:BiasedLockingStartupDelay=0`关闭延时。
2.  hashcode是懒加载，在调用hashCode方法后才会保存在对象头中。
3.  当对象头中没有hashcode时，对象头锁的状态是 可偏向（ biasable，101，且无线程id）。
4.  如果在同步代码块之前调用hashCode方法，则对象头中会有hashcode，且锁状态是 不可偏向（0 01），这时候再执行同步代码块，锁直接是 轻量级锁（thin lock，00）。
5.  如果是在同步代码块中执行hashcode，则锁是从 偏向锁 直接膨胀为 重量级锁。
