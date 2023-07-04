package com.java.sjq.base.collection.copyonwrite;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;




public class COW {
    public static void main(String[] args){

// Create a CopyOnWriteArrayList object
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

// Add elements to the list
        list.add("apple");
        list.add("banana");
        list.add("orange");

// Print the list
        System.out.println(list);

// Output: [apple, banana, orange]
    }

    public void testCOW() {
        // The following code shows the CopyOnWrite collections available in Java
        CopyOnWriteArrayList<String> cowArrayList = new CopyOnWriteArrayList<>();
        cowArrayList.add("");
        CopyOnWriteArraySet<String> cowArraySet = new CopyOnWriteArraySet<>();
        cowArraySet.add("");
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();
        chm.put("","");
        ConcurrentSkipListMap<String, String> cslm = new ConcurrentSkipListMap<>();
        cslm.put("","");
        ConcurrentSkipListSet<String> csls = new ConcurrentSkipListSet<>();
    }
}
