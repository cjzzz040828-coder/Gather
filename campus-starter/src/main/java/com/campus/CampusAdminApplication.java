package com.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * campus Admin 启动类
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CampusAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(com.campus.CampusAdminApplication.class, args);
    }
}
