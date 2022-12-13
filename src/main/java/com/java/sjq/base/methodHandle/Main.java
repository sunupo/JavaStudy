package com.java.sjq.base.methodHandle;

import java.lang.invoke.MethodHandles;

public class Main {
    public static int value = 123;

    static {
        i = 0;                // 给变量赋值可以正常编译通过
//        System.out.print(i);  // 这句编译器会提示“非法向前引用”
    }
    static int i = 1;

    static {
        System.out.println("static code" + value);
        System.out.println("i"+i);  // 这句编译器会提示“非法向前引用”

    }

    {
        System.out.println("normal code");
    }

    public Main() {
        System.out.println("construct ");
    }

    public static void main(String[] args) {
        new Main();

    }

    public MethodHandles.Lookup getLookUp(boolean isPublic) {
        return isPublic ? MethodHandles.publicLookup() : MethodHandles.lookup();
    }
}
