package com.lsh;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: LiuShihao
 * @Date: 2022/10/26 17:01
 * @Desc:
 */
@Slf4j
@SpringBootApplication
public class CuratorApplication {
    public static void main(String[] args) {
        log.info("==================App Start======================");
        SpringApplication.run(CuratorApplication.class,args);
        log.info("==================App End========================");
    }
}
