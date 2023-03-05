package com.java.sjq.base.juc.readwritelock;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  读读并发、读写互斥、写写互斥而已,
 *  如果一个对象并发读的场景大于并发写的场景,
 *  那就可以使用 ReentrantReadWriteLock来达到保证线程安全的前提下提高并发效率。
 * @author sunupo
 * @param <E>
 */
public class ReadWriteLockDemo<E> {
    private ArrayList<E> list = new ArrayList<E>();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();


    public void add(E e) {
        try {
            writeLock.lock();
            list.add(e);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeLast() {
        try {
            writeLock.lock();
            if(list.size()!=0){
                list.remove(list.size() - 1);
            }
        } finally {
            writeLock.unlock();
        }

    }

    public void set(int index, E e) {
        try {
            writeLock.lock();
            list.set(index, e);
        } finally {
            writeLock.unlock();
        }

    }

    public E get(int index) {
        try {
            // 读锁
            readLock.lock();
            return list.get(index);
        } finally {
            readLock.unlock();
        }
    }

}

