package com.java.sjq.messagequeue.kafaka.singleConsumerGroupMultiConsumer;


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

    public static void main(String[] args) {
        com.java.sjq.messagequeue.kafaka.multiConsumerGroup.Producer.startProduce();
    }
}
