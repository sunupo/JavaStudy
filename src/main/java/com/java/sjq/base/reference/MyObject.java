package com.java.sjq.base.reference;

public class MyObject implements CacheItem{
    int key;

    public MyObject(int val) {
        this.key = val;
    }

    @Override
    public String getKey() {
        return this.key+"";
    }

    @Override
    public String toString() {
        return "MyObject{" +
                "val=" + key +
                '}';
    }
}