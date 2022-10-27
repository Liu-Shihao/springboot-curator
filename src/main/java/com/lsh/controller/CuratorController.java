package com.lsh.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LiuShihao
 * @Date: 2022/10/26 21:40
 * @Desc:
 */
@RestController
@RequestMapping("/zk")
@Slf4j
public class CuratorController {

    @Autowired
    CuratorFramework curatorClient;

    @GetMapping("/create")
    public String createNode(String path,String data) throws Exception {
        curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,data.getBytes());
        return "success";
    }
    @GetMapping("/update")
    public String updateNode(String path,String data) throws Exception {
        curatorClient.setData().forPath(path,data.getBytes());
        return "success";
    }
    @GetMapping("/delete")
    public String delete(String path) throws Exception {
        curatorClient.delete().forPath(path);
        return "success";
    }
}
