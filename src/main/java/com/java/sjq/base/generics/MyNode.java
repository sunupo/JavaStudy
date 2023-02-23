package com.java.sjq.base.generics;

//public class NodeParent<T> {
//    private T data;
//    public NodeParent(T data) {
//        this.data = data;
//    }
//    public void setData(T data) {
//        System.out.println("Node.setData");
//        this.data = data;
//    }
//}


public class MyNode extends NodeParent<Integer> {
    public MyNode(Integer data) { super(data); }

    public void setData(Integer data) {
        System.out.println("MyNode.setData");
        super.setData(data);

    }

    public static void main(String[] args){
        //
        MyNode mn = new MyNode(5);
//        Node n = mn;            // A raw type - compiler throws an unchecked warning
//        n.setData("Hello");     // Causes a ClassCastException to be thrown.
//        Integer x = mn.data;
    }
}
