package com.java.sjq.base.string;

import org.junit.Test;

/**
 * jdk1.7之后的做法：intern方法的作用就是在尝试把堆中对象放入串池中。
 * 如果串池中已有，会返回串池中的对象。并且s调用intern方法后依旧指向堆中的对象。
 * 如果串池中没有，会在串池中创建一个“ab”对象并返回，并且会让s指向串池中的“ab”对象。
 * <br></>
 * 在jdk1.6，当一个String调用intern方法时，
 * 如果串池中没有，会将堆中的字符串对象复制一份放到串池中，最后返回StringTable中刚加入的对象。
 */
public class StrignIntern {
    @Test
    public void list() {
        String s1 = "ab";
        String s = new String("a") + new String("b");  // 也是新建了一个StringBuilder实现字符串的拼接与创建。最终的结果就是在堆中创建了一个“ab”字符串对象。并且在串池中加入“a”，“b”对象。
        String s2 = s.intern();

        System.out.println(s1 == "ab"); // true
        System.out.println(s == "ab"); // false, 因为 s.intern()之前已经存在 "ab"，所以 s 仍然指向堆中的对象，s2作为返回值，执行串池中的对象s1="ab"
        System.out.println(s2 == "ab"); // true

        String s3 = new String("1") + new String("1");
        s3.intern();
        String s4 = "11";
        System.out.println(s3 == s4);
    }

    @Test
    public void testintern() {
        String s = new String("1");
        s.intern();
        String s2 = "1";
        System.out.println(s == s2);

//        String s3 = new String("1") + new String("1");
        String s3 = "1" + "1";
        s3.intern();
        String s4 = "11";
        System.out.println(s3 == s4);
    }

    @Test
    public void testintern2() {
        String s1 = "abc";
        String s2 = "abc";
        System.out.println(s1 == s2); //true
    }

    @Test
    public void case4() {
        String s1 = "abc" + new String("def"); // 不会在常量池生成 abcdef
        s1.intern();
        String s2 = "abcdef";
        System.out.println(s1 == s2); //true
    }

    @Test
    public void case4_2() {
        String s1 = "abc" + new String("def"); // ▲ s1的value属性值为 "abcdef",  "abcdef"不会放入常量池，
        String s2 = "abcdef";
        String s3 = s1.intern();
        System.out.println(s1 == s2); //false
        System.out.println(s1 == s3); //false
        System.out.println(s2 == s3); //true
    }

    @Test
    public void case6() {
        String s1 = new String(new char[]{'a', 'b', 'c'});  // new String(char value[])这个构造器，这个构造器只会把参数value[]数组复制到一个new char[newLength]中（源码可以看到），然后返回这个new char[newLength]数组的地址给String对象的value。
        s1.intern();
        System.out.println(s1 == s1.intern()); // true

        String s2 = new String("def");
        s2.intern();
        System.out.println(s1 == s2.intern()); // false
    }

    @Test
    public void case7() {
        String s1 = new StringBuilder("ja").append("va").toString(); // 注意！jvm编译时会预先自动加载字面量"java"放入常量池，
        s1.intern();
        System.out.println(s1 == s1.intern()); //false

        String s2 = new StringBuilder("s2").toString();
        s2.intern();
        System.out.println(s2 == s2.intern()); //false

        String s3 = new StringBuilder("hello").append("world").toString();
        s3.intern();
        System.out.println(s3 == s3.intern()); //true
        System.out.println("java" == "java".intern()); //true
        System.out.println("aj" == "aj".intern()); //true

    }

    @Test
    public void case8_1() {
        final String s1 = "a";
        final String s2 = "b";
        String s3 = "ab";
        String s4 = s1 + s2;
        System.out.println(s3 == s4);//true
    }

    @Test
    public void case8_2() {
        String s1 = "javaEEhadoop";
        String s2 = "javaEE";
        String s3 = s2 + "hadoop";
        System.out.println(s1 == s3);//false

        final String s4 = "javaEE";//s4:常量
        String s5 = s4 + "hadoop";
        System.out.println(s1 == s5);//true
    }

    @Test
    public void case9() {
        String s1 = new String("ja");
    }

    @Test
    public void case10() {
        String s1 = "va";
    }

    @Test
    public void case11() {
        String s1 = new String(new char[]{'j', 'a', 'v', 'a'});
    }

    /**
     * s3.intern();             //因为常量池中不存在"11"，
     * https://blog.csdn.net/tianyuzui6/article/details/124417267
     */
    @Test
    public void case990(){
      //
//        String s3 = new String("1") + new String("1"); //常量池生成一个"1"，堆生成一个"11"
        String s3 = new String(new char[]{'1', '1'});
        //s3指向堆中"11"
        //中间还有2个匿名的new String("1")暂不讨论
        s3.intern();             //因为常量池中不存在"11"，
        //jdk6会将堆中"11"复制到常量池中，
        //jdk7则将堆中"11"的引用添加到常量池中，
        //此时s3仍指向堆中"11"
        String s4 = "11";        //因为常量池中已存在"11"或其引用，s4指向常量池中"11"
        System.out.println(s3==s4);  //jdk6中，s4指向常量池中"11"，s3指向堆中"11"，false
        //jdk7中，s4指向常量池中指向堆中"11"的引用，true
    }
}
