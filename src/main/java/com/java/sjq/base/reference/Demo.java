package com.java.sjq.base.reference;

import java.lang.ref.PhantomReference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

public class Demo {
    public static void main(String[] args) {
        byte[] b = new byte[1024*1024*10];
        WeakReference<Byte[]> weakReference = new WeakReference<Byte[]>(new Byte[1024*1024*5]);
        System.out.println("weak1: " + Objects.requireNonNull(weakReference.get()).toString());
//        System.gc();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Optional<Byte[]> optional = Optional.ofNullable(weakReference.get());
        System.out.println("weak2: " + optional.map(Object::toString));

        byte[] b3 = new byte[1024*1024*10];

//        DirectByteBuffer directByteBuffer;
//        ByteBuffer
//        PhantomReference phantomReference;

    }
}
