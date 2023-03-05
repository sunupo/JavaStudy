package com.java.sjq.thrift.demo.tThreadSelectorServerDemo;

import com.java.sjq.thrift.demo.HelloWorldTService;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;

public class HelloClient  extends com.java.sjq.thrift.demo.tNonblockingServerDemo.HelloClient {
    public static void main(String[] args) {
        HelloClient client = new HelloClient();
        client.startClient("china",1949);
    }
}
