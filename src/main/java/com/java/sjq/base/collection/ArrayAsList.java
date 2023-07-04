package com.java.sjq.base.collection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayAsList {
    public static void main(String[] args){
        int[] _int  = {1, 2, 9};
        List ints = Arrays.asList(_int);
        System.out.println(((int[]) (ints.get(0)))[2]);
        System.out.println(ints.size());
    }

    @Test
    public void testAsList() {

        Integer[] is = {1,2,3};
        List<Integer> integers = new ArrayList<>(Arrays.asList(is));
        integers.add(4);
        System.out.println(is.length); //3
        System.out.println(integers.size());// 4
    }

    @Test
    public void testArrayLis() {

        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        List<Integer> integers2 = new ArrayList<>(integers);

        integers2.remove(0);

        System.out.println(integers.size());// 3
        System.out.println(integers); // [1, 2, 3]
        System.out.println(integers2.size());// 2
        System.out.println(integers2); // [2, 3]
    }

    @Test
    public void testArrayLis2() {
        class MyObj implements Cloneable{
            int val;

            public MyObj(int val) {
                this.val = val;
            }

            @Override
            public String toString() {
                return "MyObj{" +
                        "val=" + val +
                        '}';
            }

            @Override
            protected MyObj clone()  {
//                Object obj = super.clone();
                Object obj = null;
                try {
                    obj = super.clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }

                if(obj instanceof MyObj){
                    return (MyObj) obj;
                }
                return null;
            }
        }
        List<MyObj> objs = new ArrayList<>();
        objs.add(new MyObj(1));
        objs.add(new MyObj(2));
        objs.add(new MyObj(3));

        ArrayList<MyObj> objs1 = new ArrayList<>(objs);
        objs1.get(0).val = 0;
        objs1.remove(2);

        System.out.println(objs);  // [MyObj{val=0}, MyObj{val=2}, MyObj{val=3}]
        System.out.println(objs1);  // [MyObj{val=0}, MyObj{val=2}]

        MyObj o1 = new MyObj(1);
        MyObj o2 = o1;
        o2.val = 2;
        System.out.println(o1.val);  // 2
        MyObj o3 = o1.clone();
        o3.val = 3;
        System.out.println(o1.val); // 2
    }
}
