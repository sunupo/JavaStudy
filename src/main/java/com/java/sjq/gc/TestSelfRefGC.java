package com.java.sjq.gc;

/**
 * 自己引用自己也会本垃圾回收期回收
 */
public class TestSelfRefGC {
    public static void main(String[] args) throws InterruptedException {
        Test t = t(); // t()结束后 test1
        for(;;)System.gc(); // 会输出 "Test2 object is garbage collected!"
//        for(;;); // 不调用System.gc() 可能很久都不会输出“Test2 object is garbage collected!”
    }
    public static Test t(){
        Test test1 = new Test("Test1 object");
        Test test2 = new Test("Test2 object");
        Test test3 = new Test("Test3 object");

        /**
         * 先让 test1->test2->test3
         * 然后孤立 test2，使 test1 -> test3
         */
        test1.setRef(test2);
        test2.setRef(test3);
        test1.setRef(test3);
        test2.setRef(test2);
        return test1;

    }
}


class Test {
    private String name;
    private Test ref;
    public Test(String name) {
        this.name = name;
        this.ref = null;
    }
    public void setRef(Test t) {
        this.ref = t;
    }
    public void finalize() throws Throwable {
        System.out.println(this.name + " is garbage collected!");
        super.finalize();
    }
}