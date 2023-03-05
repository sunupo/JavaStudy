package com.java.sjq.thrift.demo.tNonblockingServerDemo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.java.sjq.thrift.demo.HelloWorldTService;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import com.java.sjq.thrift.demo.HelloWorldTService.AsyncClient.sayHello_call;
public class HelloAsynClient {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 8090;
    public static final int TIMEOUT = 30000;
    public void startClient(String userName) {
        try {
            TAsyncClientManager clientManager = new TAsyncClientManager();
            TNonblockingTransport transport = new TNonblockingSocket(SERVER_IP, SERVER_PORT, TIMEOUT);
            TProtocolFactory tprotocol = new TBinaryProtocol.Factory();
            HelloWorldTService.AsyncClient asyncClient = new HelloWorldTService.AsyncClient(tprotocol, clientManager,
                    transport);

            System.out.println("Client start .....");
            CountDownLatch latch = new CountDownLatch(1);
            AsyncMethodCallback callBack = new AsynCallback(latch);
            System.out.println("call method sayHello start ...");
            asyncClient.sayHello(userName, 18, callBack);
            System.out.println("call method sayHello .... end");
            boolean wait = latch.await(30, TimeUnit.SECONDS);
            System.out.println("latch.await =:" + wait);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("startClient end.");
    }
    public class AsynCallback implements AsyncMethodCallback<String> {
        private CountDownLatch latch;
        public AsynCallback(CountDownLatch latch) {
            this.latch = latch;
        }
        @Override
        public void onComplete(String response) {
            System.out.println("onComplete");
                System.out.println("AsynCall result =:" + response);
                latch.countDown();
        }
        @Override
        public void onError(Exception exception) {
            System.out.println("onError :" + exception.getMessage());
            latch.countDown();
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        HelloAsynClient client = new HelloAsynClient();
        client.startClient("HelloAsynClientDemo");
    }
}

