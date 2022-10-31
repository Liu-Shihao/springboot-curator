package com.lsh.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;

/**
 * @Author: LiuShihao
 * @Date: 2022/10/31 14:09
 * @Desc: Zookeeper Node Listen
 */
@Slf4j
public class CuratorCacheApi {

    private CuratorFramework curatorFramework;

    public CuratorCacheApi(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    public void nodeCacheListen(String path) throws Exception {
        /**
         * @curator 4.0.1
         * @NodeCache：监听节点对应增、删、改操作，无法监听到子节点事件
         */
        NodeCache nodeCache = new NodeCache(curatorFramework, path);
        nodeCache.start();
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                log.info("-------------nodeCache-----------------");
                log.info("path:{}",nodeCache.getPath());
                log.info("currentData:{}",new String(nodeCache.getCurrentData().getData()));
                log.info("---------------------------------------");
            }
        });
    }

    public void pathChildrenCacheListen(String path) throws Exception {
        /**
         * @curator 4.0.1
         * @PathChildrenCache：监听节点下一级子节点的增、删、改操作
         * 1.无法对监听路径所在节点进行监听(即不能监听path对应节点的变化)
         * 2.只能监听path对应节点下一级目录的子节点的变化内容(即只能监听/path/node1的变化，而不能监听/path/node1/node2 的变化)
         */
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, path,true);
        pathChildrenCache.start();
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                log.info("-------------pathChildrenCache-----------------");
                String type = event.getType().name();
                log.info("event type:{}",type);
                if (type.equals("CHILD_ADDED")||type.equals("CHILD_UPDATED")||type.equals("CHILD_REMOVED")){
                    log.info("path:{}",event.getData().getPath());
                    log.info("data:{}",new String(event.getData().getData()));
                }
                log.info("---------------------------------------");

            }
        });
    }
    /**
     * @curator 4.0.1
     * @TreeCache：可以将指定的路径节点作为根节点，对节点及其所有的子节点操作进行监听
     */
    public void treeCacheListen(String path) throws Exception {

        TreeCache treeCache = new TreeCache(curatorFramework, path);
        treeCache.start();
        treeCache.getListenable().addListener(new TreeCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                log.info("-------------treeCache-----------------");
                String type = event.getType().name();
                log.info("event type:{}",type);
                if (type.equals("NODE_ADDED")||type.equals("NODE_UPDATED")||type.equals("NODE_REMOVED")){
                    log.info("path:{}",event.getData().getPath());
                    log.info("data:{}",new String(event.getData().getData()));
                }
                log.info("---------------------------------------");
            }
        });

    }

    /**
     * Current API
     * curator 5.1.0：NODE_CREATED、NODE_CHANGED、NODE_DELETED
     */
    public void curatorCacheListen(String path){
        CuratorCache curatorCache = CuratorCache.build(curatorFramework, path);
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(CuratorCacheListener.Type type, ChildData oldData, ChildData data) {

                if (type.name().equals(Type.NODE_CREATED.name())){
                    //NODE_CREATED（注意：创建节点时，oldData为null）
                    log.info(">>> :{} --> {}",data.getPath(),type.name());
                    //TODO...
                }else if (type.name().equals(Type.NODE_CHANGED.name())){
                    //NODE_CHANGED
                    log.info(">>> :{} --> {}",data.getPath(),type.name());
                    //TODO...
                }else {
                    //NODE_DELETED （注意：删除节点时，data为null）
                    log.info(">>> :{} --> {}",oldData.getPath(),type.name());
                    //TODO...
                }
            }
        });
        curatorCache.start();
    }
}
