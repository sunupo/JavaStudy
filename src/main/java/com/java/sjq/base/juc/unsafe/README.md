[Unsafe 类方法介绍
](https://www.imooc.com/wiki/concurrencylesson/unsafe.html    )

[使用sun.misc.Unsafe绕过new机制来创建Java对象](https://blog.csdn.net/ITer_ZC/article/details/40820919)

Unsafe.allocateInstance()方法值做了第一步和第二步，即分配内存空间，返回内存地址，没有做第三步调用构造函数。所以Unsafe.allocateInstance()方法创建的对象都是只有初始值，没有默认值也没有构造函数设置的值，因为它完全没有使用new机制，直接操作内存创建了对象。
