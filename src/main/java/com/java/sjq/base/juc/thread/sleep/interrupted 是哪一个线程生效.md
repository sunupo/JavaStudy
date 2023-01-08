https://blog.csdn.net/qq_39682377/article/details/81449451

interrupted（）方法测试的是当前线程是否被中断，当前线程！！！当前线程！！！这里当前线程是main线程，

而thread.interrupt(）中断的是thread线程，这里的此线程就是thread线程。所以当前线程main从未被中断过，

尽管interrupted（）方法是以thread.interrupted（）的形式被调用，但它检测的仍然是main线程而不是检测thread线程，所以thread.interrupted（）在这里相当于main.interrupted（）。

————————————————
版权声明：本文为CSDN博主「LZing_」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_39682377/article/details/81449451