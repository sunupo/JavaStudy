package com.java.sjq.dataStructure.interview;

import java.net.Inet4Address;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sunupo
 */
public class Main {
    static class User{
        public String name;
        public int val;
        public User(String name , int val){
            this.name = name;
            this.val = val;

        }
    }
    public static void main(String[] args){
        Scanner s = new Scanner(System.in);
        String[] names = s.nextLine().split(",");

        HashMap<String , Integer> hmap = new HashMap<>();
        Set<User> set = new HashSet<User>();
        for(int i = 0; i < names.length; i++) {
            hmap.put(names[i], hmap.getOrDefault(names[i], 0)+1);
        }
        Iterator<Map.Entry<String, Integer>> iterator = hmap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Integer> entry= iterator.next();
            set.add(new User(entry.getKey(), entry.getValue()));
        }
        List<User> collect = set.stream().sorted(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (o1.val!=o2.val){
                    return o1.val -o2.val;
                }else{
                    char[] chars1 = o1.name.toCharArray();
                    char[] chars2 = o2.name.toCharArray();
//                    字母升序
                    return jud(chars1, chars2);
                }
            }
        }).collect(Collectors.toList());
        collect.stream().forEach((item)->{
            System.out.printf("%s %d,", item.name, item.val);
        });
    }
    public static int jud(char[] c1, char[] c2){
        int a=0,b=0;
        while(a<c1.length-1 && b<c2.length-1 && c1[a]==c2[b]){
            a++;
            b++;
        }
        return c1[a]-c2[b];
    }

}

