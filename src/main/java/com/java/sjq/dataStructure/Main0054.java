package com.java.sjq.dataStructure;

import org.junit.Test;

public class Main0054 {
    public static void main(String[] args){
      //
        Integer a = 1;
        Integer b = 1;
        Integer c = 11111;
        Integer e = new Integer(1);
        Integer f = new Integer(1);
        System.out.println(a==b);
        System.out.println(a==c);
        System.out.println(e==f);
    }
    public int countNum (int L, int R, int x) {
        // write code here
        int res = 0;
        char obj = (x+"").charAt(0);
        for(int i = L; i <= R; i++){
            char[] cur = (i+"").toCharArray();
            for(int j = 0; j< cur.length;j++){
                if(cur[j]==obj){
                    res++;
                }
            }
        }
        return res;
    }
    @Test
    public void testCount(){
        int i = countNum(2, 22, 2);
System.out.println(i);
        int clz = turnTimes("CLZ");
        System.out.println(clz);

    }

    public int turnTimes (String s) {
        // write code here
        int res = 0;
        char[] obj = "CMB".toCharArray();
        char[] sChar = s.toCharArray();
        for(int i = 0; i < sChar.length; i++) {
          res += (obj[i]-sChar[i]) * Math.pow(26, 2-i);
        }
        return  res;
    }
}