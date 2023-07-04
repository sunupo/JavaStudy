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
 * LinkedBlockingQueue：一个由链表结构组成的(可选边界)有界阻塞队列。
 * PriorityBlockingQueue：一个支持优先级排序的无界阻塞队列。
 * DelayQueue：一个使用优先级队列实现的无界阻塞队列。
 * SynchronousQueue：一个不存储元素的阻塞队列。
 * LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。
 * LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。
 *
 *
 * ArrayBlockingQueue：数组 有界
 *
 * LinkedBlockingQueue：链表 （可选边界)有界【不扩容，offer 超出容量返回 false】
 *
 * PriorityBlockingQueue：优先级排序  _无界_  【虽然构造方法指定容量参数，但是offer会扩容】
 *
 * DelayQueue：一个使用优先级队列（PriorityQueue）实现的延迟  _无界_
 *
 * SynchronousQueue：一个不存储元素的阻塞队列。
 *
 * LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。  _无界_   （transfer方法， 有消费者就不排队，没有消费者就排队。try transfer 设置了排队超时时间）
 *
 * LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。 （可选边界)有界
 *
 */
/*

1 2 3 4
5 6 7 8
6 7 8 9
7 8 9 10

 */