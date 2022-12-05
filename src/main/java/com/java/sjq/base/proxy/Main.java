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
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserServiceInvocationHandler handler = new UserServiceInvocationHandler(userServiceImpl);
        UserService instance = (UserService) handler.getInstance();
        instance.add(1);  // todo 栈溢出
        byte[] bytes = ProxyGenerator.generateProxyClass("$Proxy0", new Class[]{userServiceImpl.getClass()});

        FileOutputStream os = new FileOutputStream("Proxy0.class");
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

    public void testCglibProxy2() {
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserServiceImpl.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            System.out.println("" + obj.getClass() + method.getName() + args + proxy.getSignature());

            System.out.println("事务开始......" + method.getName());
            Object o1 = proxy.invokeSuper(obj, args);
            System.out.println("事务结束......." + method.getName());
            return o1;
        });
        UserService userService = (UserService) enhancer.create();
        userService.add(100);
    }


}

abstract class ABClass{
    public ABClass(int a) {
    }
}

class Test3 extends ABClass{

    /**
     * Constructs a new {@code Proxy} instance from a subclass
     * (typically, a dynamic proxy class) with the specified value
     * for its invocation handler.
     *
     * @param h the invocation handler for this proxy instance
     * @throws NullPointerException if the given invocation handler, {@code h},
     *                              is {@code null}.
     */
    protected Test3(InvocationHandler h) {
        super(1);
    }

    public void test0() {
        test();
    }
    public void test() {
        System.out.println(Reflection.getCallerClass(-1));
    }
}