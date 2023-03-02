package com.java.sjq.base.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 使用ServiceLoader 的内部类 LazyIterator 的 hasNextService 方法，在 “META_INF/services” 路径下找
 */
public class JDKSPIDemo {
    public static void main(String[] args){
      //
        ServiceLoader<ICar> s = ServiceLoader.load(ICar.class);
        Iterator<ICar> iterator = s.iterator();
        while (iterator.hasNext()){
            ICar car = iterator.next();
            System.out.println(car.getName());
        }
    }
}
