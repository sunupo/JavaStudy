package com.java.sjq.base.collection;

import java.util.Arrays;
import java.util.List;

public class ArrayAsList {
    public static void main(String[] args){
        int[] _int  = {1, 2, 9};
        List ints = Arrays.asList(_int);
        System.out.println(((int[]) (ints.get(0)))[2]);
        System.out.println(ints.size());
    }
}
