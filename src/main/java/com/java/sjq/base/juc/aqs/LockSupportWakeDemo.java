package com.java.sjq.base.juc.aqs;

import org.slf4j.Logger;

import java.util.concurrent.locks.LockSupport;

import static org.apache.activemq.plugin.DiscardingDLQBrokerPlugin.log;

/**
 * [LockSupport.park() 阻塞住的线程如何唤醒](https://blog.csdn.net/mo_ing/article/details/120651364)
 *<br>
 * 一：LockSupport.unpark()唤醒线程：<br>
 * LockSupport.unpark()只会唤醒该线程一次。下次用LockSupport.park()再次阻塞住的时候，需要再次调用LockSupport.unpark()唤醒该线程。
 * 简言之，利用LockSupport.unpark()唤醒线程，该方法需要与LockSupport.park()方法成对出现，每次park都有对应的unpark。
 *<br><br>
 *
 * 二： 中断。（不清除终端标志位，LockSupport.park()发现有中断标志就不会阻塞了）
 * 注释的那行有什么作用呢？
 *<br>
 * 这是因为调用 t0.interrupt() 方法发出一个中断时，t0线程上面会一个中断状态（打上一个中断标记）。
 * 当发出的中断唤醒了 LockSupport.park() 方法阻塞住的线程时，park方法发现 t0线程有这个中断状态，
 * 知道该线程是需要被中断处理的，则下一次调用 LockSupport.park() 则不会再被阻塞了。
 *<br>
 * 如果发出了中断后，唤醒了 LockSupport.park() 阻塞的线程，下一次遇到 LockSupport.park() 还想被阻塞住，
 * 就需要先调用 Thread.interrupted() 方法来清除中断状态，这样下一次遇到 LockSupport.park() 方法的时候，线程便会被阻塞住。
 *
 */
public class LockSupportWakeDemo {
    public static void main(String[] args) {
//        byUnpark();
        byInterrupt();
    }

    /**
     *
     */
    private static void byUnpark() {
        Thread t0 = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread current = Thread.currentThread();
                log.info("{},开始执行!",current.getName());
                for(;;){
                    log.info("准备park住当前线程：{}....",current.getName());
                    LockSupport.park();   // 或者是LockSupport.park(this);
                     Thread.interrupted();
                    log.info("当前线程{}已经被唤醒....",current.getName());
                }
            }
        },"t0");

        t0.start();

        try {
            Thread.sleep(2000);
            log.info("准备唤醒{}线程!",t0.getName());
            LockSupport.unpark(t0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void byInterrupt(){
        Thread t0 = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread current = Thread.currentThread();
                log.info("{},开始执行!",current.getName());
                for(;;){
                    log.info("准备park住当前线程：{}....",current.getName());
                    LockSupport.park();   // 或者是LockSupport.park(this);
                    // Thread.interrupted();
                    log.info("当前线程{}已经被唤醒....",current.getName());
                }
            }
        },"t0");

        t0.start();

        try {
            Thread.sleep(2000);
            log.info("准备唤醒{}线程!",t0.getName());
            t0.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
