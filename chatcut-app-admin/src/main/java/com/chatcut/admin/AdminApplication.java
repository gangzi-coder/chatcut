package com.chatcut.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "com.chatcut")
@Slf4j
public class AdminApplication {

    public static void main(String[] args) {
        log.info("============================= Starting AdminApplication =============================");
        SpringApplication.run(AdminApplication.class, args);
        log.info("============================= AdminApplication Started =============================");
    }
}
