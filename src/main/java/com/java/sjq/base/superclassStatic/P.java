package com.java.sjq.base.superclassStatic;

/**
 * java类 static成员变量 以及方法都会被子类继承吗？
 * 结论：子类可以访问，无法继承，所以无法多态。
 */
public class P implements Cloneable{
    String name;
    @Override
    public Object clone() throws CloneNotSupportedException {
        P p = null;
        try {
            p = (P)super.clone();
        } catch ( ClassCastException e) {
            e.printStackTrace();
        }
        return p;
    }

    static public  int a=2;
    static void fun(){
        System.out.println("P-static-fun"+ a);
    }
    void gun(){
        System.out.println("p-gun"+a);
    }

    public static void main(String[] args) {
        S.fun(); // s-static-fun2
        fun(); // P-static-fun3
        P p = new P();
        S s = new S();
        p.f2(s);  // P-static-fun3
        p.f3(s);  // s-gun3

    }
    void f2(P p){  // 虽然 p.f2(s) 传入的是示例s，但是还是无法下转型为类S的实例，于是p.fun()调用的还是类P的静态方法fun()
        p.fun();
    }
    void f3(P p){
        p.gun();
    }
}

class S extends P{
    //    static public  int a=2;
    static void fun(){
        System.out.println("s-static-fun"+ a);
        a++; // S 自己定义了a，a++对P的a没影响；若S没有定义a，那么a++就会更改P的a
    }
    void gun(){
        System.out.println("s-gun"+a);
    }
}