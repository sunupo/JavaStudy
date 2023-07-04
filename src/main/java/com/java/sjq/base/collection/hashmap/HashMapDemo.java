package com.java.sjq.base.collection.hashmap;

import sun.reflect.generics.tree.Tree;

import java.lang.reflect.Array;
import java.util.*;

public class HashMapDemo {
    public static void main(String[] args){
      //
        HashSet<Integer> a = new HashSet<>();
        a.addAll(new ArrayList<>(a));
        a.add(1);
        a.add(2);
        a.add(2);
        a.add(3);
        a.remove((Integer) 2);
        System.out.println(Arrays.toString(a.toArray()));
        HashMap m;

    }
}
