package com.java.sjq.base.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

public class UserServiceInvocationHandler implements InvocationHandler {

    UserService target;
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
        System.out.println("InvocationHandler before" + "\tmethod: " + method.getName());
        if (method.getName().equals("toString") || method.getName().equals("hashCode")) {
            return method.invoke(this.target);
        }
        System.out.println("proxy==getInstance()" + (proxy == getInstance()));  // true
//        Arrays.stream(proxy.getClass().getMethods()).forEach(System.out::println);
        Method method1 = ((Proxy) proxy).getClass().getMethod("getProxyClass", ClassLoader.class, Class[].class);
        Method method2 = ((Proxy) proxy).getClass().getMethod("add", int.class);
        System.out.println(method1.getName() + "\t" + method2.getName());

        /**
         * ① 调用 proxy 这些方法不会进入死循环
         */
        synchronized (proxy) {
            proxy.notifyAll();
            proxy.notify();
            System.out.println("proxy.getClass()\t" + proxy.getClass());
            proxy.wait(1, 1);
        }
        /**
         * ② 调用 proxy 这些方法会进入循环，再次调用 InvocationHandler 的 invoke 方法
         */
        String a = new String("a");
        String intern = a.intern();
        System.out.println("string-----------------------"+(a==intern));

        System.out.println("hashcode\t+++" + proxy.hashCode());
        System.out.println("toString\t+++" + proxy.toString());


        System.out.println("this.target.toString()\t" + this.target.toString());
        System.out.println("this.target.hashCode()\t" + this.target.hashCode());

        System.out.println("this.target==proxy\t" + (this.target == proxy));

        System.out.println("this.target.toString()==proxy.toString()\t" + this.target.toString().equals(proxy.toString()));
        System.out.println("this.target.hashCode() == proxy.hashCode()\t" + (this.target.hashCode() == proxy.hashCode()));
        Object result = method.invoke(this.target, args);
        System.out.println("InvocationHandler after");

        return result;
    }
}
