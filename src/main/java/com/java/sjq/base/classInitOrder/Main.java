package com.java.sjq.base.classInitOrder;

import org.junit.Test;

import java.util.Arrays;

public class Main {

    @Test
    void testExtendFather(){
        new Son();

    }

    @Test
    void testUseFatherInnerClass(){
        Father f = new Father();
//        Father.NormalInnerFather normalInnerFather = new f.NormalInnerFather();
    }
}

class Son extends Father {
    static{
        System.out.println("static son code before");
    }
    SonProp sonProp = new SonProp();
    static SonProp sonProp1 = new SonProp("static");
    static{
        System.out.println("static son code after");
    }
    public Son() {
        System.out.println("Son is construct");
    }

    static class StaticInnerSon{
        static{
            System.out.println("static StaticInnerSon code before");
        }
        SonProp sonProp = new SonProp("NormalInnerSon");
        static SonProp sonProp1 = new SonProp("StaticInnerSon");
        static{
            System.out.println("static StaticInnerSon code after");
        }
        public StaticInnerSon() {
            System.out.println("StaticInnerSon is construct");
        }

    }
}

class Father {
    static{
        System.out.println("static father code before");
    }
    static FatherProp fatherProp = new FatherProp("static");
    FatherProp fatherProp1 = new FatherProp();
    static{
        System.out.println("static father code after");
    }
    public Father() {
        System.out.println("Father is construct");

    }
    static class StaticInnerFather{
        static{
            System.out.println("static StaticInnerFather code before");
        }
        SonProp sonProp = new SonProp("NormalInnerFather");
        static SonProp sonProp1 = new SonProp("StaticInnerFather");
        static{
            System.out.println("static StaticInnerFather code after");
        }
        public StaticInnerFather() {
            System.out.println("StaticInnerFather is construct");
        }

    }
    public class NormalInnerFather{
        public NormalInnerFather() {
            System.out.println("NormalInnerFather is construct");
        }
    }
}

class SonProp {
    public SonProp(String... str) {
        System.out.println((str.length > 0 ? str[0] : "") + "--Son Prop is construct");
    }
}

class FatherProp {
    public FatherProp(String... str) {
        System.out.println((str.length > 0 ? str[0] : "") + "--Father Prop is construct");
    }
}