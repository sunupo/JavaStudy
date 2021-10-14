package com.soecode.lyf.java;

import junit.framework.TestCase;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.proxy.*;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class BuyBookTest extends TestCase {
    @Test
    public void testCglibProxy(){
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./");
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(BuyBook.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            System.out.println("事务开始......" + method.getName());
            Object o1 = proxy.invokeSuper(obj, args);
            System.out.println("事务结束......." + method.getName());
            return o1;
        });

        BuyBook buyBook = (BuyBook) enhancer.create();
        buyBook.buy();
    }

    @Test
    public void testCallBackfilter(){
        Enhancer enhancer =new Enhancer();
        enhancer.setSuperclass(BuyBook.class);
        CallbackFilter callbackFilter = new CallbackFilter(){
            @Override
            public int accept(Method method) {
                switch (method.getName()){
                    case "buy":
                        System.out.println("method:buy");
                        return 0;
                    case "buy2":
                        System.out.println("method:buy2");
                        return 1;
                    case "sell":
                        System.out.println("method:sell");
                        return 2;
//                    case "toString":
//                        System.out.println("method:toString");
//                        return 3;
                    default:
                        return 0;
                }
            }
        };

        class Inteceptor implements MethodInterceptor{
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                System.out.println("事务开始......" + method.getName());
                Object o1 = proxy.invokeSuper(obj, args);
                System.out.println("事务结束......." + method.getName());
                return o1;
            }
        }

        Callback[] cbarray=new Callback[]{
//                (MethodInterceptor) (obj, method, args, proxy) -> {
//                    System.out.println("事务开始cc......" + method.getName());
//                    Object o1 = proxy.invokeSuper(obj, args);
//                    System.out.println("事务结束cc......." + method.getName());
//                    return o1;
//                    },
                new Inteceptor(),
                NoOp.INSTANCE,
                (FixedValue)()-> {
                    System.out.println("返回固定结果");
                    return 100;}
        };//数组中的元素都是Callback的实例
        enhancer.setCallbacks(cbarray);
        enhancer.setCallbackFilter(callbackFilter);
        BuyBook buyBook = (BuyBook) enhancer.create();

        System.out.println("开始=========");
        System.out.println(buyBook);
        System.out.println("0==========\n");

        buyBook.buy();
        System.out.println("1========\n");

        buyBook.buy2();
        System.out.println("2=-=-=-=-=-\n");

        System.out.println(buyBook.sell(100));
        System.out.println("3-=-=-=-=-=\n");

        System.out.println(buyBook.sell(200));

    }

    /**
     * BeanGenerator 实例
     */
    public void testbeanGenerator(){
        BeanGenerator beanGenerator = new BeanGenerator();

        try {

            beanGenerator.addProperty("name", String.class);
            Object target = beanGenerator.create();
            Method setter = target.getClass().getDeclaredMethod("setName", String.class);
            Method getter = target.getClass().getDeclaredMethod("getName");
            // 设置属性的值
            setter.invoke(target, "张三");

            System.out.println(getter.invoke(target));

            Class<?> clazz = target.getClass();
            System.out.println(clazz.getName());
            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                System.out.println(methods[i].getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testFastClass(){
        //1⃣️通过BuyBook.class创建FastClass实例
        FastClass fastClass = FastClass.create(BuyBook.class);

        try {
//            2⃣️创建BuyBook实例（）
//            BuyBook buyBook = new BuyBook();
            BuyBook buyBook = (BuyBook) fastClass.newInstance(new Class[]{String.class}, new Object[]{"JAVA BOOK"});
//            3⃣️调用方法1
            fastClass.invoke("sell", new Class[]{int.class}, buyBook,new Object[]{100});
//            3⃣️调用方法2
            FastMethod fastMethod = fastClass.getMethod("sell", new Class[]{int.class});
            fastMethod.invoke(buyBook, new Object[]{300});
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}