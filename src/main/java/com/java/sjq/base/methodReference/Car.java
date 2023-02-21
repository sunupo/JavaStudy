package com.java.sjq.base.methodReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 方法引用
 */
public class Car {
    //Supplier是jdk1.8的接口，这里和lamda一起使用了
    public static Car create(final Supplier<Car> supplier) {
        return supplier.get();
    }
    public static void collide(final Car car) {
        System.out.println("Collided " + car.toString());
    }
    public void follow(final Car another) {
        System.out.println("Following the " + another.toString()+this);
    }
    public void repair() {
        System.out.println("Repaired " + this.toString());
    }
    public void testThis(Car car) {
        System.out.println("testThis " + this.toString());
    }
    public static void main(String[] args) {
//        构造器引用：它的语法是Class::new，或者更一般的Class< T >::new实例如下：
        final Car car = Car.create( Car::new );
        final List< Car > cars = Collections.singletonList(car);

//        静态方法引用：它的语法是Class::static_method，实例如下：  //  todo 方法有参数 有static
        cars.forEach( Car::collide );  // static, 所以 collide 有一个参数

//        （对象方法引用）特定类的任意对象的方法引用：它的语法是Class::method实例如下： // todo 方法（少的第一个参数为调用者本身，即 car）无参数 无static
        Consumer<Car> consumer = Car::repair;
        cars.forEach( Car::repair );  // non-static， this 就是 cars 的 car

//        (实例方法引用)特定对象的方法引用：它的语法是instance::method实例如下：  todo 方法一个参数
        final Car police = Car.create( Car::new );
        System.out.println(police.toString());  // police
        police.repair();  // this 指向 police
        cars.forEach( police::follow ); // this 指向 police
        cars.forEach( police::testThis );  // this 指向 police
//        cars.forEach( police::repair ); // 报错


    }
}