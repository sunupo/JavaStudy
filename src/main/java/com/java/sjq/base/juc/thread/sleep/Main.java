package com.java.sjq.base.juc.thread.sleep;

import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicStampedReference;


/**
 * 情况1：先睡眠后打断，则直接打断睡眠，并且清除停止状态值，使之变成false：
 * 情况2：先打断后睡眠，则直接不睡眠：
 */

public class Main {
    public static void main(String[] args) throws InterruptedException {
        interruptAfterSleep();
        Thread.sleep(1000);
//        sleepAfterInterrupted();
    }

    /**
     * 1.先睡眠后打断，则直接打断睡眠，并且清除停止状态值，使之变成false：
     */
    public static void interruptAfterSleep(){
        try{
            MyThread thread=new MyThread();
            thread.start();
            Thread.sleep(10);
            thread.interrupt();

//
        }catch (InterruptedException e){
            System.out.println("main catch");
            e.printStackTrace();
        }
        System.out.println("Main end!");
    }

    /**
     *2. 先打断后睡眠，则直接不睡眠：
     * 运行到sleep接着立马抛出异常
     */
    public static void sleepAfterInterrupted(){
        try{
            MyThread2 thread=new MyThread2();
            thread.start();
            Thread.sleep(10);  // 确保thread启动了
            thread.interrupt();

//
        }catch (InterruptedException e){
            System.out.println("main catch");
            e.printStackTrace();
        }
        System.out.println("Main end!");
    }

}

class MyThread extends Thread{
    /**
     * 情况 1：先睡眠后打断，则直接打断睡眠，并且清除停止状态值，使之变成false：
     */
    @Override
    public void run() {
        super.run();
        try{
            System.out.println("run begin");
            Thread.sleep(200000);
            System.out.println("run end");
        }catch (InterruptedException e){
            System.out.println("在沉睡中被停止！进入catch！"+this.isInterrupted());
            e.printStackTrace();
        }
    }
}

class MyThread2 extends Thread{
    /**
     * ②先打断后睡眠，则直接不睡眠：
     * 解释：
     * MyThread 若在循环中被 主线程 打断，
     * Mythread 循环结束运行到自己线程中的Thread.sleep(2000);
     * 将不会进入2000ms的睡眠，将会直接抛出InterruptedException
     */
    @Override
    public void run() {
        super.run();
        try{
            System.out.println("MyThread run begin");
            for(int i=0;i<5000;i++){
                System.out.println(i);
                if(isInterrupted()){
                    System.out.println("Mythread interrupted");
                    break;
                }
            }
            System.out.println("MyThread run 循环结束");

            System.out.println("MyThread run end before");
            Thread.sleep(2000);  // 在sleep之前已经被打断，于是不会睡眠，直接抛出InterruptedException
            System.out.println("MyThread run end after");  //
        }catch (InterruptedException e){
            System.out.println("在沉睡中被停止！进入catch！"+this.isInterrupted());
            e.printStackTrace();
        }
    }
}
