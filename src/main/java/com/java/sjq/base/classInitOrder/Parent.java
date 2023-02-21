package com.java.sjq.base.classInitOrder;

public class Parent {
    // 成员变量
    private String parmemberVar = initParMemberVar();
    // 静态变量
    private static String parstaticVar = initParStaticVar();

    // 静态代码块
    static {
        System.out.println("父类静态代码块被调用...");
    }

    // 成员代码块
    {
        System.out.println("父类成员代码块被调用...");
    }

    // 构造函数
    public Parent(){
        System.out.println("父类构造函数被调用...");
    }

    /**
     *  初始化成员的方法, 输出一句话表示成员变量被初始化了
     *  初始化成员变量的时候 this指针也已经创建了,也输出一下
     */
    public String initParMemberVar(){
        System.out.println("父类成员变量初始化...");
        return "initMemberVar";
    }

    /**
     *  初始化静态变量方法, 输出一句话表示静态变量被初始化了
     */
    public static String initParStaticVar(){
        System.out.println("父类静态变量初始化...");
        return "initstaticVar";
    }

}