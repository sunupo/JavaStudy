package com.java.sjq.base.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ArrayBuilder and HeapPollutionExample 演示 堆污染
 *
 * 可变长度参数 varargs 作为方法时是参数化类型 ParameterizedType，方法内部相当于用一个数组来接受。
 * 方法内部把类型给固定了 Arrays.asList(42)，与运行时方法外传递过来的参数类型不一致。
 */
class ArrayBuilder {

    public static <T> void addToList (List<T> listArg, T... elements) {
        for (T x : elements) {
            listArg.add(x);
        }
    }

    public static void faultyMethod(List<String>... l) {
        Object[] objectArray = l;     // Valid
        objectArray[0] = Arrays.asList(42);
//        objectArray[0] = Arrays.asList("42"); 改为字符串 "42" 就不会出错了
        String s = l[0].get(0);       // ClassCastException thrown here
    }

}

public class HeapPollutionExample {

    public static void main(String[] args) {

        List<String> stringListA = new ArrayList<String>();
        List<String> stringListB = new ArrayList<String>();

        ArrayBuilder.addToList(stringListA, "Seven", "Eight", "Nine");
        ArrayBuilder.addToList(stringListB, "Ten", "Eleven", "Twelve");
        List<List<String>> listOfStringLists =
                new ArrayList<List<String>>();
        ArrayBuilder.addToList(listOfStringLists,
                stringListA, stringListB);

        ArrayBuilder.faultyMethod(Arrays.asList("Hello!"), Arrays.asList("World!"));
    }
}