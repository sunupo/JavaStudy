package com.java.sjq.base.juc.thread.threadlocal;

import javafx.collections.transformation.SortedList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;


public class Main {
    public static void main(String[] args) throws IOException {
//      ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
////      ThreadPoolExecutor  executor = new ThreadPoolExecutor();
//        System.out.println(System.getProperty("java.library.path"));
//        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\sunupo\\IdeaProjects\\JavaStudy\\src\\main\\java\\com\\java\\sjq\\base\\juc\\thread\\threadlocal\\SysInfo.java");
////        FileOutputStream fileOutputStream = new FileOutputStream("./SysInfo");
////        fileOutputStream.write();
//
//        int len;
//        byte[] b = new byte[1024];
//        while((len=fileInputStream.read(b))!=-1){
//            System.out.println(new String(b, 0, len));
////            System.out.println(new String(b));
//        }

        String[] keyName = {"daniel","daniel","daniel","luis","luis","luis","luis"};//{"leslie","leslie","leslie","clare","clare","clare","clare"};
        String[] keyTime = {"10:00","10:40","11:00","09:00","11:00","13:00","15:00"};//{"13:00","13:20","14:00","18:00","18:51","19:30","19:49"};
        System.out.printf(Arrays.toString(alertNames(keyName,keyTime).toArray()));


    }
    public static List<String> alertNames(String[] keyName, String[] keyTime) {
        if(keyName.length!=keyTime.length){
            return new ArrayList<>();
        }
        HashMap<String, List<String>> map = new HashMap<>();
        for(int i = 0; i < keyName.length; i++) {
          //
            if(map.get(keyName[i])==null){
                List<String> list = new ArrayList<>();
                list.add(keyTime[i]);
                map.put(keyName[i], list);
            }else {
                map.get(keyName[i]).add(keyTime[i]);
            }
        }
        Comparator<String> comparator = (o1, o2) -> 60*(Integer.parseInt(o1.substring(0,2))-Integer.parseInt(o2.substring(0,2)))
                +(Integer.parseInt(o1.substring(3,5))-Integer.parseInt(o2.substring(3,5)));
        map.forEach((k, v)-> {
           map.get(k).sort(comparator);
            });
        
        
//        Iterator<Set<Map.Entry<String, List<String>>>> entries = ;
        List<String> res = new ArrayList<>();
        Iterator<Map.Entry<String, List<String>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, List<String>> entry = iterator.next();
            if(judge(entry.getValue())){
               res.add(entry.getKey());
            }
        }
        String[] strs=new String[res.size()];
        res.toArray(strs);
        Arrays.sort(strs);

        res = new ArrayList<>(Arrays.asList(strs));

        return res;


    }
    public static boolean judge(List<String> list){
        if(list.size()< 3 ){
            return false;
        }
        for(int i = 0; i < list.size() - 2 ; i++) {
          //
            int h2 = Integer.parseInt(list.get(i+2).substring(0,2)),m2=Integer.parseInt(list.get(i+2).substring(3,5));
            int h1 = Integer.parseInt(list.get(i).substring(0,2)),m1=Integer.parseInt(list.get(i).substring(3,5));

            if(60*(h2-h1)+m2-m1<=60){
                return true;
            }
        }
        return false;

    }

}
