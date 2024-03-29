package com.java.sjq.base.juc.aba;

import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class AtomicStampedReferenceTest {
    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        try
        {
            lock.lock();
        } catch (Exception e){

        } finally{
            lock.unlock();
        }
        // 定义AtomicStampedReference    Pair.reference值为1, Pair.stamp为1
        AtomicStampedReference atomicStampedReference = new AtomicStampedReference(1,1);

        new Thread(()->{
            int[] stampHolder = new int[1];
            int value = (int) atomicStampedReference.get(stampHolder);
            int stamp = stampHolder[0];
            System.out.println("Thread1 read value: " + value + ", stamp: " + stamp);

            // 阻塞1s
            LockSupport.parkNanos(1000000000L);
            // Thread1通过CAS修改value值为3   stamp是版本，每次修改可以通过+1保证版本唯一性
            if (atomicStampedReference.compareAndSet(value, 3,stamp,stamp+1)) {
                System.out.println("Thread1 update from " + value + " to 3");
            } else {
                System.out.println("Thread1 update fail!");
            }
        },"Thread1").start();

        new Thread(()->{
            int[] stampHolder = new int[1];
            int value = (int)atomicStampedReference.get(stampHolder);
            int stamp = stampHolder[0];
            System.out.println("Thread2 read value: " + value+ ", stamp: " + stamp);
            // Thread2通过CAS修改value值为2
            if (atomicStampedReference.compareAndSet(value, 2,stamp,stamp+1)) {
                System.out.println("Thread2 update from " + value + " to 2");

                // do something

                value = (int) atomicStampedReference.get(stampHolder);
                stamp = stampHolder[0];
                System.out.println("Thread2 read value: " + value+ ", stamp: " + stamp);
                // Thread2通过CAS修改value值为1
                if (atomicStampedReference.compareAndSet(value, 1,stamp,stamp+1)) {
                    System.out.println("Thread2 update from " + value + " to 1");
                }
            }
        },"Thread2").start();
    }
}

/**
 * output:
 * Thread1 read value: 1, stamp: 1
 * Thread2 read value: 1, stamp: 1
 * Thread2 update from 1 to 2
 * Thread2 read value: 2, stamp: 2
 * Thread2 update from 2 to 1
 * Thread1 update fail!
 *
 * Process finished with exit code 0
 */
