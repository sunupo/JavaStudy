package com.java.sjq.thrift.demo;

import org.apache.thrift.TException;

/**
 * @author sunupo
 */
public class HelloWorldImpl implements HelloWorldTService.Iface{
    @Override
    public String sayHello(String userName, int age) throws THelloWordServiceException, TException {
        if(userName.length()<2){
            throw new THelloWordServiceException(-1, "the character length of userName");
        }
        return "这是服务端返回的内容：hello\t"+ userName +"\t your age is "+age;
    }
}
