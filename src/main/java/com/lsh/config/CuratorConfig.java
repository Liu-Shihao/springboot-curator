package com.lsh.config;

import com.lsh.api.CuratorCacheApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: LiuShihao
 * @Date: 2022/10/26 17:04
 * @Desc:
 */
@Slf4j
@Configuration
public class CuratorConfig {

    public static String connectString ="192.168.153.131:2181";//集群用,隔开


    @Bean("curatorClient")
    public CuratorFramework curatorClient() throws Exception {
//        TestingServer testingServer = new TestingServer(2181, new File("zk-data"));

        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
//                .connectString(testingServer.getConnectString())//使用TestingServer模拟zk环境（无需部署zk环境）
                .connectString(connectString)
                .connectionTimeoutMs(15 * 1000)
                //会话超时时间
                .sessionTimeoutMs(60 * 1000)
                //设置重试机制
                .retryPolicy(new ExponentialBackoffRetry(10*1000,3))
                .build();
        curatorFramework.start();
        CuratorCacheApi curatorCacheApi = new CuratorCacheApi(curatorFramework);
        curatorCacheApi.curatorCacheListen("/");
        return curatorFramework;
    }
}
