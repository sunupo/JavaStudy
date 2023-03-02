package com.java.sjq.base.JNDI.demo1;

import java.io.Serializable;
import java.rmi.Remote;

public class Person implements Remote, Serializable {
    /**
     * @title long
     * @description TODO
     * @author hadoop
     * @param Person.java
     * @return TODO
     * @throws
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private String password;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String toString(){
        return "name:"+name+" password:"+password;
    }
}
