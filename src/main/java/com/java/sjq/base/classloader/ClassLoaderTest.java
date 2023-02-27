package com.java.sjq.base.classloader;

import java.io.IOException;
import java.io.InputStream;

/**
 * 不同的类加载器加载同一个类，那么这个类也是不同的。
 */
public class ClassLoaderTest {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader myLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                InputStream is = getClass().getResourceAsStream(fileName);
                if (is == null) {
                    return super.loadClass(name);
                }
                try {
                    byte[] bytes = new byte[is.available()];
                    is.read(bytes);
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        /**
         *  loader 'ClassLoaderTest' by myLoader-
         */
        Class<?> aClass = myLoader.loadClass("com.java.sjq.base.classloader.ClassLoaderTest");
        Object obj = aClass.newInstance();
        System.out.println(obj.getClass());
        System.out.println(obj instanceof ClassLoaderTest); // false
    }
}