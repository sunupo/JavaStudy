package com.java.sjq.messagequeue.kafaka.noAutoCommit;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static com.java.sjq.messagequeue.kafaka.demo.Producer.TOPIC1;
import static com.java.sjq.messagequeue.kafaka.demo.Producer.TOPIC2;

/**
 * 消息消费者
 */
public class Consumer1 {

    public static void main(String[] args) {

        //添加配置信息
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        //设置分组
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"group2");

        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);


        //创建消费者
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        //订阅主题
        Collection<String> topics= new ArrayList<>();
        topics.add(TOPIC1);
        topics.add(TOPIC2);
        consumer.subscribe(topics);

        while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("record:\t"+record);
                System.out.println(record.value());
                System.out.println(record.key());
            }
        }

    }
}