package com.java.sjq.base.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
public class Demo {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("测试0----");
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ps aux|grep java"});
//                process.waitFor();
                InputStreamReader reader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                Thread.sleep(100);  // sleep
                while (bufferedReader.ready()){
                    System.out.println(bufferedReader.readLine());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("测试1----");
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ps aux|grep java"});
//                process.waitFor();
                InputStreamReader reader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                while (bufferedReader.ready()){
                    System.out.println(bufferedReader.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("测试2----");
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ps aux|grep java"});
                process.waitFor();
                InputStreamReader reader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                while (bufferedReader.ready()){
                    System.out.println(bufferedReader.readLine());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("测试3----");
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "ps aux|grep java"});
                InputStreamReader reader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line=bufferedReader.readLine()) != null){
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
