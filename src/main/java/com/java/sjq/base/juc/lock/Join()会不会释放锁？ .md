看过Join源码的都知道，其中的核心就是
```java
if (millis == 0) {  //由于上一步传入参数为0，因此调用当前判断
while (isAlive()) { //判断子线程是否存活
wait(0); //调用wait(0)方法
}
}
```


即，只要子线程还活着，就一直wait，阻塞当前主线程，直到被唤醒。
即join底层还是wait()，众所周知wait方法会释放锁，所以推测join也会释放锁，不过网上有很多join不释放的说法。

object.wait()和thread.join()
join()属于Thread类中，thread的对象锁，因为thread.join()这个join里面是this这个锁，也就是thread，
即在主线程中调用t.join()相当于t.wait()，我们去掉join这个方法，就相当于
```java
main(){
t.wait();
}
```

所以
```java

main(){
synchronized(obj){
thread.join(); //join不释放锁
}
}

main(){
synchronized(thread){
thread.join(); //join释放锁
}
}

```
一句话概括：
主线程（mian） 释放掉 子线程（thread.join中的thread）这把锁
————————————————
版权声明：本文为CSDN博主「LeesinDong」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/dataiyangu/article/details/104956755


[JAVA多线程：狂抓 join()方法到底会不会释放锁，给你彻底介绍清楚（三）](https://blog.csdn.net/succing/article/details/123023851)

# 1 先说结论

join底层调用的是wait()，而wait是Object的方法，wait本身是会释放锁（彻底交出CPU的执行权），所以 Thread 的join() 方法是否会释放锁？答案是会！

但是，join()只会释放Thread的锁，不会释放线程对象的锁（可能会造成死锁）。

# 2 关键点

所以针对于这个问题，要看join的外层，synchronized作用的对象，是object实体对象，还是thread！



# 3 示例

## 3.1 例子 
```java
public class ThreadJoinTestLock {
 
	public static void main(String[] args) {
		Object object = new Object();
		MThread mythread = new MThread("mythread ", object);
		mythread.start();
		//synchronized (mythread)
		synchronized (object) {
			for (int i = 0; i < 100; i++) {
				if (i == 20) {
					try {
						System.out.println("开始join");
						mythread.join();//main主线程让出CPU执行权，让mythread子线程优先执行
						System.out.println("结束join");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println(Thread.currentThread().getName() +"==" + i);
			}
		}
		System.out.println("main方法执行完毕");
	}
}
 
class MThread extends Thread {
	private String name;
	private Object obj;
	public MThread(String name, Object obj) {
		this.name = name;
		this.obj = obj;
	}
	@Override
	public void run() {
		synchronized (obj) {
			for (int i = 0; i < 100; i++) {
				System.out.println(name + i);
			}
		}
	}
}
```

## 3.1.1 结果(死锁)

可以看到，在join之前，一直是主线程在执行，main方法中的for循环到20时，此时该join方法的作用是让main主线程阻塞，给被join的线程让出CPU的执行权，让子线程mythread先执行。

但是，结局很意外，main方法的object不释放锁，已经进入了阻塞状态，因为object没有释放锁，子线程又拿不到锁，所以就卡死了，其实是主线程阻塞，子线程得不到锁（CPU的运行机会）。

此时的主线程仿佛在对子线程说：你咬我呀，我就是占着茅坑（锁）不拉屎（运行），子线程说，你有本事把茅坑让给我呀，主线程说，我就是不让。两者相斥不下，就卡死了。

如果注释掉join相关代码：则可以看到主线程执行完毕（一口气把锁用完，然后交出锁），才会执行子线程。

## 3.2 例子

```java
// 运行前，需要确保上述代码中的main方法中的代码同步块是mythread

synchronized (mythread)
```

## 3.2.1 结果（正常运行）
