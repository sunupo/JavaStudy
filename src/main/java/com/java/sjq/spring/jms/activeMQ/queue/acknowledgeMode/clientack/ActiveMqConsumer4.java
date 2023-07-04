package com.java.sjq.spring.jms.activeMQ.queue.acknowledgeMode.clientack;

import com.java.sjq.base.collection.hashmap.HashMapDemo;
import com.java.sjq.spring.jms.activeMQ.Constants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.junit.Test;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ActiveMqConsumer2 {
    public static final String MESSAGE = "Session.CLIENT_ACKNOWLEDGE 异常消息测试";

    /*
     * 模式: 点对点
     * 需求: 接收消息
     * */

    @Test
    public void recvMessage() throws Exception{
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Constants.BROKEN_URL);

        // 设置服务器最大重发消息给consumer的次数
        RedeliveryPolicy redeliverPolicy = new RedeliveryPolicy();
        redeliverPolicy.setMaximumRedeliveries(11); // 重试11次+最初的1次共12次。默认是6+1次。
        connectionFactory.setRedeliveryPolicy(redeliverPolicy);

        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        Queue sessionQueue = session.createQueue(Constants.QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(sessionQueue);

        Map<String,Boolean> map = new ConcurrentHashMap<>();
        map.put("flag", true);
        consumer.setMessageListener(message -> {
            // 获取消息
            if (message instanceof TextMessage){
                TextMessage msg = (TextMessage) message;
                try {
                    String text = msg.getText();
                    System.out.println("接收消息: "+text);
                    if(text.substring(text.length()-1).equals("5")){
                        map.put("flage", false);
                    }
                    if(map.get("flag")) {
                        msg.acknowledge();
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
        // 阻塞用于测试
        System.in.read();
        // 关闭资源
        consumer.close();
        session.close();
        connection.close();

    }
}


