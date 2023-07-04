package com.java.sjq.spring.jms.activeMQ.queue.acknowledgeMode.dupsokack;

import com.java.sjq.spring.jms.activeMQ.Constants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Before;
import org.junit.Test;

import javax.jms.*;

import static org.apache.activemq.ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE;


public class ActiveMqProviderConsumer {
    public static final String MESSAGE = "Session.DUPS_OK_ACKNOWLEDGE 异常消息测试";

    @Before
    public void sendMessage() throws Exception{
        ConnectionFactory cf = new ActiveMQConnectionFactory(Constants.BROKEN_URL);
        Connection connection = cf.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue ningning = session.createQueue(Constants.QUEUE_NAME);
        MessageProducer producer = session.createProducer(ningning);
        TextMessage message = new ActiveMQTextMessage();
        for(int i = 0; i < 10; i++) {
            Thread.sleep(10);
            message.setText(MESSAGE+i);
            // 发送消息
            producer.send(message);

        }

               // 关闭资源
        producer.close();
        session.close();
        connection.close();

    }

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
        Session session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);

        Queue sessionQueue = session.createQueue(Constants.QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(sessionQueue);

        consumer.setMessageListener(message -> {
            // 获取消息
            if (message instanceof TextMessage){
                TextMessage msg = (TextMessage) message;
                String text="";
                try {
                    text = msg.getText();
                    if(text.equals(MESSAGE+3)){
                        throw new RuntimeException("模拟业务抛出异常 ");
                    }
                    System.out.println("接收消息: √"+text);
                } catch (JMSException e) {
                    System.out.println("接收消息: 异常×"+text);
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


