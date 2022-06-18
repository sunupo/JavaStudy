package com.java.sjq.base.lambda;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 1：当 ::前面为一个类名，后面跟一个静态方法时，
 * 如下所示，此时等号左边的函数式接口中的方法和静态方法签名一致即可，即方法参数个数和类型一致，
 * 如果左边的函数式接口中的方法返回值不是void则要求右边lambda静态方法的返回值和其一致，
 * 如果左边的函数式接口中的方法返回值是void，则右边的lambda静态方法的返回值可以为任意类型。
 *
 * 2：当::前面为一个类名，后面为一个实例方法时，
 * 如下图所示，此时要求等号左边的函数式接口方法中的第一个参数为实例对象，
 * 后面的参数和实例方法保持一致即可。即函数式接口方法的参数个数比实例方法的参数多一个，因为最前面一个留给了实例对象。
 *
 * 3：当::前面为this时，后面肯定为实例方法，如下图所示，此时等号左边的函数式接口中的方法和实例方法签名一致即可，
 * 与上面1中所述的静态方法看上去类似，但是注意这边是在实例方法中声明的。
 */
public class Demo1 {

    public static String  fun1(String a){
        System.out.println("fun1: " + a);
        return a;
    }
    public void fun2(){
        System.out.println("fun2: ");
    }
    public String fun3(String a){
        System.out.println("fun3: " + a);
        return a+"fun3";
    }
    public int fun4(String a){
        System.out.println("fun3: " + a);
        return 0;
    }
    public int fun5(int a){
        System.out.println("fun5: " + a);
        return a*a;
    }
    public int fun6(int a){
        System.out.println("fun6: " + a);
        return a+10;
    }
    public int fun7(int a){
        System.out.println("fun7 " + a);
        return a*2;
    }
    public boolean fun8(int a){
        return a>18;
    }
    public boolean fun9(int a){
        return a<35;
    }

    /**
     * 函数式接口必须有一个不包含 default 的 抽象方法。
     * @param <T>
     */
    @FunctionalInterface
    public interface CustomFunction<T, U>{
        int apply(T t, U u);
        default String applyNext(T t, U u) {
            return "";
        }
    }

    public static void main(String[] args) {
        Consumer<String> consumer = Demo1::fun1;  // ①
        CustomFunction<Demo1, String> customFunction = Demo1::fun4;  //②
        Consumer<Demo1> consumer2 = Demo1::fun2; // ②
        BiConsumer<Demo1, String> biConsumer = Demo1::fun3; // ②
        Demo1 demo1 = new Demo1();
        consumer.accept("hello");
        consumer2.accept(demo1);
        biConsumer.accept(demo1, "biConsumer");
        Consumer<String> consumer3 = demo1::fun3; // ③
        consumer3.accept("demo1::fun3");


        /**
         * Function
         */
        Function<Integer, Integer> function5  = demo1::fun5;
        Function<Integer, Integer> function6  = demo1::fun6;
        Function<Integer, Integer> function7  = demo1::fun7;
        Integer apply = function5.andThen(function6).andThen(function7).apply(10);
        System.out.println("apply" + apply);

        /**
         * Predict
         */

        Predicate<Integer> predicate = demo1::fun8;
        boolean test = predicate.and(demo1::fun9).test(36);
        System.out.println("test: "+test);

    }
}
