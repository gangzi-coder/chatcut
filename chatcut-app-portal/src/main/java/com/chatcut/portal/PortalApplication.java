package com.chatcut.portal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 官网门户服务：面向公网匿名访问，全部接口只读。
 * 鉴权由网关白名单放行 /portal/**，本服务不引入 sa-token，也没有登录态。
 */
@SpringBootApplication(scanBasePackages = "com.chatcut")
@MapperScan("com.chatcut.portal.**.repository.mapper")
public class PortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
