package com.java.sjq.base.juc.blockqueue;

import java.util.concurrent.*;

class DelayedElement implements Delayed{
    String name;
    long time;

    private long start = System.currentTimeMillis();


    public DelayedElement(String name, long time) {
        this.name = name;
        this.time = time;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert((start+time) - System.currentTimeMillis(),TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if(this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
            return -1;
        } else if(this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}

public class DelayQueueDemo {
    public static void main(String[] args) {
        // Create a DelayQueue
        DelayQueue<DelayedElement> queue = new DelayQueue<>();

        // Create and add elements to the queue
        DelayedElement element1 = new DelayedElement("Element 1", 1000);
        DelayedElement element2 = new DelayedElement("Element 2", 2000);
        DelayedElement element3 = new DelayedElement("Element 3", 3000);
        queue.offer(element1);
        queue.offer(element2);
        queue.offer(element3);

        // Create a thread to remove elements from the queue
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    // Remove the head of the queue
                    DelayedElement element = queue.take();
                    System.out.println("Removed element: " + element.getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Start the thread
        thread.start();
    }
}
