package com.java.sjq.redis;

import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicStampedReference;

public class RedisDemo {
    public static void main(String[] args){
        //
        //Connecting to Redis server on localhost
        Jedis jedis = new Jedis("localhost");
        System.out.println("Connection to server sucessfully");
        AtomicStampedReference atomicStampedReference;
        //check whether server is running or not
        jedis.set("name", "sunjingqin", "XX","EX", 1000 );
        jedis.set("name", "sunjingqin2", "XX","EX", 1000 );
        jedis.keys("*").stream().forEach((item)->{
            System.out.println(item);
            System.out.println(jedis.get(item));
        });
        System.out.println(""+jedis.ttl("name"));
        System.out.println(jedis.get("name"));
        System.out.println("Server is running: "+jedis.ping());


    }
}
