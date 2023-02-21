package com.leetcode;

import java.util.HashMap;
import java.util.Map;

public class TwoSum {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        int[] res = new int[2];
        for(int i=0;i<nums.length;i++){
            if(map.get((Integer)(target - nums[i]))==null){
                map.put((Integer)nums[i], (Integer)i);
            }else{
                res[1] = i;
                res[0] = map.get((Integer)(target - nums[i]));
            }
        }
        return res;

    }
}
