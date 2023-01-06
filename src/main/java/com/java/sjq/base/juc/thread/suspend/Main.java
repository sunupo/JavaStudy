package com.java.sjq.base.juc.thread.suspend;

// This class contains an integer array &
// Threads set the element's value for this array
class NumVal {
    private int num[] = null;
    boolean valueSet = false;
    int i = 0;
    NumVal()
    {
        // Creating integer array of 10 elements
        num = new int[10];
    }
    // method to set the values in the array
    public void setVal(int n)
    {
        if (i < 9) {
            System.out.println("Putting value " + n
                    + " in the NumVal Array");
            num[i] = n;
            i++;
        }
    }
    // method to get the values from the array
    public int getVal()
    {
        if (i >= 0) {

            System.out.println("Giving n = " + num[i]);
            i--;
            return num[i + 1];
        }
        else {
            return -1;
        }
    }
}
// Creating Our Thread Class
class MyThread extends Thread {

    // MyThread want mutually exclusive
    // lock on the object
    // referred by: NumObjToSetVal
    NumVal NumObjToSetVal = null;

    // Constructor
    public MyThread(String threadName, NumVal numV)
    {
        super(threadName);
        NumObjToSetVal = numV;
    }

    public void run()
    {
        // Only 1 thread at a time an access the object
        // referred by : NumObjToSetVal
        synchronized (NumObjToSetVal)
        {
            int n = 0;
            while (n < 5) {
                System.out.println(
                        "THREAD NAME : "
                                + Thread.currentThread().getName());
                n++;
                NumObjToSetVal.setVal(n);
                try {
                    // Make the thread sleep for 100 ms
                    Thread.sleep(100);
                    System.out.println(
                            Thread.currentThread().getName()
                                    + "is awake now");
                }
                catch (Exception e) {
                    System.out.println("Exception Caught");
                }
                // If n is 2 , we suspend this thread
                if (n == 2) {
                    // suspend the thread, now this thread
                    // will release lock on NumObjToSetVal
                    // only when resume() method is called
                    // on this thread, thread will go in
                    // waiting state
                    Thread.currentThread().suspend();
                }
            }
        }
    }
}

public class Main {
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        NumVal v = new NumVal();

        // Creating thread 1 that want exclusive lock on
        // object referred by v
        MyThread thread1 = new MyThread("Thread1 ", v);

        // Creating thread 2 that want exclusive lock on
        // object referred by v
        // thread1 is not going to release lock on Object
        // referred by v until resume() method is not called
        // and for acquiring lock on v Object refred by v ,
        // thread1 must have released lock on Object
        // referred by v, if lock is not released, thread2
        // will keep on waiting for thread1 to release lock
        // onbject referred by v & deadlock will be formed
        MyThread thread2 = new MyThread("Thread2 ", v);

        // starting both threads
        thread1.start();
        thread2.start();
        /** 把线程从suspend状态揪出来
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread1.resume();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread2.resume();
         */

        for (int i = 500; i <= 501; i++) {
            System.out.println("Main Thread " + i);
        }
    }
}
// Why Thread.stop(), Thread.suspend(), and Thread.resume() Methods are Deprecated After JDK 1.1 Version?
// https://www.geeksforgeeks.org/why-thread-stop-thread-suspend-and-thread-resume-methods-are-deprecated-after-jdk-1-1-version/