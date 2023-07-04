package com.java.sjq.zoopkeeper.distributedlock;

// Here is an example of implementing a distributed lock in Java using ZooKeeper:

import com.java.sjq.zoopkeeper.zkthrift.Register;
import org.apache.zookeeper.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public class DistributedLock {

    private final String lockPath;
    private final ZooKeeper zooKeeper;
    private String lockNodePath;
    private CountDownLatch lockAcquiredSignal = new CountDownLatch(1);

    public DistributedLock(String lockPath, ZooKeeper zooKeeper) {
        this.lockPath = lockPath;
        this.zooKeeper = zooKeeper;
    }

    public void acquireLock() throws KeeperException, InterruptedException {
        // Create the lock node as an ephemeral sequential node
        lockNodePath = zooKeeper.create(lockPath + "/lock_", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        // Watch the previous node in the lockPath to see if we are the next node to acquire the lock
        String previousNodePath = lockPath + "/" + getPreviousNode(lockNodePath);
        zooKeeper.exists(previousNodePath, new LockWatcher(lockAcquiredSignal));

        // Wait until we have acquired the lock
        lockAcquiredSignal.await();
    }

    public void releaseLock() throws KeeperException, InterruptedException {
        zooKeeper.delete(lockNodePath, -1);
    }

    private String getPreviousNode(String nodePath) throws KeeperException, InterruptedException {
        String[] nodePathParts = nodePath.split("/");
        String nodeNumber = nodePathParts[nodePathParts.length - 1];
        int nodeNumberInt = Integer.parseInt(nodeNumber);
        int previousNodeNumberInt = nodeNumberInt - 1;
        String previousNodePath = lockPath + "/lock_" + String.format("%010d", previousNodeNumberInt);
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
        // To use this distributed lock, you would create a ZooKeeper instance and pass it to the constructor of DistributedLock:

        ZooKeeper zooKeeper = new Register().getZKClient();
        // To use the distributed lock, you would create an instance of DistributedLock and call acquireLock() to acquire the lock and releaseLock() to release it:

        DistributedLock lock = new DistributedLock("/my-lock-path", zooKeeper);
        try {
            lock.acquireLock();
            // Do some critical section work here
        } finally {
            lock.releaseLock();
        }

    }
}


