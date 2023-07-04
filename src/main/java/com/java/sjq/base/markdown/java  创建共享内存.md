[(137条消息) Java的共享内存使用\_java创建共享内存\_meilidekcl的博客-CSDN博客](https://blog.csdn.net/meilidekcl/article/details/129131495)

在 Java 中创建[共享内存](https://so.csdn.net/so/search?q=%E5%85%B1%E4%BA%AB%E5%86%85%E5%AD%98&spm=1001.2101.3001.7020)可以使用 Java 的 NIO（New IO）库，通过使用 DirectByteBuffer 类和 FileChannel 类，可以将内存映射到文件上，从而实现共享内存的效果。

具体步骤如下：

1.  创建一个 RandomAccessFile 对象，用于创建共享内存所对应的文件。

2.  使用 FileChannel 的 map 方法，将文件映射到 DirectByteBuffer 对象上。

3.  将 DirectByteBuffer 对象传递给需要访问共享内存的进程，从而实现进程间共享内存。


代码示例：

```
package com.mappedbytebuffer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @projectName: RMITEST
 * @package: com.mappedbytebuffer
 * @className: multiThread
 * @author: Falin Luo
 * @description: TODO
 * @date: 2023/2/20 19:30
 * @version: 1.0
 */
public class multiThread {
    private static int size = 64;
    private static multiThread INSTANCE = null;
    FileChannel fl = null;
    RandomAccessFile raf = null;
    MappedByteBuffer map = null;
    public multiThread(){
        try{
            File file = new File("testMap-mutilThread.sm");
            if(!file.exists()){
                file.createNewFile();
            }
            this.raf = new RandomAccessFile(file, "rw");
            this.fl = this.raf.getChannel();
            this.map = this.fl.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized multiThread getInstance(){
        if(INSTANCE == null){
            INSTANCE = new multiThread();
        }
        return INSTANCE;
    }

    public void hit(int position){
    // 无锁操作
        // 对position位置操作
        this.map.position(position);
        byte[] b = new byte[1];
        this.map.get(b,0,1);
        // 加1操作
        System.out.println("byte num is : " + b[0]);
        b[0]++;
        this.map.position(position);
        this.map.put(b,0,1);
        this.map.force();
        return;
    }

    public synchronized void safeHit(int position){
    // 同步锁
        getInstance().hit(position);
        return;
    }
    public synchronized void go(int position){
    // 同步锁加文件锁
        FileLock fl = null;
        try {
            fl = this.fl.tryLock();
            getInstance().hit(position);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                assert fl != null;
                if(fl != null)
                fl.release();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 2000; i++) {
            new Thread(new mT()).start();
        }
    }
    static class mT extends Thread {
        @Override
        public void run() {
            getInstance().hit(10);
        }
    }
}

```

实际上我们写了三个方法，第一个没有加锁，第二个加了线程同步锁，第三个在第二个基础上加了文件锁。  
需要注意的是，MappedByteBuffer 的读写操作都是原子的，因此在多线程并发访问的时候，不需要加锁。

**多线程测试**

```
不使用同步锁，在线程切换中有数据丢失。
因为读写操作是原子的，指的是读和写单独的一个操作是原子的，在硬件上就是一条指令，不会被其他线程和进程干扰。
但是在这里，我们的操作是读和写一个作为一个一致性操作的事务，所以还是得同步。
byte num is : 28
byte num is : 29
byte num is : 30
byte num is : 30
byte num is : 31
byte num is : 32

当换成safeHit方法得时候，执行的时间明显变慢，但是不会出现数据丢失的情况
```

**多进程进程**

```
// 写一个新的进程，做的操作和前面是一样的
public class copyMutilThread {
    public static void main(String[] args) {
        for (int i = 0; i < 2000; i++) {
            new Thread(new mtl()).start();
        }
    }
    static class mtl extends Thread{
        @Override
        public void run() {
            multiThread.getInstance().safeHit(10);
        }
    }
}
```

```
当两个进程都在运行safeHit的时候，如果不对文件加锁，可能会造成数据丢失，也就是两个进程对同一个资源操作。
所以最保险的方式还是对文件加锁的同时，线程也要同步。
```
