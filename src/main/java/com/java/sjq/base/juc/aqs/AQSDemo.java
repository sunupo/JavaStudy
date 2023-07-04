package com.java.sjq.base.juc.aqs;

import com.java.sjq.base.juc.thread.printABC.ReentrantLockCondition;
import org.apache.kafka.common.utils.CopyOnWriteMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.stream.Collectors;

public class AQSDemo {
    AbstractQueuedSynchronizer aqs = new AbstractQueuedSynchronizer() {

    };

    public static void main(String[] args){
        nthUglyNumber(20);
      //
    }
    static int nthUglyNumber(int n) {
        // 可以理解为三个指向有序链表头结点的指针
        int p2 = 1, p3 = 1, p5 = 1; // todo 定义三个指针 p2,p3,p5 表示下一个丑数是当前指针指向的丑数乘以对应的质因数。初始时，三个指针的值都是 111。
        // 可以理解为三个有序链表的头节点的值
        int product2 = 1, product3 = 1, product5 = 1;
        // 可以理解为最终合并的有序链表（结果链表）
        int[] ugly = new int[n + 1];
        // 可以理解为结果链表上的指针
        int p = 1;

        // 开始合并三个有序链表，找到第 n 个丑数时结束
        while (p <= n) {
            // 取三个链表的最小结点
            int min = Math.min(Math.min(product2, product3), product5);
            System.out.printf("min:%d\n,%d,%d,%d\t---%d,%d,%d\n",min,product2,product3, product5, p2,p3,p5);
            // 将最小节点接到结果链表上
            ugly[p] = min;
            System.out.println(Arrays.toString(Arrays.stream(ugly).filter((i)->i>=0).toArray()));
            System.out.println();
            p++;
            // 前进对应有序链表上的指针
            if (min == product2) {
                product2 = 3 * ugly[p2];
                p2++;
            }
            if (min == product3) {
                product3 = 5 * ugly[p3];
                p3++;
            }
            if (min == product5) {
                product5 = 7 * ugly[p5];
                p5++;
            }
        }
        // 返回第 n 个丑数
        return ugly[n];
    }
}
