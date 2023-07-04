package com.java.sjq.dataStructure;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Author: Amos
 * E-mail: amos@amoscloud.com
 * Date: 2022/11/11
 * Time: 4:35
 * Description: 40%
 */
public class Main0154 {
    String reverseWords(String s, int start, int end){
        int len = end - start + 1;
        String[] srcArr = s.split(" ");
        String[] reverseArr=new String[len];
        System.arraycopy(srcArr,start,reverseArr,0,len);
        Collections.reverse(Arrays.asList(reverseArr));
        System.arraycopy(reverseArr,0,srcArr,start,len);
        return Arrays.stream(srcArr).collect(Collectors.joining(" "));
    }
    @Test
    public void testReverseWords(){
        String s = "I am a developer";
        int start=1;
        int end=2;
        System.out.println(reverseWords(s,start,end));
    }
    public static void main(String[] args) {



//        4
//        5
//        5 4 1 1 1
//        int gpu=4;
//        int n =5;
////        int[] arr = new int[]{5,4,1,1,1};
//        int[] arr = new int[]{1,2,3,4,5};
//        int left = 0;
//        for(int i = 0; i < arr.length; i++) {
//          //
//            left = Math.max(left + arr[i] - gpu, 0);
//            System.out.println(left);
//
//
//        }
//        System.out.println(left);
//        System.out.println(arr.length + (left+gpu-1)/gpu);

        // 8,9 10 ,11,12,13,14
        // 11/4=2  12/4=3  13/4 = 3  14/4 = 3, 15/4=3 16/4=4 17/4 =3

//        100 10
//
        int base = 100;
        Integer[] arr = new Integer[]{95, 96 ,97 ,98, 99 ,101 ,102, 103, 104, 105};
        Arrays.sort(arr, (o1, o2) -> {
            if(Math.abs(o1.intValue()-base) - Math.abs(o2.intValue() -base) >0){
                return 1;
            }else if(Math.abs(o1.intValue()-base) - Math.abs(o2.intValue()-base)<0){
            return  -1;
            } else {
                return o1.intValue() -o2.intValue();
            }
        });
        System.out.println(Arrays.toString(arr));

//
//        try (Scanner scanner = new Scanner(System.in)) {
//            int n = scanner.nextInt();
//            int[] sites = new int[n];
//            for (int i = 0; i < sites.length; i++) {
//                sites[i] = scanner.nextInt();
//            }
//            int bestSite = solution(sites);
//            System.out.print(bestSite);
//        }
//    }
//    private static int solution(int[] sites) {
//        Arrays.sort(sites);
//        if (sites.length % 2 == 0) {
//            return sites[sites.length / 2 - 1];
//        } else {
//            return sites[sites.length / 2];
//        }
    }
    @Test
    public void fun(){
        int n=50;
        int[] dp = new int[50+1];
        dp[0]=0;
        dp[1]=1;
        dp[2] = 1;
        dp[3]=2;
        for(int i = 4; i < n+1; i++) {
          //
            dp[i] = dp[i-1] + dp[i-3];
        }
        System.out.println(dp[50]);

    }
}
