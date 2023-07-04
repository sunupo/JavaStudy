package com.java.sjq.dataStructure;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 输入: s = "abcabcbb"
 输出: 3
 解释: 因为无重复字符的最长子串是 "abc"，所以其长度为 3。
 */
public class Solution {
    Map<Character, Integer> win = new HashMap<>();

    public  int lengthOfLongestSubstring(String s) {
        char[] sChars = s.toCharArray();
        int sLen = s.length();
        int left=0, right =0;


        int res = 0;
        while(right<sLen){
            char rightChar = sChars[right];
            right++;
            win.put(rightChar, win.getOrDefault(rightChar, 0) + 1);

            while (win.getOrDefault(rightChar, 0) > 1){
                char leftChar = sChars[left];
                win.put(leftChar, win.get(leftChar) - 1);
                left++;
            }
            res = Math.max(res, right - left);

        }

        return res;
    }

    @Test
    public  void t(){
//       String s = "abcabcbb";
//       String s = "abcdcabcdefad";
//       int [] a = new int[256];
//       a['A'] = 100;
//       a['a'] = 101;
//       System.out.println(a[65]);
//       System.out.println(a[97]);
//       Character c;
//       System.out.println(lengthOfLongestSubstring(s));
        System.out.println(getLower('A'));
        System.out.println(getLower('b'));
        System.out.println(getLower('c'));
        System.out.println(getLower('Z'));
        System.out.println(getUpper('A'));
        System.out.println(getUpper('b'));
        System.out.println(getUpper('c'));
        System.out.println(getUpper('Z'));
        System.out.println(circle('A'));
        System.out.println(circle('b'));
        System.out.println(circle('c'));
        System.out.println(circle('Z'));
    }
    //    0100 0001
    //    0110 0001
    char getLower(char c){
        return (char) (c | 0x20);
    }
    char getUpper(char c){
        // 11011111
        return (char) (c & 0xdf);
    }
    char circle(char c){
        // 0010 0000
        return (char)(c ^ 0x20);

    }

}