package com.chatcut.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.chatcut.gateway", "com.chatcut.common.security"})
@Slf4j
public class GatewayApplication {

    public static void main(String[] args) {
        log.info("============================= Starting GatewayApplication =============================");
        SpringApplication.run(GatewayApplication.class, args);
        log.info("============================= GatewayApplication Started =============================");
    }
}
