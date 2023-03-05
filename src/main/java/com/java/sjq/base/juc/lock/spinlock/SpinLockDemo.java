package com.java.sjq.base.juc.lock.spinlock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

class SpinLock {
    /**
     * 持有锁的线程，null表示锁未被线程持有
     */
    private AtomicReference<Thread> ref = new AtomicReference<>();
    public void lock(){
        Thread currentThread = Thread.currentThread();
        int attemptCount=1;
        while(!ref.compareAndSet(null, currentThread)){
            //当ref为null的时候compareAndSet返回true，反之为false
            //通过循环不断的自旋判断锁是否被其他线程持有
            System.out.println(currentThread.getId()+":\t"+attemptCount);
            ++attemptCount;
        }
    }
    public void unLock() {
        Thread cur = Thread.currentThread();
        if(ref.get() != cur){
            //exception ...
        }
        ref.set(null);
    }
}


//自旋锁测试
public class SpinLockDemo {
    static int count = 0;

    @Test
    public void spinLockTest() throws InterruptedException {
        int num = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(num);
        CountDownLatch countDownLatch = new CountDownLatch(num);
        SpinLock spinLock = new SpinLock();
        for (int i = 0; i < num; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    spinLock.lock();
                    ++count;
                    try {
                        Thread.sleep(100*num);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    spinLock.unLock();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println (count);
    }
}
