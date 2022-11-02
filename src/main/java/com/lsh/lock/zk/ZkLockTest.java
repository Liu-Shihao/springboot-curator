package com.lsh.lock.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: LiuShihao
 * @Date: 2022/11/2 16:03
 * @Desc:
 */
public class ZkLockTest {

    public static ZooKeeper getZkClient()throws Exception{
        String connectionString = "192.168.153.131:2181";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(connectionString,30*1000 , new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if(Event.KeeperState.SyncConnected==watchedEvent.getState()){
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
        return zooKeeper;
    }

    public static void main(String[] args) throws Exception {
        ZooKeeper zkClient = ZkLockTest.getZkClient();
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                try {
                    String threadName = Thread.currentThread().getName();
                    ZookeeperLock lock = new ZookeeperLock(zkClient,threadName);
                    lock.tryLock("lock");
                    Thread.sleep(2000);
                    lock.unLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        System.in.read();
    }

}
