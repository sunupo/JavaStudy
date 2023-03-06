package com.java.sjq.zoopkeeper.zkthrift;

import com.java.sjq.thrift.demo.HelloWorldTService;
import com.java.sjq.thrift.demo.THelloWordServiceException;
import org.apache.thrift.TConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author sunupo
 */
public class InvokeService {
    private static final String ZK_SERVER = "localhost:2181";
    public static final String HELLO_SERVER_NAME = "/helloWorldService";

    private final static String SEPARATOR=":";
    public static Map<String, Map<String, Integer>> serviceIpPortMapCache = new HashMap<>();

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
    public Map<String, Map<String, Integer>> queryAvailableService(ZooKeeper zooKeeper){
        final Map<String, Map<String, Integer>> availableService = new HashMap<>(10);
        try {
            zooKeeper.getData(HELLO_SERVER_NAME, true, new AsyncCallback.DataCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                    System.out.println("rc:" + rc +"\tpath:"+path+ "\tdata:"+new String(data)+"\tstate:"+stat);
                }
            }, null);
            List<String> subPaths = zooKeeper.getChildren(HELLO_SERVER_NAME, (e)->{
                // todo 服务的孩子发生变化
                System.out.println(e);
            }, null);
            System.out.printf(HELLO_SERVER_NAME+":\t"+Arrays.toString(subPaths.toArray())); // [helloWorldService0000000001, helloWorldService0000000000]
            Map<String, Integer> map = new HashMap<>();
            for (String path : subPaths) {
                String data = new String(zooKeeper.getData(HELLO_SERVER_NAME+"/"+path, (e)->{
                    // todo 临时节点数据变化 ip+port，需要更爱本地维护的map
                    System.out.println(e);
                }, null));
                System.out.println("subPath:\t"+data);
                map.put(data.split(SEPARATOR)[0], Integer.parseInt(data.split(SEPARATOR)[1]));
            }
            availableService.put(HELLO_SERVER_NAME, map);

        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return availableService;
    }

    /**
     * 可以设置一定的策略选择 ip，port
     * @param ipPortMap 某个服务所有的ip和端口
     */
    public Map.Entry<String, Integer> selectIpPort(Map<String, Integer> ipPortMap, ZooKeeper zooKeeper){
        Objects.requireNonNull(ipPortMap);
        Objects.requireNonNull(zooKeeper);
        Map.Entry<String, Integer> choose = null;
        for (Map.Entry<String, Integer>  entry: ipPortMap.entrySet()) {
            choose = entry;
        }
        return choose;

    }
    public String invokeService(final String ip, final int port, int timeout){
        TTransport transport = null;
        try {
            transport = new TSocket(TConfiguration.DEFAULT, ip,port, timeout);
            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            HelloWorldTService.Client client = new HelloWorldTService.Client(protocol);
            transport.open();
            String result = client.sayHello("sunjingin", 2);
            return result;
        } catch (TTransportException e) {
            e.printStackTrace();
            throw new RuntimeException("TTransportException");
        } catch (THelloWordServiceException e) {
            throw new RuntimeException(e);
        } catch (TException e) {
            throw new RuntimeException(e);
        }finally{
            if(!Objects.isNull(transport)){
                transport.close();
            }
        }

    }

    public static void main(String[] args){
      //
        InvokeService demo = new InvokeService();
        ZooKeeper zooKeeper = demo.getZKClient();
        serviceIpPortMapCache.putAll(demo.queryAvailableService(zooKeeper));
        Map.Entry<String, Integer> ipPortEntry = demo.selectIpPort(serviceIpPortMapCache.get(HELLO_SERVER_NAME), zooKeeper);
        System.out.println("使用的服务器是：\t"+ipPortEntry.getKey()+"\t"+ipPortEntry.getValue());
        String res = demo.invokeService(ipPortEntry.getKey(), ipPortEntry.getValue(), 30000);
        System.out.println(res);
    }

}
