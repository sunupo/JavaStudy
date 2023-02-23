package com.java.sjq.base.juc.thread.threadlocal;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadLocalDemo {
    public static void main(String[] args){
      //
        ThreadLocal<Integer> tl = ThreadLocal.withInitial(()->100);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                2,
                4,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "MyThread--"+r);
                    }
                },
                new ThreadPoolExecutor.AbortPolicy());

        Runnable r1 = ()->{
            tl.set(tl.get()+10);
            System.out.println("r1 tl.get()"+tl.get());
//            tl.remove(); // tag(a)
        };
        Runnable r2 = ()->{
            System.out.println("r2 tl.get()"+tl.get());
        };

        executor.execute(r1);


        for(int i = 0; i < 7; i++) {
            executor.execute(r2);
        }

        /**
         *
         * 以下结果可以看到线程池复用导致 threadLocal 变量重用了。
         *
         * 可能的输出1
         * r2 tl.get()100
         * r1 tl.get()110
         * r2 tl.get()100
         * r2 tl.get()110
         * r2 tl.get()110
         * r2 tl.get()100
         * r2 tl.get()110
         * r2 tl.get()100
         *
         * 可能的输出2
         * r1 tl.get()110
         * r2 tl.get()100
         * r2 tl.get()110
         * r2 tl.get()100
         * r2 tl.get()100
         * r2 tl.get()110
         * r2 tl.get()100
         * r2 tl.get()110
         *
         * 总结：避免线程服用导致 threadLocal变量复用，所以需要添加 tl.remove()
         */


    }
}
