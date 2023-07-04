package com.java.sjq.base.reference;

import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.PhantomReference;
import java.util.*;

public class PhantomDemo {


    /**
     * output:

     */
    @Test
    public   void testPhantom(){
        ReferenceQueue<MyObject> queue = new ReferenceQueue<>();
        PhantomReference<MyObject> phantomReference = new PhantomReference<MyObject>(new MyObject(1), queue);
        System.out.println("phantom: " + phantomReference.get());
        System.gc();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Optional<MyObject> optional = Optional.ofNullable(phantomReference.get());
        System.out.println("weak2: " + optional.map(Object::toString));
        new Thread(()->{
            while (true){
                Reference<? extends MyObject> poll = null;
                try {
                    poll = queue.remove();

                System.out.println("clean thread: poll: " + poll);
                if(poll==null){
                    continue;
                }
                System.out.println("clean thread: poll.get(): " + poll.get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();
    }
}

