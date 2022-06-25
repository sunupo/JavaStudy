package com.java.sjq.spring.jms.activeMQ;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import javax.jms.*;

/**
 * @ Description <a href="https://blog.csdn.net/weixin_44232093/article/details/124849400"> Maven使用ActiveMQ </a>
 * @ author          懒惰的星期六
 * @ create          2022-06-25 17:37:51
 */
public class ActiveMqConsumer {
    /*
     * 模式: 点对点
     * 需求: 接收消息
     * */

    @Test
    public void recvMessage() throws Exception{
        // 创建连接工厂  对象,指定连接地址 ,协议,ip,通信端口
        ConnectionFactory cf = new ActiveMQConnectionFactory(Constants.BROKEN_URL);
        // 从工厂中获取连接对象
        Connection connection = cf.createConnection();
        // 开启连接
        connection.start();
        // 从连接中获取session session存储回话数据
        // 参数一: true 使用AUTO_ACKNOWLEDGE之外的消息发送模式 自动确认模式
        // 参数二: 自动确认模式 AUTO_ACKNOWLEDGE
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // 指定消息空间
        Queue ningning = session.createQueue(Constants.QUEUE_NAME);

        // 指定消息消费者 并告知空间名称
        MessageConsumer consumer = session.createConsumer(ningning);

        // 接收消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                // 获取消息
                if (message instanceof TextMessage){
                    TextMessage msg = (TextMessage) message;
                    try {
                        String text = msg.getText();
                        System.out.println("接收消息: "+text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // 关闭资源
        consumer.close();
        session.close();
        connection.close();

    }
}


