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

# 面试题

## ZK分布式锁和Redis分布式锁到底该选谁？
redis分布式锁：
优点：性能高，能保证AP，保证其高可用，
缺点：正如Redisson的那篇文章所言，主要是如果出现主节点宕机，从节点还未来得及同步主节点的加锁信息，可能会导致重复加锁。虽然Redis官网提供了RedLock算法来解决这个问题，Redisson也实现了，但是RedLock算法其实本身是有一定的争议的，有大佬质疑该算法的可靠性；同时因为需要的机器过多，也会浪费资源，所以RedLock也不推荐使用。
zk分布式锁：
优点：zk本身其实就是CP的，能够保证加锁数据的一致性。每个节点的创建都会同时写入leader和follwer节点，半数以上写入成功才返回，如果leader节点挂了之后选举的流程会优先选举zxid（事务Id）最大的节点，就是选数据最全的，又因为半数写入的机制这样就不会导致丢数据
缺点：性能没有redis高
所以通过上面的对比可以看出，redis分布式锁和zk分布式锁的侧重点是不同的，这是redis和zk本身的定位决定的，redis分布式锁侧重高性能，zk分布式锁侧重高可靠性。所以一般项目中redis分布式锁和zk分布式锁的选择，是基于业务来决定的。如果你的业务需要保证加锁的可靠性，不能出错，那么zk分布式锁就比较符合你的要求；如果你的业务对于加锁的可靠性没有那么高的要求，那么redis分布式锁是个不错的选择。



