package com.chatcut.enterprise;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.chatcut")
@MapperScan("com.chatcut.enterprise.**.repository.mapper")
@EnableScheduling
public class EnterpriseApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseApplication.class, args);
    }
}
