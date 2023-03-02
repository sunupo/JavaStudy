package com.java.sjq.base.juc.thread.printABC;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockCondition {
    private static Lock lock = new ReentrantLock();
    private static Condition A = lock.newCondition();
    private static Condition B = lock.newCondition();
    private static Condition C = lock.newCondition();
    private static int state = 0;
    public static void changeState(){
        state++;
        state%=3;
    }
    static class ThreadA extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                System.out.println("1");
                for (int i = 0; i < 10; i++) {
//              TODO  	为什么用while不用if：多线程并发，不能用if，必须用循环测试等待条件，避免虚假唤醒
                    while (state != 0)//注意这里是不等于0，也就是说在state % 3为0之前，当前线程一直阻塞状态
                        A.await(); // A释放lock锁
                    System.out.print("A");
                    changeState();
                    B.signal(); // A执行完唤醒B线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    static class ThreadB extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                System.out.println("2");

                for (int i = 0; i < 10; i++) {
                    while (state != 1)
                        B.await();// B释放lock锁，当前面A线程执行后会通过B.signal()唤醒该线程
                    System.out.print("B");
                    changeState();
                    C.signal();// B执行完唤醒C线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    static class ThreadC extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                System.out.println("3");

                for (int i = 0; i < 10; i++) {
                    while (state != 2)
                        C.await();// C释放lock锁
                    System.out.print("C");
                    changeState();
                    A.signal();// C执行完唤醒A线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        new ThreadB().start();
        new ThreadC().start();

        new ThreadA().start();


    }
}
