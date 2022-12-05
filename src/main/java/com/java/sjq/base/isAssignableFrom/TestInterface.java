package com.java.sjq.base.isAssignableFrom;

interface InterfaceA{
}

class ClassB implements InterfaceA{

}
class ClassC implements InterfaceA{

}
class ClassD extends ClassB{

}
public class TestInterface {
    public static void main(String[] args) {
        System.out.println(InterfaceA.class.isAssignableFrom(InterfaceA.class));
        System.out.println(InterfaceA.class.isAssignableFrom(ClassB.class));
        System.out.println(InterfaceA.class.isAssignableFrom(ClassC.class));
        System.out.println(ClassB.class.isAssignableFrom(ClassC.class));  // false。tricky：B、C 看作兄弟关系
        System.out.println("============================================");

        System.out.println(ClassB.class.isAssignableFrom(ClassD.class));
        System.out.println(InterfaceA.class.isAssignableFrom(ClassD.class)); // 注意 虽然 A 是 interface，D是 class，仍然是 true。
    }
}
