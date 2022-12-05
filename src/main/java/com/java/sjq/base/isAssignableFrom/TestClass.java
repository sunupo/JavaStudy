package com.java.sjq.base.isAssignableFrom;

class A{
}
class B extends A{
}
class C extends B{
}
class D extends A{
}
public class TestClass {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        B b1 = new B();
        C c = new C();
        D d = new D();
//        A.isAssignableFrom(B) 确定一个类(B)是不是继承来自于另一个父类(A)，或者B就是A
        System.out.println(a.getClass().isAssignableFrom(a.getClass()));
        System.out.println(a.getClass().isAssignableFrom(b.getClass()));
        System.out.println(a.getClass().isAssignableFrom(c.getClass()));
        System.out.println(b1.getClass().isAssignableFrom(b.getClass()));

        System.out.println(b.getClass().isAssignableFrom(c.getClass()));

        System.out.println(b.getClass().isAssignableFrom(a.getClass())); // false
        System.out.println(d.getClass().isAssignableFrom(b.getClass()));  // false

        System.out.println("=====================================");
        System.out.println(A.class.isAssignableFrom(a.getClass()));
        System.out.println(A.class.isAssignableFrom(b.getClass()));
        System.out.println(A.class.isAssignableFrom(c.getClass()));

        System.out.println("=====================================");
        System.out.println(Object.class.isAssignableFrom(a.getClass()));
        System.out.println(Object.class.isAssignableFrom(String.class));
        System.out.println(String.class.isAssignableFrom(Object.class));  // false。String 是 Object 的子类
    }
}