package com.java.sjq.dataStructure.interview;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Main3 {
    public volatile int i=0;
    public volatile int j=0;
    public volatile int p=0;
    public static CyclicBarrier cb = new CyclicBarrier(1000);
    public static ReentrantLock lock = new ReentrantLock();
    public static void main(String[] args) throws InterruptedException {
        Main3 main3 = new Main3();
        main3.fun();

        Thread.sleep(1000);  // 等待1000个线程都结束
        System.out.println(main3.i);
        System.out.println(main3.j);
        System.out.println(main3.p);
    }
    public synchronized void funj(){
        j++;
    }

    public void funp(){
        lock.lock();
        try{
            p++;
        }catch (Exception e){

        }finally{
            lock.unlock();
        }
    }
    public void fun(){
        for(int j = 0; j < 10000; j++) {
          new Thread(()->{
              i++;
              funj();
              funp();
          }).start();
        }
    }

    public volatile AtomicInteger inc = new AtomicInteger(0);
    //public int inc = 0;

    public void increase() {
        inc.getAndIncrement();
    }
}

class SynchronizedTest {
    public volatile int race = 0;
    //使用synchronized保证++操作原子性
    public synchronized void increase() {
        race++;
    }
    public int getRace(){
        return race;
    }

    public static void main(String[] args) {
        //创建5个线程，同时对同一个volatileTest实例对象执行累加操作
        SynchronizedTest synchronizedTest=new SynchronizedTest();
        int threadCount = 1000;
        Thread[] threads = new Thread[threadCount];//5个线程
        for (int i = 0; i < threadCount; i++) {
            //每个线程都执行1000次++操作
            threads[i]  = new Thread(()->{
                for (int j = 0; j < 100; j++) {
                    synchronizedTest.increase();
                }
//                System.out.println(synchronizedTest.getRace());
            });
            threads[i].start();
        }

        //等待所有累加线程都结束
        for (int i = 0; i < threadCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //所有子线程结束后，race是：5*10000=50000。
        System.out.println("累加结果："+synchronizedTest.getRace());
    }
}