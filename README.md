# Curator 5.1 用法Demo

Apache Curator是Apache ZooKeeper的高级Java客户端。它提供了许多有用的分布式系统解决方案，包括Elections、Locks、Barriers、Counters、Catches、Nodes/Watches、Queues。
所有依赖Zookeeper的Apache项目，都使用Apache Curator，如：Hadoop、Flink、HBase等等。

# 版本
SpringBoot：2.6.7<br>
Curator：5.1.0<br>
Zookeeper使用curator-test的TestingServer进行模拟。<br>
# 一、监听
在Curator低版本（4.0.1）中，使用了`NodeCache`、`PathChildrenCache`和`TreeCache`

```java
  NodeCache nodeCache = new NodeCache(client, "/");
        nodeCache.start();
        nodeCache.getListenable().addListener(new NodeCacheListener() {

            @Override
            public void nodeChanged() throws Exception {
                System.out.println("=======节点改变===========");
                String path = nodeCache.getPath();
                String currentDataPath = nodeCache.getCurrentData().getPath();
                String currentData = new String(nodeCache.getCurrentData().getData());
                Stat stat = nodeCache.getCurrentData().getStat();
                System.out.println("path:"+path);
                System.out.println("currentDataPath:"+currentDataPath);
                System.out.println("currentData:"+currentData);
            }
        });

```
```java
 PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/",true);
        pathChildrenCache.start();
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println("=======节点子节点改变===========");
                PathChildrenCacheEvent.Type type = event.getType();
                String childrenData = new String(event.getData().getData());
                String childrenPath = event.getData().getPath();
                Stat childrenStat = event.getData().getStat();
                System.out.println("子节点监听类型："+type);
                System.out.println("子节点路径："+childrenPath);
                System.out.println("子节点数据："+childrenData);
                System.out.println("子节点元数据："+childrenStat);

            }
        });
```



在5.1.0版本中`NodeCache`和`PathChildrenCache`已经被弃用。使用新的`org.apache.curator.framework.recipes.cache.CuratorCacheListener`和
`org.apache.curator.framework.recipes.cache.CuratorCache`实现。
```java
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData childData, ChildData childData1) {
                log.info(">>> :{} --> {}",childData.getPath(),type.name());
                if (type.name().equals(Type.NODE_CREATED.name())){
                    //TODO...
                }else if (type.name().equals(Type.NODE_CHANGED.name())){
                    //TODO...
                }else {
                    //NODE_DELETED
                    //TODO...
                }
            }
        });
        curatorCache.start();
```

# 二、 异步


# 三、事务





