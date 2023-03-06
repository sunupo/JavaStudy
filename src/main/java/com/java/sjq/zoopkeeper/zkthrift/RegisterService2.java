package com.java.sjq.zoopkeeper.zkthrift;

import org.apache.zookeeper.ZooKeeper;

import java.util.AbstractMap;
import java.util.Map;

/**
 * @author sunupo
 */
public class RegisterService2 {
    public static void main(String[] args){
        Register demo = new Register();
        ZooKeeper zooKeeper = demo.getZKClient();
        Map.Entry<String, Integer> ipPort1 = new AbstractMap.SimpleEntry<>("localhost",8092);
        Map.Entry<String, Integer> ipPort2 = new AbstractMap.SimpleEntry<>("localhost",8093);
        demo.startAndRegisterService(zooKeeper, ipPort1, ipPort2);
    }

}
