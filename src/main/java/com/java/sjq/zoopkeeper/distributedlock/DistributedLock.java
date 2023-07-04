package com.java.sjq.zoopkeeper.distributedlock;

// Here is an example of implementing a distributed lock in Java using ZooKeeper:

import com.java.sjq.zoopkeeper.zkthrift.Register;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


public class DistributedLock {

    private final String lockPath;
    private final ZooKeeper zooKeeper;
    private String lockNodePath;
    private CountDownLatch lockAcquiredSignal = new CountDownLatch(1);

    private String subPrefix = "/lock_";


    public DistributedLock(String lockPath, ZooKeeper zooKeeper) throws InterruptedException, KeeperException {
        this.lockPath = lockPath;
        this.zooKeeper = zooKeeper;
        if(zooKeeper.exists(lockPath,false)==null){
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper.create(lockPath, lockPath.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new AsyncCallback.StringCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, String name) {
                    countDownLatch.countDown();
                }
            }, null);
            countDownLatch.await();
        }
    }

    public void acquireLock() throws KeeperException, InterruptedException {
        // Create the lock node as an ephemeral sequential node
        lockNodePath = zooKeeper.create(lockPath + subPrefix, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(Thread.currentThread().getId()+"acquireLock::"+"lockNodePath::"+lockNodePath);
        // Watch the previous node in the lockPath to see if we are the next node to acquire the lock
        String previousNodePath = getPreviousNode(lockNodePath);
        System.out.println(Thread.currentThread().getId()+"acquireLock::"+"previousNodePath::"+previousNodePath);
        Stat exists = zooKeeper.exists(previousNodePath, new LockWatcher(lockAcquiredSignal));
        if(exists!=null){
            // 如果当前节点存在上一个节点，
            // Wait until we have acquired the lock
            lockAcquiredSignal.await();
        }
        System.out.println(Thread.currentThread().getId()+"acquireLock::success");
    }

    public void releaseLock() throws KeeperException, InterruptedException {
        zooKeeper.delete(lockNodePath, -1);
    }

    private String getPreviousNode(String nodePath) throws KeeperException, InterruptedException {

        String[] nodePathParts = nodePath.split("/");
        String nodeNumber = nodePathParts[nodePathParts.length - 1];
        int nodeNumberInt = Integer.parseInt(nodeNumber.substring(subPrefix.length()));
        int previousNodeNumberInt = nodeNumberInt - 1;
        String previousNodePath = lockPath + subPrefix + String.format("%010d", previousNodeNumberInt);
        return previousNodePath;
    }

    private class LockWatcher implements Watcher {

        private final CountDownLatch lockAcquiredSignal;

        public LockWatcher(CountDownLatch lockAcquiredSignal) {
            this.lockAcquiredSignal = lockAcquiredSignal;
        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeDeleted) {
                lockAcquiredSignal.countDown();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new Register().getZKClient();
        DistributedLock lock = new DistributedLock("/my-lock-path", zooKeeper);
        lock.testDL();


    }
    public void testDL(){
        for(int i = 0; i < 4; i++) {
            //
            new Thread(()->{
                try {
                    myTestMethod();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (KeeperException e) {
                    throw new RuntimeException(e);
                }
            }, "thread::"+i).start();
        }

    }

    public void myTestMethod() throws InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new Register().getZKClient();
        DistributedLock lock = new DistributedLock("/my-lock-path", zooKeeper);
        try {
            lock.acquireLock();
            Thread.sleep(10000);
            // Do some critical section work here
        } finally {
            lock.releaseLock();
        }
    }
}


