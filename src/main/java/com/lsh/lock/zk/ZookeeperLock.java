package com.lsh.lock.zk;

import lombok.Data;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: LiuShihao
 * @Date: 2022/11/2 15:38
 * @Desc: 直接使用Zookeeper原生API实现分布式锁
 */
@Data
public class ZookeeperLock implements Watcher,AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback, AsyncCallback.StatCallback {
    ZooKeeper zk;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    String lockName ;
    String threadName;
    String lockRoot = "/lock";

    public ZookeeperLock(ZooKeeper zk, String threadName) {
        this.zk = zk;
        this.threadName = threadName;
    }
    /**
     * 使用异步方式创建临时序列节点，阻塞
     */
    public void tryLock(String name){
        try {
            zk.create(lockRoot+"/"+name,threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,this,threadName);
            countDownLatch.await();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 删除临时节点，触发watch delete事件
     */
    public void unLock(){
        try {
            zk.delete(lockRoot+lockName,-1);
            System.out.println(threadName + " over work....");
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }
    /**
     * create callback
     * @param rc
     * @param path
     * @param ctx
     * @param name
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name != null){
            System.out.println(ctx.toString()+" create path: "+ name);
            lockName = name.substring(5);
            zk.getChildren(lockRoot, false, this, ctx );
        }
    }
    /**
     * getChildren callback
     * @param rc
     * @param path
     * @param ctx
     * @param children
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        Collections.sort(children);
        int i = children.indexOf(lockName.substring(1));
        if(i == 0){
            //是当前第一位置, countDownLatch放行
            System.out.println(threadName +" i am first....");
            try {
                zk.setData(lockRoot,threadName.getBytes(),-1);
                countDownLatch.countDown();
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            //不是，检查前一个是否还存在？
            zk.exists(lockRoot+"/"+children.get(i-1),this,this,"abc");
        }
    }

    /**
     * exists callback
     * @param rc
     * @param path
     * @param ctx
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        //todo
    }

    /**
     * watch event callback
     * 如果第一线程，锁释放了，其实只有第二个节点收到了回调事件！！
     * 如果不是第一个节点主动释放锁，而是某一个节点挂了（session断开连接，临时节点自动被删除），也能造成后边的节点收到这个通知，从而让他后边的节点那个跟去watch挂掉这个节点前边的
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        System.out.println("watch event: "+event.getPath()+" "+event.getType());
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren(lockRoot,false,this ,"abc");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }
}