package com.java.sjq.base.methodHandle;

import java.lang.invoke.MethodHandle;
        import java.lang.invoke.MethodHandles;
        import java.lang.invoke.MethodType;

public class MethodHandleTest{

    public static void a(String s) {
        System.out.println("a: " + s);
    }

    public void b(String s) {
        System.out.println("b: " + s);
    }

    public static int c(String s1, String s2) {
        System.out.println("c: arg0: " + s1 + ", arg1:" + s2);
        return 1;
    }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup l = MethodHandles.lookup();
        MethodHandle mh0 = l.findStatic(MethodHandleTest.class, "a", MethodType.methodType(void.class, String.class));
        System.out.println("mh0's type: " + mh0.type());
        mh0.invokeExact("mh0");

        MethodHandle mh1 = l.findVirtual(MethodHandleTest.class, "b", MethodType.methodType(void.class, String.class));
        System.out.println("mh1's type: " + mh1.type());
        mh1.invokeExact(new MethodHandleTest(), "mh1");

        MethodHandle mh2 = mh1.bindTo(new MethodHandleTest()); // 要绑定到目标 mh1 的第一个参数的值为 new MethodHandleTest()
        System.out.println("mh2's type: " + mh2.type());
        mh2.invokeExact("mh2"); // 因为绑定到了mh1，所以这里比mh1少一个参数
        System.out.println();

        MethodHandle mh3 = l.findStatic(MethodHandleTest.class, "c", MethodType.methodType(int.class, String.class, String.class));
        System.out.println("mh3's type: " + mh3.type());
        int c = (int) mh3.invokeExact("mh3-1", "mh3-2");
        System.out.println("mh3 invokeExact result: " + c);

        MethodHandle mh4 = MethodHandles.insertArguments(mh3, 0, "mh4-1");
        System.out.println("mh4's type: " + mh4.type());
        int c2 = (int) mh4.invokeExact("mh4-2");
        System.out.println("mh4 invokeExact result: " + c2);

        MethodHandle mh5 = MethodHandles.dropArguments(mh3, 1, String.class);
        System.out.println("mh5's type: " + mh5.type());
        int c3 = (int) mh5.invokeExact("mh5-1", "mh5-2", "mh5-3");
        System.out.println("mh5 invokeExact result: " + c3);

        MethodHandle mh6 = mh0.asType(MethodType.methodType(void.class, Object.class));
        System.out.println("mh6's type: " + mh6.type());
        mh6.invokeExact((Object) "mh6");
    }
}