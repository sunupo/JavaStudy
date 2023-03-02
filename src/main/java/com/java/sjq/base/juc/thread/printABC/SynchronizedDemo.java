package com.java.sjq.base.juc.thread.printABC;


/**
 * 好像不太对
 */
public class SynchronizedDemo {

    static String[] locks = {"locka","lockb","lockc","lockd"};// 每个线程对应一个锁标记。
//	static String state="A";

    static class MyThread implements Runnable{
        String[] strings={"A","B","c","D"};
        String str;
        int[] idx = new int[2];
        int cnt=3;
        public MyThread(int i) {  //如果strings=[ABCD]。i=0，idx=[3,0]; i=1,idx=[0,1];i=2, idx=[1,2],i=3,idx=[2,3]
            this.str = strings[i];
            idx[0]=(i+strings.length-1)%strings.length;
            idx[1]=i;
            // TODO Auto-generated constructor stub
        }
        synchronized void decrease(){
            cnt--;
        }
        @Override
        public void run() {
            while(cnt>0){
                decrease();
                synchronized (locks[idx[0]]) {  //locks[idx[0]]代表当前线程的前一个线程的锁标记
                    synchronized (locks[idx[1]]) { // locks[idx[1]] 代表当前线程的锁标记
                        System.out.println(str);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        locks[idx[1]].notifyAll();
                    }
                    try {
                        if (cnt>0)
                            locks[idx[0]].wait();  //假如当前线程是A，locks[idx[0]]="lockc",只有C线程会调用"lockc".notify
                        else
                            locks[idx[0]].notifyAll();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread ta = new Thread(new MyThread(0));
        Thread tb = new Thread(new MyThread(1));
        Thread tc = new Thread(new MyThread(2));
        Thread td = new Thread(new MyThread(3));
//		todo 必须保证t tb tc td依次被cpu调度依次过后，才能输出ABCDABCDABCD……
        try {
            ta.start();
            Thread.sleep(500);
            tb.start();
            Thread.sleep(500);
            tc.start();
            Thread.sleep(500);
            td.start();

        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
