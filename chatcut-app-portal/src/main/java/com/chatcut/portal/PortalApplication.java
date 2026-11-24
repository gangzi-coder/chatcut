package com.chatcut.portal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 官网门户服务：面向公网匿名访问。公开内容读取免登录；访客留资/反馈为
 * 本服务允许的写入（含限流、防重复提交与输入校验）。
 * 鉴权由网关白名单放行 /portal/**，本服务不引入 sa-token，也没有登录态。
 */
@SpringBootApplication(scanBasePackages = "com.chatcut")
@MapperScan("com.chatcut.portal.**.repository.mapper")
public class PortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
