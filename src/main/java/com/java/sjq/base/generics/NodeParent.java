package com.java.sjq.base.generics;



public class NodeParent<T> {
    private T data;
    public NodeParent(T data) {
        this.data = data;
    }
    public void setData(T data) {
        System.out.println("Node.setData");
        this.data = data;
    }
}
