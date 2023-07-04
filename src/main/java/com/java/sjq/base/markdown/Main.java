package com.java.sjq.base.markdown;

import java.util.Arrays;
import java.util.Scanner;

/**
 * 从数组中选择元素组成小于n得最大整数
 * 5 4 8 2
 * 5416
 * 5288
 *
 * [(137条消息) 算法：小于n的最大数\_一直不懂的博客-CSDN博客](https://blog.csdn.net/shenchaohao12321/article/details/128786765?spm=1001.2101.3001.6650.3&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EYuanLiJiHua%7EPosition-3-128786765-blog-127660164.235%5Ev28%5Epc_relevant_recovery_v2&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EYuanLiJiHua%7EPosition-3-128786765-blog-127660164.235%5Ev28%5Epc_relevant_recovery_v2&utm_relevant_index=3)
 */
public class Main{

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while(in.hasNextLine()){
            String[] s = in.nextLine().split(" ");
            int[] nums = new int[s.length];
            for(int i = 0; i < nums.length; i++){
                nums[i] = Integer.parseInt(s[i]);
            }

            int n = Integer.parseInt(in.nextLine());
            System.out.println(new Main().getMaxLessNum(nums, n));
        }
    }

    private int getMaxLessNum(int[] nums, int n){
        Arrays.sort(nums);
        int minNum = nums[0];
        String maxNum = getMaxString(minNum, n);
        StringBuilder s = new StringBuilder();
        boolean preIndexLess = false;
        for(int i = 0; i < maxNum.length(); i++){
            int index = preIndexLess ? nums.length - 1 : search(maxNum, i, nums);
            s.append(nums[index]);
            if(nums[index] < (maxNum.charAt(i) - '0')){
                preIndexLess = true;
            }
        }

        return Integer.parseInt(s.toString());

    }

    // 获取要查询的最大值， 可能出现拼接不出与原来数字长度相等的数，那么就返回长度减一的最大值
    private String getMaxString(int minNum, int n){
        boolean flag = false;

        String s = String.valueOf(n);
        // 解决前一个版本出现的bug
        for(char c : s.toCharArray()){
            if((c - '0') > minNum){
                flag = true;
                break;
            }else if ((c - '0') < minNum){
                break;
            }
        }

        int maxNum = flag ? (n - 1) : (int)(Math.pow(10, s.length() - 1) - 1);
        return String.valueOf(maxNum);
    }

    /**
     * 二分查找，查找等于或者小于findNum的最右边位置
     *
     */
    private int search(String maxNum, int index, int[] nums){
        int curMax = maxNum.charAt(index) - '0';
        int minNum = nums[0];
        int findNum = curMax;
        if(index < maxNum.length() - 1){
            findNum = (maxNum.charAt(index + 1) - '0') < minNum ? curMax - 1 : curMax;
        }

        int left = 0, right = nums.length - 1;
        while(left <= right){
            int mid = left + (right - left) / 2;
            if(nums[mid] > findNum){
                right = mid - 1;
            }else if(nums[mid] < findNum){
                left = mid + 1;
            }else{
                return mid;
            }
        }

        return right;
    }
}

