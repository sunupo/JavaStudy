package com.java.sjq.spring.jms.activeMQ.queue.base;



import com.java.sjq.spring.jms.activeMQ.Constants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import javax.jms.*;

/**
 * @ Description <a href="https://blog.csdn.net/weixin_44232093/article/details/124849400"> Maven使用ActiveMQ </a>
 * @ author          懒惰的星期六
 * @ create          2022-06-25 16:30:11
 */
public class MQProvider {

    /*
     * 模式: 点对点模式
     * 需求: 发送消息
     * */

    @Test
    public void sendMessage() throws Exception{
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

        // 设置消息发送的目标地 在acticeMQ消息服务器开辟空间并起名字
        Queue ningning = session.createQueue(Constants.QUEUE_NAME);

        // 指定消息发送者 并告知空间名称
        MessageProducer producer = session.createProducer(ningning);
        // 创建消息对象封装消息
        TextMessage message = new ActiveMQTextMessage();
        message.setText("发送点对点消息");

        // 发送消息
        producer.send(message);
        // 关闭资源
        producer.close();
        session.close();
        connection.close();

    }
}

