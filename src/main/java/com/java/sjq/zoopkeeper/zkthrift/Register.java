package com.java.sjq.zoopkeeper.zkthrift;

import com.java.sjq.thrift.demo.HelloWorldImpl;
import com.java.sjq.thrift.demo.HelloWorldTService;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author sunupo
 */
public class Register {
    private static final String ZK_SERVER = "localhost:2181";
    public static final String HELLO_SERVER_NAME = "/helloWorldService";

    private final static String SEPARATOR=":";
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,
            2,
            4,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            (Runnable r) -> new Thread(r, "MyThread--"+r),
            new ThreadPoolExecutor.AbortPolicy());

    public ZooKeeper getZKClient(){
        try {
            final CountDownLatch countDownLatch=new CountDownLatch(1);
            ZooKeeper zooKeeper=
                    new ZooKeeper(ZK_SERVER,4000, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if(Event.KeeperState.SyncConnected==event.getState()){
                                //如果收到了服务端的响应事件，连接成功
                                countDownLatch.countDown();
                            }
                        }
                    });
            countDownLatch.await();
            System.out.println(zooKeeper.getState());
            return zooKeeper;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public TServer startHelloWorldService(final int serverPort){
        try {
            System.out.println("HelloWorld TSimpleServer start ....");
            TProcessor tprocessor = new HelloWorldTService.Processor<HelloWorldTService.Iface>(new HelloWorldImpl());
            TServerSocket serverTransport = new TServerSocket(serverPort);  // transport
            TServer.Args tArgs = new TServer.Args(serverTransport);
            tArgs.processor(tprocessor);
            tArgs.protocolFactory(new TBinaryProtocol.Factory());
            TServer server = new TSimpleServer(tArgs);
            server.serve();
            System.out.println("HelloWorld TSimpleServer start success....");
            return server;
        } catch (Exception e) {
            System.out.println("Server start error!!!");
            e.printStackTrace();
            throw new RuntimeException("Server start error!!!");
        }
    }

    public void startAndRegisterService(ZooKeeper zooKeeper, Map.Entry<String, Integer>... ipPorts){
        if(ipPorts.length==0){
            System.out.println("ip port 不能为空");
            return;
        }

        try {
            if(zooKeeper.exists(HELLO_SERVER_NAME,false)==null){
                zooKeeper.create(HELLO_SERVER_NAME, HELLO_SERVER_NAME.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            for(int i = 0; i < ipPorts.length; i++) {
                final String ip = ipPorts[i].getKey();
                final int port = ipPorts[i].getValue();
                executor.execute(() -> {
                    try {
                        System.out.println("开始注册\t"+port);
                        System.out.println(zooKeeper.create(
                                HELLO_SERVER_NAME + HELLO_SERVER_NAME,
                                (ip + SEPARATOR + port).getBytes(),
                                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.EPHEMERAL_SEQUENTIAL));
                        System.out.println("完成注册\t"+port);

                        System.out.println("启动thrift服务\t"+port);
                        startHelloWorldService(port); // server 开始监听，阻塞

                    } catch (KeeperException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                });
            }
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
