package com.java.sjq.dataStructure.interview;

import java.util.*;

/**
 * @author sunupo
 */
public class Main {
    public static void main(String[] args){
      //
        cal("123", "45");
        cal("123", "456");
        cal("999", "999");
        cal("9", "9");
    }
    public static void cal(String s1, String s2){
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        Map<Integer,String> res = new HashMap<>();
        int jinwei=0;
        for(int i = c2.length - 1; i > -1; i--) {
            String tmpRes="";
            for(int k=i; k<c2.length -1; k++){
                tmpRes = tmpRes+"0";
            }
            jinwei = 0;
          for(int j = c1.length - 1; j > -1; j--) {
            int cur = (c2[i] -'0') *(c1[j]-'0') + jinwei;
            // 进位
              jinwei = cur/10;
              tmpRes = (cur%10 ) + tmpRes;
//              System.out.println(jinwei +"--"+tmpRes+"--"+(cur%10+'0'));
          }
          for(int p = 0; p< i; p++){
              tmpRes = "0"+tmpRes;
          }
            res.put(i, tmpRes);
        }
        res.put(0, jinwei>0 ? jinwei+res.getOrDefault(0,""): res.getOrDefault(0,""));

        Iterator<Map.Entry<Integer, String>> iterator = res.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer, String> next = iterator.next();
            System.out.println(next.getKey() +":"+next.getValue());

        }
        int jin = 0;
        String result = "";
        // 把每个字符串相加，字符串个数为c2.length
        for(int i=c1.length+c2.length-2;i>-1;i--){
            int tmp =jin;
            for(int j = 0; j < c2.length; j++) {
              tmp += (res.get(j).charAt(i) - '0');
            }
            result = (tmp%10)+""+result;
            jin = tmp/10;
//            System.out.println(result+"::::"+jin+":::"+tmp);
        }
        System.out.println(jin>0 ? jin+result: result);


    }
}

// 给定两个STRING类型的大整数（超过10000位），求两者的乘积.