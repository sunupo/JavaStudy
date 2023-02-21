package com.java.sjq.base.juc.reentrantlock;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main2 {
    private Lock lock = new ReentrantLock();



    public static void main(String[] args) throws InterruptedException {
    }

    @Before
    public void before(){
        lock.lock();
    }

    @Test
    public void test(){
        lock.unlock();
        System.out.println("has unlock");
    }


}
