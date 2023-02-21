package com.java.sjq.base.juc.thread.threadpool;

import java.util.Scanner;
import java.util.concurrent.*;

import static com.dyuproject.protostuff.CollectionSchema.MessageFactories.PriorityBlockingQueue;

public class Main {
    public static void main(String[] args) throws InterruptedException {
      //

        PriorityBlockingQueue queue = new PriorityBlockingQueue();
        BlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<>(100);
        //带有阻塞的入队操作
        blockingQueue.put(1);
        blockingQueue.put(2);
        blockingQueue.put(3);
        //带有阻塞的出队操作
        Integer ret = blockingQueue.take();
        System.out.println(ret);
        ret = blockingQueue.take();
        System.out.println(ret);
        ret = blockingQueue.take();
        System.out.println(ret);

        ret = blockingQueue.take();//此时就会阻塞等待了
        System.out.println(ret);
    }
    public void array(){
        ArrayBlockingQueue arrayBlockingQueue = new ArrayBlockingQueue(1);
        LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
        PriorityBlockingQueue priorityBlockingQueue = new PriorityBlockingQueue();
        DelayQueue delayQueue = new DelayQueue();
        SynchronousQueue synchronousQueue = new SynchronousQueue();

        LinkedTransferQueue linkedTransferQueue = new LinkedTransferQueue();
        LinkedBlockingDeque linkedBlockingDeque = new LinkedBlockingDeque();
    }
}
/**
 * ArrayBlockingQueue：一个由数组结构组成的有界阻塞队列。
 * LinkedBlockingQueue：一个由链表结构组成的有界阻塞队列。
 * PriorityBlockingQueue：一个支持优先级排序的无界阻塞队列。
 * DelayQueue：一个使用优先级队列实现的无界阻塞队列。
 * SynchronousQueue：一个不存储元素的阻塞队列。
 * LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
 * LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。
 *
 *
 * ArrayBlockingQueue：一个由数组结构组成的有界阻塞队列。
 *
 * LinkedBlockingQueue：一个由链表结构组成的有界阻塞队列。
 *
 * PriorityBlockingQueue：一个支持优先级排序的无界阻塞队列。
 *
 * DelayQueue：一个使用优先级队列实现的延迟无界阻塞队列。
 *
 * SynchronousQueue：一个不存储元素的阻塞队列。
 *
 * LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
 *
 * LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。
 *
 */
/*

1 2 3 4
5 6 7 8
6 7 8 9
7 8 9 10

 */