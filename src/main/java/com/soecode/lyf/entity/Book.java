package com.soecode.lyf.entity;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * 图书实体
 */
public class Book implements HttpSessionBindingListener {

    private long bookId;// 图书ID

    private String name;// 图书名称

    private int number;// 馆藏数量

    private boolean enough;

    public boolean isEnough() {
        return enough;
    }

    public void setEnough(boolean enough) {
        this.enough = enough;
    }

    public Book() {
    }

    public Book(long bookId, String name, int number) {
        this.bookId = bookId;
        this.name = name;
        this.number = number;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Book [bookId=" + bookId + ", name=" + name + ", number=" + number + "]";
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        System.out.println("book valueBound:\t" + event.getName() + ":" + event.getValue());
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        System.out.println("book valueUnbound: \t" + event.getName() + ":" + event.getValue());

    }
}
