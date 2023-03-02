package com.java.sjq.base.JNDI.demo1;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;


public class JNDIDemo1 {
    /**
     *
     * @title initPerson
     * @description 绑定一个对象到JNDI服务上
     * @author hadoop
     * @throws Exception
     */
    public static void initPerson() throws Exception{
        //配置JNDI工厂和JNDI的url和端口。如果没有配置这些信息，将会出现NoInitialContextException异常
        LocateRegistry.createRegistry(3000);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty(Context.PROVIDER_URL, "rmi://localhost:3000");

//        初始化
        InitialContext ctx = new InitialContext();

        //实例化person对象
        Person p = new Person();
        p.setName("zc");
        p.setPassword("123");

        //将person对象绑定到JNDI服务中，JNDI的名字叫做：person。
        ctx.bind("person", p);
        ctx.close();
    }

    /**
     *
     * @title findPerson
     * @description 通过JNDI获得person对象
     * @author hadoop
     * @throws Exception
     */
    public static void findPerson() throws Exception{
        //因为前面已经将JNDI工厂和JNDI的url和端口已经添加到System对象中，这里就不用在绑定了
        InitialContext ctx = new InitialContext();

        //通过lookup查找person对象
        Person person = (Person) ctx.lookup("person");

        //打印出这个对象
        System.out.println(person.toString());
        ctx.close();
    }

    public static void main(String[] args) throws Exception {
        initPerson();
        findPerson();
    }
}