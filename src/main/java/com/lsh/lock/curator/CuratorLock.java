package com.lsh.lock.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @Author: LiuShihao
 * @Date: 2022/10/31 17:46
 * @Desc: 使用Curator InterProcessLock 实现分布式锁（公平锁）
 * 实现Zookeeper分布式锁，主要是基于Zookeeper的 临时序列节点来实现的。
 * 1. 临时节点，指的是节点创建后，如果创建节点的客户端和 Zookeeper 服务端的会话失效(例如断开连接)，那么节点就会被删除。
 * 2. 持久节点指的是节点创建后，即使创建节点的客户端和 Zookeeper 服务端的会话失效(例如断开连接)，节点也不会被删除，只有客户端主动发起删除节点的请求，节点才会被删除。
 * 3. 序列节点，这种节点在创建时会有一个序号，这个序号是自增的。序列节点既可以是临时序列节点，也可以是持久序列节点。
 *
 * 临时序列实现分布式锁原理：
 * 当客户端来加锁的时候，会先在加锁的节点下建立一个子节点，这个节点就有一个序号，类似 lock-000001 ，
 * 创建成功之后会返回给客户端所创建的节点，然后客户端会去获取这个加锁节点下的所有客户端创建的子节点，当然也包括自己创建的子节点。
 * 拿到所有节点之后，给这些节点进行排序，然后判断自己创建的节点在这些节点中是否排在第一位，
 * 如果是的话，那么就代表当前客户端就算加锁成功了，如果不是的话，那么就代表当前客户端加锁失败。
 * 加锁失败的节点并不会不停地循环去尝试加锁，而是在自己创建节点的前一个节点上加一个监听器，然后就进行等待。
 * 当前面一个节点释放了锁，就会反过来通知等待的客户端，然后客户端就加锁成功了。
 *
 * 从这里可以看出redis和zk防止死锁的实现是不同的，redis是通过过期时间来防止死锁，而zk是通过临时节点来防止死锁的。
 *
 * 为什么使用顺序节点？其实为了防止羊群效应。
 * 如果没有使用顺序节点，假设很多客户端都会去加锁，那么加锁就会都失败，都会对加锁的节点加个监听器，
 * 那么一旦锁释放，那么所有的加锁客户端都会被唤醒来加锁，那么一瞬间就会造成很多加锁的请求，增加服务端的压力。
 *
 * zk实现的分布式锁是公平的吗？
 * 其实使用临时顺序节点实现的分布式锁就是公平锁。所谓的公平锁就是加锁的顺序跟成功加锁的顺序是一样的。
 * 因为节点的顺序就是被唤醒的顺序，所以也就是加锁的顺序，所以天生就是公平锁。
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
