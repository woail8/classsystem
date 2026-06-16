package com.campuslearning.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服务端启动入口。
 * 当前版本先提供可运行的核心业务骨架，后续可继续接入 MySQL 与 Redis 持久化。
 */
@SpringBootApplication
public class CampusLearningServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusLearningServerApplication.class, args);
    }
}
