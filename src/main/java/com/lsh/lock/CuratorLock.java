package com.lsh.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @Author: LiuShihao
 * @Date: 2022/10/31 17:46
 * @Desc: 使用Curator InterProcessLock 实现分布式锁
 */
@Slf4j
public class CuratorLock {


    public CuratorFramework getCuratorFramework(){
        String connectionString = "192.168.153.131:2181";
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3,Integer.MAX_VALUE);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        curatorFramework.start();
        return curatorFramework;
    }

    public static void main(String[] args) throws Exception {
        String lock = "/lock";
        //获得两个客户端
        CuratorFramework client1 = new CuratorLock().getCuratorFramework();
        CuratorFramework client2 = new CuratorLock().getCuratorFramework();


        final InterProcessLock lock1 = new InterProcessMutex(client1, lock);
        final InterProcessLock lock2 = new InterProcessMutex(client2, lock);
        //模拟两个线程
        new Thread(() -> {
            try {
                //线程加锁
                lock1.acquire();
                System.out.println("线程1获取锁");

                //线程沉睡
                Thread.sleep(5*1000);

                //线程解锁
                lock1.release();
                System.out.println("线程1释放了锁");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

        //线程2
        new Thread(() -> {
            //线程加锁
            try {
                lock2.acquire();
                System.out.println("线程2获取到锁");

                //线程沉睡
                Thread.sleep(5*1000);

                lock2.release();
                System.out.println("线程2释放锁");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }
}
