package com.soecode.lyf.java;


import com.soecode.lyf.service.impl.BookServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SJQ {
    public static final int paraA=10;
    private static final String paraString="sunjignqin";
    private void myfun(Number a, String b){
        System.out.println("123"+a+b);
        new Outer().fun1();
    }
    int a=10;

    public static void main(String[] args) {
        new SJQ().myfun(1,"2");
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        Integer[] obj = list.toArray(new Integer[5]);
        System.out.println("show obj: "+ Arrays.toString(obj));

        Object[] objects1 = list.toArray();
        Object[] objects2 = list.toArray();
        System.out.println("objects1 == objects2 : "+(objects1 == objects2));
        objects1[1]=4;
        System.out.println("show objects1: "+ Arrays.toString(objects1));
        System.out.println("show objects2: "+ Arrays.toString(objects2));
        System.out.println("show list: "+list);
        Arrays.copyOf(objects1, 2);
        System.out.println("show objects2: "+ Arrays.toString(Arrays.copyOf(objects1, 2)));
        System.out.println("show objects2: "+ Arrays.toString(Arrays.copyOfRange(objects1, 2,10)));



    }
//    class Inner1{
//        public class Inner100{
//
//        }
//    }
//    public final nimin fun(){
//        class Inner2{  // class 不能有修饰符
//
//        }
//        return new nimin(10){
//
//        };
//    }
//
//    public static  void main(String[] args) {
//        SJQ sjq = new SJQ();
//        sjq.fun();
//    }
}

class Outer {
    int out_i=1;

    Outer(){

    }
    class Inner {
        public String publicString = "Inner.publicString";
        Inner(){

        }
        int Inner_fun1(){
            return out_i;
        }
        class InnerInner{
            public String publicString = "Inner.Inner.publicString";
            int InnerInner_fun1(){
                return out_i;
            }
            class InnerInnerInner{
                public String publicString = "Inner.Inner.Inner.publicString";
                int InnerInnerInner_fun1(){
                    return out_i;
                }
            }
        }
    }
    String fun1(){
        int fun1_i=2;
        SJQ sjq = new SJQ();
        class Inner2{
            public String publicString = "Inner.publicString";
            String Inner2_fun1(){
                return out_i + publicString;
            }
            int Inner2_fun2(){
                System.out.println(fun1_i); //可以访问方法内的非final变量
//                fun1_i=2;  // 不允许对外界简单类型变量修改。1.没有定义为final类型的fun_i不能修改。2.定义为final的其它变量肯定不能改。
                sjq.a = 100;  // 可以对外界引用类型的属性进行修改。
//                sjq=new SJQ(); // 不允许对外界引用类型变量修改。
                return fun1_i + out_i ;
            }
        }
        Inner2 inner2 = new Inner2();
        String a =inner2.Inner2_fun2()+inner2.Inner2_fun1();
        return a;
    }

    static class PublicInner{
    }
    abstract class AbstractInner{
         public abstract void fun1();
    }

    private static Runnable myRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    class MyClassRunnable implements Runnable{
        @Override
        public void run() {

        }
    }

    public PublicInner outer_fun1(int paraA){
//        todo 匿名内部类（也是局部内部类）
        AbstractInner abstractInner = new AbstractInner() {  //匿名内部类的父类是abstract，必须实现父类的抽象方法。也就是说匿名内部类不能是抽象的。
            @Override
            public void fun1() {

            }
        };
        return new PublicInner(){
            int a=100;
            int A;
            public void setA(int a) {
                this.a = a;
//                this.A = paraA++;// 错误，只能修改final修饰的基本类型局部变量
                this.A = paraA; // 正确，可以访问非final修饰的基本类型局部变量
            }
        };
//        return new Inner(){  // 匿名内部类
////            public Inner(){  //1、匿名内部类不能有构造方法(因为没有自己的名字，^_^)
////            }
////            static int b=100;  // 2、匿名内部类不能定义静态成员
////            public static void fun(){}
//            int a=100;
//            public int getA(){
//                return a++;
//            }
//        };
    }

    static class Inner3{
        public String publicString = "Inner3.publicString";
    }
    class ttt{}

    ttt t = new Outer.ttt();

    Other anonymousOther = new Other() {
        public String publicString = "Anonymous Other.publicString";
    };
    public Other getAnonymousOther() {
        return anonymousOther;
    }

    Other Other = new Other();
    public Other getOther() {
        return Other;
    }

    BookServiceImpl bookService = new BookServiceImpl();

    public static void main(String[] args) {
        System.out.println(new Outer().new Inner());
        System.out.println(new Outer.Inner3());
        System.out.println("\t");
        System.out.println(new Outer().getAnonymousOther());
        System.out.println("\t");
        System.out.println(new Outer().fun1());
        Location mapLocation = new MapLocation(){};
//        funnn((UserLocation)mapLocation);
        System.out.println(null instanceof Object);




    }
    static class MyUserLocation implements UserLocation{

    }

    public static void funnn(UserLocation location){
        System.out.println(location.a);

    }

}

interface Location{
    int a=10;

}

interface MapLocation extends Location{
    int a=11;

}

interface UserLocation extends Location{
int a=12;
}

class Other {
    public String publicString = "Other.publicString";
}
//
///**
// *  外部类的修饰符分为：可访问控制符和非访问控制符两种。
// *     可访问控制符是: 公共类修饰符 public
// *     非访问控制符有：抽象类修饰符 abstract
// *     最终类修饰符：final
// */
// class class2{
//    /**
//     * 1.成员内部类：可以有 public private protected修饰符作用在class
//     * --1.作为外部类的一个成员存在，与外部类的属性方法并列。
//     * --2.成员内部类中，不能定义静态成员（属性、方法）。
//     * --3.成员内部类中，可以访问外部类的所有成员。
//     * --4.成员内部类中，可以与外部类存在同名变量。
//     * --5.成员内部类中，访问自己的变量，使用"变量名"或者"this.变量名"。（同名变量，优先访问内部类自己的变量）
//     * --6.成员内部类中，访问外部类中与成员内部类中同名的变量，使用"外部类名字.this.变量名"。
//     * --7.成员内部类中，访问外部类中与成员内部类中不同名的变量，使用"变量名"或者"外部类名字.this.变量名"。
//     * --8.外部类的非static方法访问成员内部类的成员：a:创建内部类对象。b：调用内部类成员。
//     * --9.外部类的static方法（或者外部类的外部）访问成员内部类的成员：
//     * ----a：创建外部类对象：Out out = new Out()。
//     * ----b:通过外部类对象创建内部类对象：In in = out.new In()
//     * ----c：通过in访问内部类成员
//     * 2.局部内部类：
//     * 3.静态内部类：使用了static修饰符作用在class
//     * 4.匿名内部类：
//     * 除了内部类，
//     */
//    public class Inner{
//
//     }
//
//}
//
//abstract class  nimin{
//    public int a;
//    nimin(int a){
//        this.a = a;
//    }
//    static void fun1(){
//
//    }
//    abstract void fun2();
//}
//
///**
// * interface 接口中：方法：public abstract；属性：public、static、final
// */
//interface int1{
//    public static final int a=0;
//    public abstract void fun1();
//    public abstract void fun2();
//
//    /** 接口方法的默认修饰符包含abstract，需要被子类实现。
//    final的作用但是限制子类重新覆盖此方法，所以不能共存。*/
////    public abstract final void fun2();
//}
// class class1 implements int1{
//    @Override
//    public void fun1() {
//         new int1(){
//            @Override
//            public void fun1() {
//
//            }
//
//             @Override
//             public void fun2() {
//
//             }
//         };
//    }
//
//    @Override
//    public void fun2() {
//
//    }
//}
///**
// * abstract 修饰：类、方法
// * final 修饰：类、方法、变量
// * final 与 static可以同时用吗？
// *
// *
// */