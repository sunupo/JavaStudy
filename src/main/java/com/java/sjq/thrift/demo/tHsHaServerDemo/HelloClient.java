package com.java.sjq.thrift.demo.tNonblockingServerDemo;

import com.java.sjq.thrift.demo.HelloWorldTService;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;

public class HelloClient {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 8090;
    public static final int TIMEOUT = 30000;
    public void startClient(String userName, Integer age) {
        TTransport transport = null;
        try {
            transport = new TFramedTransport(new TSocket(new TConfiguration(),SERVER_IP, SERVER_PORT, TIMEOUT));
            // 协议要和服务端一致
            TProtocol protocol = new TBinaryProtocol(transport);
            // TProtocol protocol = new TCompactProtocol(transport);
            // TProtocol protocol = new TJSONProtocol(transport);
            HelloWorldTService.Client client = new HelloWorldTService.Client(protocol);
            transport.open();
            String result = client.sayHello(userName, age);
            System.out.println("Thrify client result =: " + result);
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
    public static void main(String[] args) {
        HelloClient client = new HelloClient();
        client.startClient("china",1949);
    }
}
