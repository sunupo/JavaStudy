package com.java.sjq.base.iterateAnyMap;

import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;

public class IterationDemo {
    public static void main(String[] arg)
    {

        Map<String,String> gfg = new HashMap<String,String>();

        // enter name/url pair
        gfg.put("GFG", "geeksforgeeks.org");
        gfg.put("Practice", "practice.geeksforgeeks.org");
        gfg.put("Code", "code.geeksforgeeks.org");
        gfg.put("Quiz", "www.geeksforgeeks.org");

        // using for-each loop for iteration over Map.entrySet()
        for (Map.Entry<String,String> entry : gfg.entrySet()) {
            System.out.println("Key = " + entry.getKey() +
                    ", Value = " + entry.getValue());
        }

        System.out.println(Float.floatToIntBits(0.0f));
        System.out.println(Float.floatToIntBits(-0.0f));
        System.out.println(Float.floatToIntBits(0));
        System.out.println(0.0f==-0.0f);
        System.out.println((float)0.0f==(float)-0.0f);
        System.out.println(new Float(0.0f).equals((float)-0.0f));
    }
}
