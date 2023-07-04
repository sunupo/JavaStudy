package com.java.sjq.dataStructure;

import java.util.Scanner;

public class Main {
    public static void main(String[] args){
//        System.out.println(judge("1233421"));
        Scanner s = new Scanner(System.in);
        String str = s.nextLine();
        if(judge(str) || judge(str.substring(1)) || judge(str.substring(0, str.length()-2))){
            System.out.println("true");

        }else{
            for(int i = 1; i < str.length()-1; i++) {
              StringBuilder sb = new StringBuilder();
                String s1 = sb.append(str.substring(0, i)).append(str.substring(i + 1, str.length())).toString();
//                System.out.println(i+"-"+s1);
                if(judge(s1)){
                  System.out.println("true");
                  return;
                }
            }
            System.out.println("false");
        }
    }

    public static boolean judge(String s){
        int len = s.length();

        if(s.length()%2==0){
            int left = len/2 -1;
            int right = len/2;
            while(left>=0 && right<len&&s.charAt(left)==s.charAt(right)){
                left--;
                right ++;
            }

        return left==-1 && right==len;
        }else{
            int left = len/2 -1;
            int right = len/2 + 1;
            while(left>=0 && right<len&&s.charAt(left)==s.charAt(right)){
                left--;
                right ++;
            }
            System.out.println(left+"-"+right);
            return left==-1 && right==len;
        }

    }

//    public void judge(String s){
//        int len = s.length();
//        // s 一共最多 2n - 1 个回文串
//        for(int i=0; i< 2*len -1;i++){
//            int left = i/2;
//            int right = i/2 + i%2;
//
//
//        }
//
//    }
}

//
//编写一个函数来验证给定字符串是否满足回文串 II。
//        给你一个字符串 s，最多 可以从中删除一个字符。
//        请你判断 s 是否能成为回文字符串：如果能，返回 true ；否则，返回 false 。
//        示例 1：
//
//        输入：s = "aba"
//        输出：true
//        示例 2：
//
//        输入：s = "abca"
//        输出：true
//        解释：你可以删除字符 'c' 。
//        示例 3：
//
//        输入：s = "abc"
//        输出：false
//        提示：
//        1 <= s.length <= 10^5
//        由小写英文字母组成