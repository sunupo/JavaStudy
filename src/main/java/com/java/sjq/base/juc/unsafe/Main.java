package com.java.sjq.base.juc.unsafe;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    int num;
    private int age;
    public static void main(String[] args) throws NoSuchFieldException {
        Unsafe unsafe = getUnsafe();
        System.out.println("Unsafe 加载成功："+unsafe);
        Field _age = Main.class.getDeclaredField("age");
        long offset = unsafe.objectFieldOffset(_age);
        Main main = new Main();
        unsafe.getAndSetInt(main, offset, 20); //unsafe.putOrderedInt(main, offset, 20);
        AtomicInteger i;
        i.lazySet();
        CyclicBarrier
        System.out.println(unsafe.getInt(main, offset));
        System.out.println(unsafe.getIntVolatile(main, offset));
        System.out.println(main.age);
        unsafe.park(true, 0);
        System.out.println(offset);
    }
    public static Unsafe getUnsafe() {
        Unsafe unsafe = null;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            /**
             * https://blog.nowcoder.net/n/c03e52fb56074bd6ad9081e79a6408cf#:~:text=field.get%20%28null%29%20%3A,%E9%80%9A%E8%BF%87%E5%AD%97%E6%AE%B5%E8%8E%B7%E5%8F%96%E5%AF%B9%E8%B1%A1%201%E3%80%81%E5%AD%97%E6%AE%B5%E4%B8%8D%E6%98%AF%E9%9D%99%E6%80%81%E5%AD%97%E6%AE%B5%E7%9A%84%E8%AF%9D%2C%E8%A6%81%E4%BC%A0%E5%85%A5%E5%8F%8D%E5%B0%84%E7%B1%BB%E7%9A%84%E5%AF%B9%E8%B1%A1%201%202
             *  field.get(null) :  通过字段获取对象
             *
             * 1、字段不是静态字段的话,要传入反射类的对象
             * 1
             * 2
             * Field field=A.class.getDeclaredField("fild");
             * int a= (Integer)field.get(new A()) ;
             * 2、字段是静态字段的话,传入任何对象都是可以的,包括null
             * 1
             * 2
             * 3
             * Field static field=A.class.getDeclaredField("staticFild");
             * int b= (Integer)staticfield.get("") ;
             * int d= (Integer)staticfield.get(null) ;
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unsafe;
    }
}


