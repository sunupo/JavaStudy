package com.java.sjq.base.juc.lock.spinlock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

class SpinLockTest {
    /**
     * 持有锁的线程，null表示锁未被线程持有
     */
    private AtomicReference<Thread> ref = new AtomicReference<>();
    public void lock(){
        Thread currentThread = Thread.currentThread();
        while(!ref.compareAndSet(null, currentThread)){
            //当ref为null的时候compareAndSet返回true，反之为false
            //通过循环不断的自旋判断锁是否被其他线程持有
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
public class SpinLockTestTest {
    static int count = 0;

    @Test
    public void spinLockTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(100);
        SpinLockTest spinLockTest2 = new SpinLockTest();
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    spinLockTest2.lock();
                    ++count;
                    spinLockTest2.unLock();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println (count);
    }
}
