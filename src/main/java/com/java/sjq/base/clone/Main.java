package com.java.sjq.base.clone;


import java.util.Arrays;



public class Main {
    static class Child{
        int next;

        public Child(int next) {
            this.next = next;
        }
    }
    public static void main(String[] args){
      // 一、一维基本类型数组，深拷贝
        int[] iii = new int[]{1,2};
        int[] cloneIii=iii.clone();
        cloneIii[1]=3;
        System.out.println(Arrays.toString(cloneIii));
        System.out.println(Arrays.toString(iii));

//        二、二维基本类型数组，变为一维之后才会是浅拷贝。
        int[][] jjj = {{1,2},{3,4}};
        int[][] cloneJjj = jjj.clone();
        cloneJjj[0]=new int[]{9,9};
        System.out.println("jjj"+Arrays.toString(jjj[0]));
        System.out.println("cloneJjj"+Arrays.toString(cloneJjj[0]));

//        三、引用类型数组，浅拷贝
        Child[] c = new Child[2];
        for(int i = 0; i < c.length; i++) {
            c[i] = new Child(i);
        }
        Child[] cloneC = c.clone();
        cloneC[0].next=999;
        System.out.println("cloneC\t"+Arrays.toString(cloneC));
        System.out.println("c\t"+Arrays.toString(c));
        System.out.println("c[0].next\t"+c[0].next);
        System.out.println("cloneC[0].next\t"+cloneC[0].next);

        System.out.println("c[0]==cloneC[0]\t"+(c[0]==cloneC[0]));
        System.out.println("c[0].next==cloneC[0].next\t"+(c[0].next==cloneC[0].next));
    }
}
