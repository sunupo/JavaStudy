package com.java.sjq.base.juc.thread.interrupt;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.*;

public class InterruptAThread {


    public static void main(String[] args){
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "MyThread--"+r);
            return t;
        });
        //
        Runnable r1 = ()->{
            while(!Thread.currentThread().isInterrupted()){
                System.out.println("running……"+ Calendar.getInstance().get(Calendar.SECOND));
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){  // 异常会对中断标志复位，设为false
                    System.out.println("外部中断后的 中断标志为：\t"+Thread.currentThread().isInterrupted());
                    Thread.currentThread().interrupt(); // 主动再设置一下中断，或者return
                    System.out.println("自己中断后的 中断标志为：\t"+Thread.currentThread().isInterrupted());
                    return;
                }
            }
            System.out.println("r1  end ……");
        };

        Runnable r2 = ()->{
            System.out.println("r2 终止线程池");
            executorService.shutdownNow(); // 具体实现在 ThreadPoolExecutor.java
        };

        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(r1, 0, 2, TimeUnit.SECONDS);
        executorService.schedule(r2, 2, TimeUnit.SECONDS);
        try {
            System.out.println("get1");
            System.out.println(scheduledFuture.get(4, TimeUnit.SECONDS));
            System.out.println("get2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


//        executorService.execute(r1);

//        Future<String> ok = executorService.submit(r1, "ok");
//        try {
//            System.out.println(ok.get());
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
    }
}
