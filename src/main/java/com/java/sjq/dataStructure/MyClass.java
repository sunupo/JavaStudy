package com.java.sjq.dataStructure;


import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class MyClass {

    static Map<Character, Integer> win = new HashMap<>();
    static Map<Character, Integer> need = new HashMap<>();
    public static void main2(String[] args){
        Scanner scanner  = new Scanner(System.in);
        String s1 = scanner.nextLine();
        String s2 = scanner.nextLine();
        int k = scanner.nextInt();
        for (Character c : s1.toCharArray()) {
            need.put(c, need.getOrDefault(c, 0) + 1);
        }

        int left=0, right=0;
        int valid = 0;
        int resIndex = -1;

        while(right<s2.length()){
            Character rightChar = s2.charAt(right);
            right++;
            if(need.containsKey(rightChar)){
                win.put(rightChar, win.getOrDefault(rightChar, 0) + 1);
                if(need.get(rightChar).equals(win.get(rightChar))){
                   valid ++;
                }
            }
            System.out.printf("left:%d right:%d valid:%d\n",left, right, valid);
            while(valid == need.size()){// left ++
                if((right - left)==(s1.length() + k)){
                    resIndex = left;
                }
                Character leftChar = s2.charAt(left);
                left++;
                if(need.containsKey(leftChar)){
                    if(need.get(leftChar).equals(win.get(leftChar))){
                        win.put(leftChar, win.get(leftChar) - 1);
                        valid --;
                    }
                }
            }
            if(resIndex != -1){
                break;
            }
        }
        System.out.println(resIndex);
    }
    public static void main3(String[] args){
      //
        LCS();
    }

    public static void LCS(){
//        private_void_method
//        public_void_method
//        _void_method
// 这种不能算        pi_void_method
        Scanner scanner  = new Scanner(System.in);
        char[] chars1 = scanner.nextLine().toCharArray();
        char[] chars2 = scanner.nextLine().toCharArray();
        int[][] dp = new int[chars1.length + 1][chars2.length+1];
        Arrays.fill(dp[0], 0);
        String[][] sdp = new String[chars1.length + 1][chars2.length+1];
        for(int i = 0; i < sdp.length; i++) {
          for(int j = 0; j < sdp[0].length; j++) {
            sdp[i][j] = "";
          }
        }
        for(int i = 1; i < chars1.length +1; i++) {
          for(int j = 1; j < chars2.length + 1; j++) {
            if(chars1[i-1] == chars2[j-1]){

                if(i>=2 && j>=2){
                    if(chars1[i-2] == chars2[j-2]){
                        dp[i][j] = dp[i-1][j-1] + 1;
                        sdp[i][j] = sdp[i-1][j-1] + chars1[i-1];
                    }else{
                        dp[i][j] = dp[i-1][j-1];
                        sdp[i][j] ="" + chars1[i-1];
                    }
                }else{
                    dp[i][j] = dp[i-1][j-1] + 1;
                    sdp[i][j] = sdp[i-1][j-1] + chars1[i-1];
                }

            }else{
                dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                sdp[i][j] = (dp[i-1][j] > dp[i][j-1]) ? sdp[i-1][j] : sdp[i][j-1];
//                     dp[i][j] = dp[i-1][j-1];
//                sdp[i][j] = sdp[i-1][j-1];
            }
          }
        }
        for (int[] a : dp) {
//            System.out.println(Arrays.toString(a));
        }
//        System.out.println(dp[chars1.length ][chars2.length]);
        System.out.println(sdp[chars1.length ][chars2.length]);
    }

    static class Node{
        int id;
        int dis;
        int queueNum;

        int time; // 花费的时间 赶路 + 排队
        int cost;
        int finalTime;  // 做了核酸的时间点
        boolean selected;

        public Node(int id, int dis, int queueNum) {
            this.id = id;
            this.dis = dis;
            this.queueNum = queueNum;
            this.cost = dis2Cost(dis);
        }
    }
    public static void main(String[] args){

    }

    public static void openMouse(){
        Scanner scanner  = new Scanner(System.in);
        String[] line1 = scanner.nextLine().split(" ");
        String[] line2 = scanner.nextLine().split(" ");
        int h1,m1,h2,m2;
        h1 = Integer.parseInt(line1[0]);
        m1 = Integer.parseInt(line1[1]);
        h2 = Integer.parseInt(line2[0]);
        m2 = Integer.parseInt(line2[1]);
        int n = Integer.parseInt(scanner.nextLine());
        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            String[] line =scanner.nextLine().split(" ");
            nodes.add(new Node(Integer.parseInt(line[0]),Integer.parseInt(line[1]),Integer.parseInt(line[2])));
        }
        for (Node node : nodes) {
            setNodesForFilter(node, h1,m1,h2,m2);
        }
        List<Node> collect = nodes.stream().filter((node) -> node.selected == true).collect(Collectors.toList());
        List<Node> collect1 = collect.stream().sorted(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.time < o2.time) {
                    return 1;
                } else if (o1.time < o2.time) {
                    return -1;
                } else {
                    if (o1.cost < o2.cost) {
                        return 1;
                    } else if (o1.cost > o2.cost) {
                        return -1;
//
                    } else {
                        if (o1.id < o2.id) {
                            return 1;
                        } else if (o1.id > o2.id) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }

            }
        }).collect(Collectors.toList());
        System.out.println(collect1.size());
        collect1.forEach((node)->{
            System.out.println(node.id+" "+node.time+" "+node.cost);
        });
    }
    // 不下班 做完核酸的时间
    static int setNodesForFilter(Node node,int h1, int m1, int h2, int m2){
        int wayTime = dis2Time(node.dis); // 赶路时间。
        int queueTime = getFinalQueueNum(node, h1, m1,h2,m2 ); //排队时间
        int finalTime = getLeftTime(8, 0, h1, m1)+ wayTime+queueTime;
        if(finalTime < 720){
            node.selected = true;
        }
        node.time = wayTime + queueTime;
        return 0;
    };

    // 通过这个人到核酸检测点的额时间，计算这个点位的排队人数
    static int getFinalQueueNum(Node node, int h1, int m1, int h2, int m2){
        int  res = 0;
        int wayTime = dis2Time(node.dis); // 赶路时间。
        // todo 计算这段时间内 新增了多少排队的人。
        // 计算任意时间区间，新增的人数。
        // 1 计算开始时间在那个区间   左闭右开[ 8,10)
        // 2 计算结束时间在那个区间
        // 3
        int begin = changeTimeToInt(h1, m1);
        int end = begin + wayTime;
        int addPeople = getWayAddPeople(begin) - getWayAddPeople(end);

        return Math.max(addPeople + node.queueNum - wayTime*1, 0) ;
    }

    static int getWayAddPeople(int begin){
        int addPeople1=0;
        if(begin< 0){
            addPeople1 +=         3*120 + 2*60 + 120*10 + 240 + 120 *20;
        }
        else if(begin<120){
            addPeople1 += 3*(120-begin) + 2*60 + 120*10 + 240 + 120 *20;
        }else if(begin<240){
            addPeople1 +=           240-begin +  120*10 + 240 + 120 *20;
        } else if(begin<360){
            addPeople1 +=                (360-begin)*10 + 240 + 120 *20;
        } else if(begin < 600){
            addPeople1 +=                         (240-begin) + 120 *20;
        } else if(begin <720){
            addPeople1 +=                                 (120-begin) *20;
        }else {
            addPeople1 = Integer.MAX_VALUE;
        }
        return addPeople1;
    }

    static int changeTimeToInt(int h, int m){
        return getLeftTime(8, 0, h, m);
    }
    static int dis2Time(int dis){
        return 10*dis;
    }
    static int dis2Cost(int dis){
        return 10*dis;
    }

    static int getLeftTime(int h1, int m1, int h2, int m2){
        return 60*(h2-h1) + (m2-m1);
    }
}
/*
ab
cdeaabccd
4
ab
cdeaabccd
3
cacd
abcdf
 */

/*


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){
      //
        LCS();
    }

   public static void LCS(){
        Scanner scanner  = new Scanner(System.in);
        char[] chars1 = scanner.nextLine().toCharArray();
        char[] chars2 = scanner.nextLine().toCharArray();
        int[][] dp = new int[chars1.length + 1][chars2.length+1];
        Arrays.fill(dp[0], 0);
        String[][] sdp = new String[chars1.length + 1][chars2.length+1];
        for(int i = 0; i < sdp.length; i++) {
          for(int j = 0; j < sdp[0].length; j++) {
            sdp[i][j] = "";
          }
        }
        for(int i = 1; i < chars1.length +1; i++) {
          for(int j = 1; j < chars2.length + 1; j++) {
            if(chars1[i-1] == chars2[j-1]){
                dp[i][j] = dp[i-1][j-1] + 1;
                sdp[i][j] = sdp[i-1][j-1] + chars1[i-1];
            }else{
//                 dp[i][j] = dp[i-1][j-1];
//                 sdp[i][j] = sdp[i-1][j-1];
                dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                sdp[i][j] = (dp[i-1][j] > dp[i][j-1]) ? sdp[i-1][j] : sdp[i][j-1];
            }
          }
        }
        for (int[] a : dp) {
//             System.out.println(Arrays.toString(a));
        }
//         System.out.println(dp[chars1.length ][chars2.length]);
       System.out.println(sdp[chars1.length ][chars2.length]);
    }
}

 */