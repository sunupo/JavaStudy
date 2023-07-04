package com.java.sjq.dataStructure.sort;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

// [(130条消息) 快速排序详解\_凉夏y的博客-CSDN博客](https://blog.csdn.net/LiangXiay/article/details/121421920)
// [C 排序算法 | 菜鸟教程](https://www.runoob.com/cprogramming/c-sort-algorithm.html)
public class QuickSort {
    boolean flag = false;

    public static void main(String[] args){
      //
        int[] arr = new int [20];
        Random random = new Random(2023);
        for(int i = 0; i < arr.length; i++) {
          arr[i] = random.nextInt(50);
        }
        System.out.println(Arrays.toString(arr));

        QuickSort quickSort = new QuickSort();
        quickSort.quickSort(arr, 0, arr.length -1);
        System.out.println(Arrays.toString(arr));
        Arrays.sort(arr);
        System.out.println(Arrays.toString(arr));
    }

    static void swap(int[] arr, int p, int q){
//        arr[p]^=arr[q];
//        arr[q]^=arr[p];
//        arr[q]^=arr[q];
        int tmp = arr[p];
        arr[p]=arr[q];
        arr[q]=tmp;
    }

    void quickSort(int[] arr, int start, int end){
        if(start >= end){
            return ;
        }
        int key = quickSortDetail2(arr, start, end);
        System.out.println("key:\t"+key);
        quickSort(arr, start, key-1);
        quickSort(arr, key+1, end);
    }

    int quickSortDetail(int[] arr, int start, int end){
        // 第一个元素为哨兵。 每次交换从右边开始，然后左边继续，注意等号比较在哪个循环。
        int sentinel = arr[start];
        int left = start, right = end;
        while(left<right){
            while (left<right && arr[right] > sentinel){
                right--;
            }
            while (left<right && arr[left]<=sentinel){ // 这儿必须是小于等于
                left++;
            }
            swap(arr, left, right);
        }
        swap(arr, start, left);
        return left;
    }

    int quickSortDetail2(int[] arr, int start, int end){
        // 最后一个元素为哨兵。 每次交换从左边开始，然后右边继续，注意等号比较在哪个循环。
        int sentinel = arr[end];
        int left = start, right = end;
        while(left<right){

            while (left<right && arr[left]<sentinel){
                left++;
            }
            while (left<right && arr[right] >= sentinel){ // 这儿必须是大于等于
                right--;
            }
            swap(arr, left, right);
        }
        swap(arr, end, left);
        return left;
    }

//    top-k
    int quickSortDetail3(int[] arr, int start, int end){
        // 第一个元素为哨兵。 每次交换从右边开始，然后左边继续，注意等号比较在哪个循环。
        int sentinel = arr[start];
        int left = start, right = end;
        while(left<right){
            while (left<right && arr[right] > sentinel){
                right--;
            }
            while (left<right && arr[left]<=sentinel){ // 这儿必须是小于等于
                left++;
            }
            swap(arr, left, right);
        }
        swap(arr, start, left);
        return left;
    }

    void quickSortTopK(int[] arr, int start, int end, int k){
        if(start>=end){
            return;
        }
        int i = quickSortDetail3(arr, start, end);
        System.out.println(i+"="+arr[i]);
        System.out.println(Arrays.toString(arr));
        if(i==k){

        }else if(i< k) {
            quickSortTopK(arr, i+1, end, k);
        }else{
            quickSortTopK(arr, start, i-1, k);
        }
    }
    @Test
    public void testTopK(){
        int[] arr = new int [20];
        Random random = new Random(2023);
        for(int i = 0; i < arr.length; i++) {
            arr[i] = random.nextInt(50);
        }
        System.out.println(Arrays.toString(arr));
        int topk = 5;
        if(arr.length<topk){
            System.out.println(arr.length - 1);
        }else{
            quickSortTopK(arr, 0, arr.length - 1, topk);
        }
        System.out.println(Arrays.toString(arr));

    }
}
