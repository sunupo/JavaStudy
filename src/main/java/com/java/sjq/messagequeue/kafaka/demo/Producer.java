package com.java.sjq.messagequeue.kafaka.demo;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * 消息生产者
 * @author sunupo
 */
public class Producer {

    public static final String TOPIC1 = "black-topic";
    public static final String TOPIC2 = "white-topic";

    public static void main(String[] args) {

        //添加kafka的配置信息
        Properties properties = new Properties();
        //配置broker信息
        properties.put("bootstrap.servers","127.0.0.1:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.RETRIES_CONFIG,10);

        //生产者对象
        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        //封装消息
        ProducerRecord<String,String> record1 = new ProducerRecord<String, String>(TOPIC1,"00001","hello kafka 1 !");
        ProducerRecord<String,String> record2 = new ProducerRecord<String, String>(TOPIC2,"00002","hello kafka 2 !");
        //发送消息
        try {
            Future<RecordMetadata> send = producer.send(record1);
            RecordMetadata recordMetadata = send.get();
            System.out.println("get result1 \t"+recordMetadata);
            System.out.println("get result2 \t"+producer.send(record2).get());

        }catch (Exception e){
            e.printStackTrace();
        }

        //关系消息通道
        producer.close();
    }
}
