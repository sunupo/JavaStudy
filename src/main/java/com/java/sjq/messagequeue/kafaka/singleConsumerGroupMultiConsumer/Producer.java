package com.java.sjq.messagequeue.kafaka.multiConsumerGroup;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

        ArrayList<String> topics = new ArrayList<>();
        topics.add(TOPIC1);
        topics.add(TOPIC2);
        for (String topic : topics) {
            for(int i = 0; i < 2; i++) {
                //封装消息
                ProducerRecord<String,String> record = new ProducerRecord<String, String>(topic,"key0000"+i,"value hello kafka "+i);
                try {
                    //发送消息
                    Future<RecordMetadata> send = producer.send(record);
                    System.out.println("RecordMetadata:\t"+send.get(2, TimeUnit.SECONDS));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        //关系消息通道
        producer.close();
    }
}
