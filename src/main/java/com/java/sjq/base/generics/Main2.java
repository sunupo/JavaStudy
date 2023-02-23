package com.java.sjq.base.generics;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Main2
{
    public static void main(String[] args) {
        //

        List<String> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        System.out.println(a.getClass()+""+(a.getClass()==b.getClass()));
        Pair<Integer, String> p1 = new Pair(1, "apple");
        Pair p2 = new Pair(2, "pear");
        boolean same = Main2.compare(p1, p2);

    }

    /**
     * 静态方法的返回类型前，必须加上泛型
     * @param p1
     * @param p2
     * @return
     * @param <K>
     * @param <V>
     */
    public static <K,V > boolean compare(Pair<K, V> p1, Pair<K, V> p2) {
        return p1.getKey().equals(p2.getKey()) &&
                p1.getValue().equals(p2.getValue());
    }
}
