package com.java.sjq.base.createClass;

import org.openjdk.jol.info.ClassLayout;

public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog();
        System.out.println(ClassLayout.parseInstance(dog).toPrintable());
        System.out.println(dog.hashCode());
        System.out.println(Integer.toString(dog.hashCode(), 2));
        System.out.println(ClassLayout.parseInstance(dog).toPrintable());

    }
}

class Dog {

}

/**
 * Java利用 ClassLayout 查看对象头
 * https://blog.csdn.net/weixin_39009690/article/details/122080406
 *
 * Java synchronized偏向锁后hashcode存在哪里？ 原创
 * https://blog.51cto.com/u_15303040/5252003
 */