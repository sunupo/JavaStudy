package com.java.sjq.base.classInitOrder;

import java.util.Arrays;

public class Child extends Parent{
    // 成员变量
    private String memberVar = initMemberVar();
    // 静态变量
    private static String staticVar = initStaticVar();

    public Main next= new Main();

    // 静态代码块
    static {
        System.out.println("子类静态代码块被调用...");
    }

    // 成员代码块
    {
        System.out.println("子类成员代码块被调用...");
    }

    // 构造函数
    public Child(){
        System.out.println("子类构造函数被调用...");
    }

    /**
     *  初始化成员的方法, 输出一句话表示成员变量被初始化了
     *  初始化成员变量的时候 this指针也已经创建了,也输出一下
     */
    public String initMemberVar(){
        System.out.println("子类 this 指针: " + this);
        System.out.println("子类成员变量初始化...");
        return "initMemberVar";
    }

    /**
     *  初始化静态变量方法, 输出一句话表示静态变量被初始化了
     */
    public static String initStaticVar(){
        System.out.println("子类静态变量初始化...");
        return "initstaticVar";
    }

    public static void main(String[] args) {
        System.out.println("父类、子类静态部分执行结束，开始下一步----------");
        new Child();
        int[] iii = new int[]{1,2};
        int[] cloneIii=iii.clone();
        cloneIii[1]=3;
        System.out.println(Arrays.toString(cloneIii));
        System.out.println(Arrays.toString(iii));
        Child[] c = new Child[2];
        for(int i = 0; i < c.length; i++) {
          //
            c[i] = new Child();
        }
        Child[] cloneC = c.clone();
        System.out.println(c[0]==cloneC[0]);
        System.out.println(c[0].next==cloneC[0].next);
    }
}