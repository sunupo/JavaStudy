package com.java.sjq.base.reference;

import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

public class WeakDemo {


    /**
     * output:
     * weak1: [Ljava.lang.Byte;@14514713
     * weak2: Optional.empty
     * ReferenceQueue 的 poll()方法是非阻塞的，而remove()方法是阻塞的。在使用ReferenceQueue时，应根据具体情况选择使用哪种方法。
     */
    @Test
    public   void testWeak(){
        ReferenceQueue<MyObject> queue = new ReferenceQueue<>();
        WeakReference<MyObject> weakReference = new WeakReference<MyObject>(new MyObject(1), queue);
        System.out.println("weak1: " + Objects.requireNonNull(weakReference.get()).toString());
        System.gc();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Optional<MyObject> optional = Optional.ofNullable(weakReference.get());
        System.out.println("weak2: " + optional.map(Object::toString));
        new Thread(()->{
            while (true){
                Reference<? extends MyObject> poll = queue.poll();
                System.out.println("clean thread: poll: " + poll);
                if(poll==null){
                    continue;
                }
                System.out.println("clean thread: poll.get(): " + poll.get());

            }
        }).start();
    }
    public static void main(String[] args) {

    }

    @Test
    public void testCleanMapEntry(){
        WeakCache<MyObject> cache = new WeakCache<>();
        final String key1= "1";
        cache.put(key1, new MyObject(Integer.parseInt(key1)));
        while(cache.get(key1)!=null){
            String s = new Random().nextInt()+"";
            cache.put(s, new MyObject(Integer.parseInt(s)));
//            System.out.println(cache.getCacheMap().size()+"\t"+cache.get(key1));
        }
        System.out.printf("key=%s 对应的弱引用已经被清除, cacheMap仍包含key？%s", key1, cache.getCacheMap().containsKey(key1));
        cache.getCacheMap().remove(key1);
        System.out.printf("key=%s 对应的弱引用已经被清除, cacheMap仍包含key？%s", key1, cache.getCacheMap().containsKey(key1));

    }
}








interface CacheItem{
    String getKey();
}

class WeakCache<T extends CacheItem> {
    // 创建一个Map对象，用于存储缓存数据
    private Map<String, WeakReference<T>> cacheMap = new HashMap<>();

    // 创建一个ReferenceQueue对象，用于存储被回收的弱引用
    private ReferenceQueue<T> referenceQueue = new ReferenceQueue<>();

    // 创建一个清理线程，用于清理被回收的弱引用
    private Thread cleanupThread = new Thread(() -> {
        while (true) {
            try {
                // 从referenceQueue中获取WeakReference对象
                WeakReference<T> weakReference = (WeakReference<T>) referenceQueue.remove();
                // 从缓存中删除对应的数据
                System.out.println("即将：\t" + weakReference);
                System.out.println("即将remove：\t" + weakReference.get()); // weakReference 的 get 也为空
                cacheMap.remove(weakReference.get().getKey());
            } catch (InterruptedException e) {
            // 处理异常
            }
        }
    });

    // 构造函数中启动清理线程
    public WeakCache() {
        cleanupThread.start();
    }

    // 添加数据到缓存中
    public void put(String key, T value) {
        // 创建MyObject对象，并将其包装成WeakReference对象，添加到cacheMap中
        cacheMap.put(key, new WeakReference<>(value, referenceQueue));
    }

    // 从缓存中获取数据
    public T get(String key) {
        // 从cacheMap中获取WeakReference对象
        WeakReference<T> weakReference = cacheMap.get(key);
        return weakReference != null ? weakReference.get() : null;
    }

    public Map<String, WeakReference<T>> getCacheMap() {
        return cacheMap;
    }
}

