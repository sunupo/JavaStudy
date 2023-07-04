package com.java.sjq.dataStructure;

import java.util.*;


public class Main0033 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int n = scanner.nextInt();
            List<Map<Integer, Integer>> res = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Map<Integer, Integer> map = new TreeMap<>();
                for (int j = 0; j < 3; j++) {
                    map.put(scanner.nextInt(), j);
                }
                res.add(map);
            }
            solution(res);
        }
    }

    private static void solution(List<Map<Integer, Integer>> res) {

        Integer res1 = new ArrayList<>(res.get(0).keySet()).get(0);
        System.out.println(res.get(0).keySet());
        int sum = res1;
        Integer type = res.get(0).get(res1);

        if (res.size() > 1) {
            for (int i = 1; i < res.size(); i++) {
                List<Integer> keyList = new ArrayList<>(res.get(i).keySet());
                System.out.println(res.get(i).keySet());
                Integer resN = keyList.get(0);
                Integer typeN = res.get(i).get(resN);
                if (!typeN.equals(type)) {
                    sum += resN;
                    type = typeN;
                } else {
                    sum += keyList.get(1);
                    type = res.get(i).get(keyList.get(1));
                }

            }
        }


        System.out.print(sum);
    }
}
