[(116条消息) 线程组概念\_l8y11的博客-CSDN博客](https://blog.csdn.net/weixin_44029159/article/details/126794317)

## 1\. 概述

线程组（ThreadGroup），其作用是对线程进行批量管理。

每个线程便然存在于一个线程组中，线程不能独立于线程组存在。

main() 方法的线程名为 main。  
需要注意的是，如果在创建新线程时没有显式设置线程组，那么默认将父线程的线程组设置为自己的线程组。

```
public class Strings {
    public static void main(String[] args) {
        System.out.printf("main() name: %s %n", Thread.currentThread().getName());
        Thread t1 = new Thread();
        System.out.printf("t1 thread name: %s %n", t1.getName());
        System.out.printf("t1 thread group: %s %n", t1.getThreadGroup().getName());
    }
}

// main() name: main 
// t1 thread name: Thread-0 
// t1 thread group: main 
```

线程组是向下引用的[树形结构](https://so.csdn.net/so/search?q=%E6%A0%91%E5%BD%A2%E7%BB%93%E6%9E%84&spm=1001.2101.3001.7020)，因为如果下级引用上级会导致 GC 无法回收上级线程。



## 2\. 线程优先级问题

在概述中，我们知道线程必然存在于一个线程组中。

那么当线程和线程组的优先级不一致时会导致什么样的结果呢？

```
public class Strings {
    public static void main(String[] args) {
        ThreadGroup tg = new ThreadGroup("thread group");
        tg.setMaxPriority(5);
        Thread t = new Thread(tg, "thread 1");
        t.setPriority(9);
        System.out.printf("thread group priority: %d %n", tg.getMaxPriority());
        System.out.printf("thread 1 priority: %d %n", t.getPriority());
    }
}

// thread group priority: 5 
// thread 1 priority: 5
```

所以，线程的优先级大于所在线程组的优先级，那么该线程的优先级将会失效，取而代之的是线程组的最大优先级。



## 3\. 常用操作

## 3.1 统一异常处理

```
public class Strings {
    public static void main(String[] args) {
        ThreadGroup tg = new ThreadGroup("thread group") {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.printf("thread name: %s, error message %s; %n", t.getName(), e.getMessage());
            }
        };

        Runnable task = () -> {
            throw new RuntimeException("thread runtime exception.");
        };
        Thread t1 = new Thread(tg, task, "thread 1");
        t1.start();
    }
}
// thread name: thread 1, error message thread runtime exception.; 
```



## 3.2 拷贝线程组

```
Thread[] threads = new Thread[threadGroup.activeCount()];
TheadGroup threadGroup = new ThreadGroup();
threadGroup.enumerate(threads);
```