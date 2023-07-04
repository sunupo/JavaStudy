package com.java.sjq.redis;

import org.redisson.Redisson;
import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import redis.clients.jedis.Jedis;

public class RedissonDemo {
    public static void main(String[] args){
      //
        RedissonClient redisClient =  Redisson.create();
        Jedis jedis = new Jedis();
//        RedissonLock lock = new RedissonLock();
        RLock lock1 = redisClient.getLock("");
        lock1.lock();
        lock1.unlock();
    }
}
