package com.java.sjq.base.classloader;

import com.w3cspring.annotationconfig.AutowireAnnotation.SpringConfiguration;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Base64;

public class Main extends ClassLoader {

    public static void main(String[] args) {
//        ClassLoader loader = {new URLClassLoader("https://localhost:8080/")};
//        try {
//            loader.loadClass("");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        URL url = testFun();
        System.out.println("main" + url);

    }

    /**
     * defineClass 通过字节码数组创建具体的类
     */
    public static URL testFun() {
        Main main = new Main();
        byte[] code = Base64.getDecoder().decode("yv66vgAAADQAGwoABgANCQAOAA8IABAKABEAEgcAEwcAFAEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBAApTb3VyY2VGaWxlAQAKSGVsbG8uamF2YQwABwAIBwAVDAAWABcBAAtIZWxsbyBXb3JsZAcAGAwAGQAaAQAFSGVsbG8BABBqYXZhL2xhbmcvT2JqZWN0AQAQamF2YS9sYW5nL1N5c3RlbQEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAdwcmludGxuAQAVKExqYXZhL2xhbmcvU3RyaW5nOylWACEABQAGAAAAAAABAAEABwAIAAEACQAAAC0AAgABAAAADSq3AAGyAAISA7YABLEAAAABAAoAAAAOAAMAAAACAAQABAAMAAUAAQALAAAAAgAM");
        Class<?> hello = main.defineClass("Hello", code, 0, code.length);
        try {
            hello.newInstance();
            hello.getConstructor().newInstance();
            System.out.println("getTypeName:\t" + hello.getTypeName());
            Arrays.stream(hello.getMethods()).forEach((method) -> {
                // java跟c不一样，java中的main方法不属于任何一个类，它仅仅是一个程序入口，所以你写到哪里都行，当然要在你的项目文件夹里才行。
//                System.out.println(method.getName());
            });
            return main.test1();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }finally {
            System.out.println("finally");
        }
        return main.test1();
    }

    /**
     * 获取资源的位置
     */
    URL test1() {
        URL url = Main.class.getProtectionDomain().getCodeSource()
                .getLocation();

        System.out.println(url);  // file:/C:/Users/sunupo/IdeaProjects/JavaStudy/target/classes/

        InputStream resourceAsStream = Main.class.getResourceAsStream("Main.class");
        System.out.println(resourceAsStream);
        return url;
    }

    /**
     * Runtime 反射使用 demo
     *
     * @throws Exception
     */
    void test2() throws Exception {
        Class<?> runtime = Class.forName("java.lang.Runtime");
        Method getRuntime = runtime.getMethod("getRuntime");
        Method exec = runtime.getMethod("exec", String.class);
        Object invoke = getRuntime.invoke(runtime);
        exec.invoke(invoke, "calc");
//这里直接使用Runtime.getRuntime().exec("calc")就行
    }

}

/**
 * package Loder;
 * <p>
 * public class Hello{
 * public Hello(){
 * System.out.println("Hello World!!");
 * }
 * }
 */