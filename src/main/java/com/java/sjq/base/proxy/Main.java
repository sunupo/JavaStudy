package com.java.sjq.base.proxy;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.junit.Test;
import sun.misc.ProxyGenerator;
import sun.reflect.Reflection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

    }

    @Test
    public void testJDKProxy() throws IOException {
        UserService userServiceImpl = new UserServiceImpl();
        UserServiceInvocationHandler handler = new UserServiceInvocationHandler(userServiceImpl);
        UserService instance = (UserService) handler.getInstance();
        instance.add(1);

        
        byte[] bytes = ProxyGenerator.generateProxyClass("$Proxy0", new Class[]{userServiceImpl.getClass()});
        FileOutputStream os = new FileOutputStream("src/main/java/com/java/sjq/base/proxy/Proxy0.class");
        os.write(bytes);
        os.close();
        Arrays.stream(userServiceImpl.getClass().getInterfaces()).forEach(i -> {
            System.out.println(i.getTypeName());
        });

    }

    @Test
    public void testCglibProxy() {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./cgg");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserServiceImpl.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            System.out.println("" + obj.getClass() + "\t" + method.getName() + "\t" + args + "\t" + proxy.getSignature());
            System.out.println("事务开始......" + method.getName());
            Object o1 = proxy.invokeSuper(obj, args);
            System.out.println("事务结束......." + method.getName());
            return o1;
        });
        UserService userService = (UserService) enhancer.create();
        userService.add(100);
        testCglibProxy2();
    }
@Test
    public void testCglibProxy2() {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./src/main/java/com/java/sjq/base/proxy/");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserServiceImpl.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            System.out.println("" + obj.getClass() +"\t"+ method.getName() +"\t"+ Arrays.toString(args) +"\t"+ proxy.getSignature());

            System.out.println("事务开始......" + method.getName());
            Object o1 = proxy.invokeSuper(obj, args);
            System.out.println("事务结束......." + method.getName());
            return o1;
        });
        UserService userService = (UserService) enhancer.create();
        userService.add(100);
    }


}