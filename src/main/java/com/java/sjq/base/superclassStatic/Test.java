package com.java.sjq.base.superclassStatic;

/**
 * java类 static成员变量 以及方法都会被子类继承吗？
 * 结论：子类可以访问，无法继承，所以无法多态。
 */
class Parent {
    String name;
    static public  int a=2;
    static void fun(){
        System.out.println("P-static-fun:\t"+ a);
    }
    void gun(){
        System.out.println("p-gun:\t"+a);
    }
}
class Son extends Parent {
//    static public  int a=2;
    static void fun(){
        System.out.println("s-static-fun:\t "+ a);
        a++; // S 自己定义了a，a++对P的a没影响；若S没有定义a，那么a++就会更改P的a
    }
    void gun(){
        System.out.println("s-gun:\t"+a);
    }
}
public class Test{
    public static void main(String[] args) {
        Son.fun(); // s-static-fun2
        Parent.fun(); // P-static-fun3
        Parent parent = new Parent();
        Son son = new Son();
        fun(son);  // P-static-fun3
        gun(son);  // s-gun3

        fun(parent);  // P-static-fun2
        gun(parent);  // P-gun3

    }
    static void fun(Parent p){  // 虽然 p.f2(s) 传入的是示例s，但是还是无法下转型为类S的实例，于是p.fun()调用的还是类P的静态方法fun()
        p.fun(); // 不应该通过类实例访问静态成员
    }
    static void gun(Parent p){
        p.gun();
    }

}
/**
 * 总结：当 son，parent 作为方法参数传递时，方法的参数定义为 Parent 类型。
 * 那么，通过参数类型的对象调用 son与parent的相同签名的方法的时候，
 * 1. 如果方法是 static 静态的那么总会调用 parent 的静态方法
 * 2. 如果方法是 non-static 非静态的，那么就会调用son，parent各自类中声明的方法.
 * 所以避免通过类实例访问静态成员。虽然不报错。
 */