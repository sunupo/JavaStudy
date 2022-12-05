package com.java.sjq.base.juc.unsafe;

import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class Main {
    int num;
    private int age;
    public static void main(String[] args) throws NoSuchFieldException {
        Unsafe unsafe = getUnsafe();
        System.out.println("Unsafe 加载成功："+unsafe);
        Field _age = Main.class.getDeclaredField("age");
        long offset = unsafe.objectFieldOffset(_age);
        Main main = new Main();
        unsafe.getAndSetInt(main, offset, 20);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unsafe;
    }
}


