package com.java.sjq.base.juc.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TestSynchronized {

    private int num = 0;

    public void test(){

        for (int i = 0;i < 1000;i ++){

            synchronized (this){

                num ++;

            }

        }

    }
    synchronized public void test2(){

        num++;

    }
    public String concatString(String s1, String s2, String s3) {

        synchronized (s3){
            s3="0";
        }
        synchronized (s3){
            s3="0";
        }
        synchronized (s3){
            s3="0";
        }
        synchronized (s3){
            s3="0";
        }
        synchronized (s3){
            s3="0";
        }
        concatString2();
        return s1 + s2 + s3;
    }
    public synchronized int concatString2() {
        return 1;

    }

}

class mylock implements Lock{
    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}