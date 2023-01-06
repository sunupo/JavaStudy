[Unsafe 类](https://www.cnblogs.com/upyang/p/12309513.html)
# 主要接口

（1）public native int getInt(Object o, long offset);　　//获得给定对象偏移量上的int值

（2）public native void putInt(Object o, long offset, int x);　　//设置给定对象偏移量上的int值

（3）public native long objectFieldOffset(Field f);　　//获得字段在对象中的偏移量

（4）public native void putIntVolatile(Object o, long offset);　　//设置给定对象的int值，使用volatile语义（其它线程马上能看到我的改动）

（5）public native int getIntVolatile(Object o, long offset);　　//获得给定对象的int值，使用volatile语义（其它线程马上能看到我的改动）

（6）public native void putOrderedInt(Object o, long offset, int x);　　//和putIntVolatile()一样，但是它要求被操作字段就是volatile类型的


[Unsafe.putOrderedInt源码学习](https://blog.csdn.net/u010597819/article/details/113922471)
todo: 学习CyclicBarrier 