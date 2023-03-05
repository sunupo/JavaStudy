package com.java.sjq.thrift.demo.tHsHaServerDemo;

import com.java.sjq.thrift.demo.HelloWorldImpl;
import com.java.sjq.thrift.demo.HelloWorldTService;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.layered.TFramedTransport;

public class HelloServer {
    public static final int SERVER_PORT = 8090;
    public void startServer() {
        try {
            System.out.println("HelloWorld TSimpleServer start ....");
            TServerSocket serverTransport = new TServerSocket(SERVER_PORT);  // transport
            TServer.Args tArgs = new TServer.Args(serverTransport);
            tArgs.transportFactory(new TFramedTransport.Factory());


            TProcessor tprocessor = new HelloWorldTService.Processor<HelloWorldTService.Iface>(new HelloWorldImpl());
            tArgs.processor(tprocessor);

            tArgs.protocolFactory(new TBinaryProtocol.Factory());
            // tArgs.protocolFactory(new TCompactProtocol.Factory());
            // tArgs.protocolFactory(new TJSONProtocol.Factory());


            TServer server = new THsHaServer(tArgs);
            server.serve();
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        HelloServer server = new HelloServer();
        server.startServer();
    }
}


