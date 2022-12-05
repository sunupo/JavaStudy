package com.java.sjq.base.proxy;

import sun.reflect.Reflection;

import java.util.Arrays;

public class UserServiceImpl implements UserService{
    public void add(int num){
        System.out.println("UserServiceImpl::add("+num+")");
    }
}
