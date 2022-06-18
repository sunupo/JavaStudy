package com.java.sjq.base.extendsAbstractInnerClass;

/**
 * 抽象父类有抽象内部类，子类继承父类可以不必继承父类的抽象内部类
 */
public class Child extends Parent{
    @Override
    public void parentFunction() {
        System.out.println(getClass().getDeclaredMethods()[0].getName());
    }

    public static void main(String[] args) {
        Child child = new Child();
        child.parentFunction();

    }
}
