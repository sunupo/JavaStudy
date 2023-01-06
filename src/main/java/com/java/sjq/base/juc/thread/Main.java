package com.java.sjq.base.juc.thread;

public class Main {
     public static void main(String[] args){
      //
        String a="a";
        String b="b";
        String c=a+b;
        System.out.println(c);
        System.out.print(c==a+b);
        new Main().getNameSun();

    }
    synchronized public void getNameSun(){
         char  t='t';
         short s=1;
         int x = 0;
         long y=10L;
         float f = 100f;
         double d = 998d;

         System.out.println(d+f);
    }
}


class Thread1 extends Thread{
    private long i;

    public void setI(long i) {
        this.i = i;
    }

    public long getI() {
        return i;
    }

    @Override
    public void run() {
        super.run();
        while (true){
            try {
                if(isInterrupted()){
                    throw new InterruptedException("被中断");
                }
                Thread.sleep(0); //作用就是“触发操作系统立刻重新进行一次CPU竞争”。
            } catch (InterruptedException e) {
                
                throw new RuntimeException(e);
            }
        }
    }
}