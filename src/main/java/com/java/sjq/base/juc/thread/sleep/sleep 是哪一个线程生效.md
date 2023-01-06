# sleep究竟是让哪一个线程休眠？

sleep方法只能让当前线程睡眠，调用某一个线程类的对象t.sleep()，睡眠的不是t，而是当前线程。
我们通过继承Thread类创建线程。在Runner1的run()中不写sleep()，在主线程中写Runner1.sleep(5000)，结果不是Runner1睡眠，还是主线程睡眠，请看下面输出结果

注意：sleep是个静态方法,直接使用Thread.sleep（）就可以调用。在主线程里面调用subThread.sleep(0)其实相当于Thread.sleep(0),所以哪个线程调用sleep，哪个线程就睡眠。