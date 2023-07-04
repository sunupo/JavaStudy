package com.java.sjq.base.markdown;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import sun.nio.ch.DirectBuffer;

import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// 加载log4j配置文件


 public class Singleton {
    private volatile static Singleton instance;
    private Singleton() {}
    public static Singleton getInstance() {
//        MappedByteBuffer
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    private static final Logger LOG = Logger.getLogger(Singleton.class);
    public  static void main(String[] args) {
        Map map = new HashMap();

        map.put("foo", "bar");
        map.put("baz", "biff");

        // 打印map中的值
        for (Object key : map.keySet()) {
            LOG.info(key + " = " + map.get(key));

        }
    }
}


