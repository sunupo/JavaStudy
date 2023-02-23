package com.java.sjq.base.generics;

import java.util.List;

/**
 *
 */
public class Main {
    static class Box<T>{
    }
    public static void main(String[] args){
      //
        Main m = new Main();
        Box<Number> b=new Box<>();
        m.boxTest(b);
        Box<Integer> c=new Box<>();
//        m.boxTest(c); // compile-time error
        Box<Integer> d=new Box<>();
        m.boxTestWildcard(d);

    }
    public synchronized void boxTest(Box<Number> n){
    }
    public void boxTestWildcard(Box<? extends Number> n){

    }
//    void foo(List<?> i) {
//        i.set(0, i.get(0));
//    }

    void foo(List<?> i) {
        fooHelper(i); // 通配符捕获和帮助器方法
    }


    // Helper method created so that the wildcard can be captured
    // through type inference.
    private <T> void fooHelper(List<T> l) {
        l.set(0, l.get(0));
    }
}
