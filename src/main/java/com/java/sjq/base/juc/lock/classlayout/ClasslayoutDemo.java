package com.java.sjq.base.juc.lock.classlayout;

import com.java.sjq.base.JNDI.User;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

/**
 * non-biasable
 * biasable 0x0000000000000005 (biasable; age: 0)
 * biased
 * thin
 * fat
 */

public class ClasslayoutDemo {
    User user =new User();
    public static void main(String[] args){
        ClasslayoutDemo demo = new ClasslayoutDemo();
//        demo.testHashcode1();
//        demo.testHashcode2();
        demo.testHashcode3();
//        demo.thinlock();


    }

    /**
     * 无锁状态，有 hash 不可偏向。(对象头默认没有hash，第一次调用hashcode方法的时候，对象头就会有hash了)
     *  加锁直接成为 thin lock
     */
    public  void  testHashcode1(){
        System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable()); //  (object header: mark)     0x0000000000000005 (biasable; age: 0) 转为二进制最后三位 101 无锁可偏向
        System.out.printf("hashcode:\t"+user.hashCode());
        System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable());  // (object header: mark)     0x00000077afea7d01 (hash: 0x77afea7d; age: 0) 转为二进制最后三位 001 无锁不可偏向
        synchronized (user){
            System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable()); // (object header: mark)     0x000000e5e39ff390 (thin lock: 0x000000e5e39ff390) 转为二进制最后三位 000 轻量级锁

        }

    }


    /**
     * 无锁状态，没有hash可偏向
     */
    public  void  testHashcode2(){
        System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable()); // (object header: mark)     0x0000000000000005 (biasable; age: 0) 转为二进制最后三位 101 无锁可偏向
        synchronized (user){
            System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable()); // (object header: mark)     0x00000202a1099805 (biased: 0x0000000080a84266; epoch: 0; age: 0) 转为二进制最后三位 101 是偏向锁

        }

    }

    /**
     * 偏向锁调用hashcode直接升级为重量级锁
     */
    public  void  testHashcode3(){

            User[] user = new User[10];

        System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable()); //  (object header: mark)     0x0000000000000005 (biasable; age: 0) 转为二进制最后三位 101 无锁可偏向
        synchronized (user){
            System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable()); //  (object header: mark)     0x000001ae779d8805 (biased: 0x000000006b9de762; epoch: 0; age: 0) 转为二进制最后三位 101 是偏向锁
            System.out.printf("hashcode:\t"+user.hashCode());
            System.out.println("t0\t"+ClassLayout.parseInstance(user).toPrintable()); // (object header: mark)     0x000001ae7b86e2da (fat lock: 0x000001ae7b86e2da) 转为二进制最后三位 010 是重量级锁

        }

    }

    /**
     * 看看对象头的 mark 尾部不同锁状态下的标志位.
     * jdk8偏向锁是默认开启，但是是有延时的，可通过参数： -XX:BiasedLockingStartupDelay=0关闭延时。
     */
    public void thinlock(){
        ClasslayoutDemo demo = new ClasslayoutDemo();
        System.out.println("t0\t"+ClassLayout.parseInstance(demo.user).toPrintable());
        Thread t1 = new Thread(()->{

            System.out.println("t1-0\t"+Thread.currentThread().getId()+"\n"+ClassLayout.parseInstance(user).toPrintable());
            synchronized (user){
                System.out.println("t1-1\t"+Thread.currentThread().getId()+"\n"+ClassLayout.parseInstance(user).toPrintable());

                user.setAddress("1");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread t2 = new Thread(()->{
            System.out.println("t2-0\t"+Thread.currentThread().getId()+"\n"+ClassLayout.parseInstance(user).toPrintable());
            synchronized (user){
                System.out.println("t2-1\t"+Thread.currentThread().getId()+"\n"+ClassLayout.parseInstance(user).toPrintable());

                user.setAddress("2");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t1.start();
        try {
//            Thread.sleep(100);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t2.start();
    }
}

/**
 * 对象头部的Class对象指针实际上指向一个klass c对象。 此对象表示此Java类的元数据信息
 * OFF  SZ               TYPE DESCRIPTION               VALUE
 *   0   8                    (object header: mark)     0x00000202a1099805 (biased: 0x0000000080a84266; epoch: 0; age: 0)
 *   8   4                    (object header: class)    0xf800c149
 *  12   4                int User.id                   0
 *  16   4   java.lang.String User.username             null
 *  20   4   java.lang.String User.sex                  null
 *  24   4     java.util.Date User.birthday             null
 *  28   4   java.lang.String User.address              null
 * Instance size: 32 bytes
 * Space losses: 0 bytes internal + 0 bytes external = 0 bytes total
 *
 *
 * Process finished with exit code 0
 */