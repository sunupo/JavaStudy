package com.java.sjq.base.juc.thread.threadGroup;

public class TheadGroupDemo {
    public static void main(String[] args) {
        System.out.printf("main() name: %s %n", Thread.currentThread().getName());
        Thread t1 = new Thread();
        System.out.printf("t1 thread name: %s %n", t1.getName());
        System.out.printf("t1 thread group: %s %n", t1.getThreadGroup().getName());

        ThreadGroup tg = new ThreadGroup("thread group"){
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.printf("thread name: %s, error message %s; %n", t.getName(), e.getMessage());
            }
        };
        tg.setMaxPriority(5);
        Thread t = new Thread(tg, ()->{
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(100);
            }
        },"thread 1");
        t.setPriority(9);
        t.start();

        System.out.printf("thread group priority: %d %n", tg.getMaxPriority());
        System.out.printf("thread 1 priority: %d %n", t.getPriority());


        System.out.println("tg.activeCount"+tg.activeCount());
        Thread[] threads = new Thread[tg.activeCount()];
        ThreadGroup threadGroup = new ThreadGroup("new ");
        threadGroup.enumerate(threads);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t.interrupt();
    }
}
