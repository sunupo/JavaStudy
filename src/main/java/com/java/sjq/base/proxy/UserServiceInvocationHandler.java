package com.java.sjq.base.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UserServiceInvocationHandler implements InvocationHandler {

    UserService target;  // b被代理的目标对象
    Optional<UserService> userServiceInstance = Optional.empty();

    public UserServiceInvocationHandler(UserService userService) {
        this.target = userService;
    }

    public Object getInstance() {
        if (!userServiceInstance.isPresent()) {
            userServiceInstance = Optional.of((UserService) Proxy.newProxyInstance(
                    this.target.getClass().getClassLoader(),
                    this.target.getClass().getInterfaces(),
                    this));
        }
        return userServiceInstance.get();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("\n\nInvocationHandler before" + "\tmethod: " + method.getName());
        /**
         * 在 InvocationHandler 内部 invoke 方法内调用一下方法，产生递归调用栈溢出：toString、hashCode、equals。
         * 当然也不能调用被代理的方法。在例子中即为 UserService.add()
         * 但是以下方法不会产生递归调用：notify、notifyAll、wait、getClass
         */
        String[] recurProxyMethod = {"toString", "hashCode", "equals"};
        List<String> recurProxyMethodList = Arrays.asList(recurProxyMethod);
        if(recurProxyMethodList.contains(method.getName())){
            return method.invoke(this.target, args);
        }

        System.out.println("proxy==getInstance():\t" + (proxy == getInstance()));  // true
        System.out.println("this.target==proxy:\t" + (this.target == proxy)); // false

        System.out.println( "Proxy.isProxyClass():\t"+Proxy.isProxyClass(proxy.getClass())); // true

//        Arrays.stream(proxy.getClass().getMethods()).forEach(System.out::println); // 输出看看proxy对象有哪些方法
        Method method1 = ((Proxy) proxy).getClass().getMethod("getProxyClass", ClassLoader.class, Class[].class);  // Proxy 类中的方法
        Method method2 = ((Proxy) proxy).getClass().getMethod("add", int.class);
        System.out.println(method1.getName() + "\t" + method2.getName());

        /**
         * ① 调用 proxy 这些方法不会进入死循环
         */
        synchronized (proxy) {  // 必须拿到 proxy 的监视器才能调用 proxy 的notify，wait方法
            proxy.notifyAll();
            proxy.notify();
            System.out.println("proxy.getClass()\t" + proxy.getClass());
            proxy.wait(1, 1);
            proxy.wait(1);
        }

        /**
         * ② 调用 proxy 这些方法会进入循环，再次调用 InvocationHandler 的 invoke 方法
         */

        System.out.println("hashcode\t+++" + proxy.hashCode());  // hashcode	+++254413710
        System.out.println("this.target.hashCode()\t" + this.target.hashCode());  // this.target.hashCode()	254413710
        System.out.println("this.target.hashCode() == proxy.hashCode()\t" + (this.target.hashCode() == proxy.hashCode())); // true


        System.out.println("toString\t+++" + proxy.toString());  // toString	+++com.java.sjq.base.proxy.UserServiceImpl@f2a0b8e
        System.out.println("this.target.toString()\t" + this.target.toString());  // this.target.toString()	com.java.sjq.base.proxy.UserServiceImpl@f2a0b8e
        System.out.println("this.target.toString()==proxy.toString()\t" + this.target.toString().equals(proxy.toString())); // true

        /**
         * this.target 与 proxy hashCode相等， toString 相等，但是下面的输出却是 false
         */
        System.out.println("this.target.equals(proxy)：\t"+this.target.equals(proxy)); // false
        System.out.println("this.target == proxy?：\t"+this.target ==proxy);  // false

        Object result = method.invoke(this.target, args);
        System.out.println("InvocationHandler after");
        return result;
    }
}
