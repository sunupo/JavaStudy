package com.java.sjq.base.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试 泛型-上界通配符 javac编译-jad反编译后的结果
 * @param <T>
 */
public class Node<T extends Comparable<T>> {

    private T data;
    private Node<T> next;

    public Node(T data, Node<T> next) {
        this.data = data;
        this.next = next;
    }

    public T getData() { return data; }
    // ...
    public static void main(String[] args){
      //
        Node<? extends Comparable> node = new Node<>(new Comparable() {
            @Override
            public int compareTo(Object o) {
                return 0;
            }
        }, null);
        Comparable c = node.getData();
    }
    public void fun(List<? super ArrayList> l){

    }
}
