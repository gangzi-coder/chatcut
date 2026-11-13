package com.chatcut.common.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chatcut.oss")
public class OssProperties {

    /** 服务端访问用内网 Endpoint（内网流量免费） */
    private String internalEndpoint;

    /** 浏览器访问用公网 Endpoint */
    private String publicEndpoint;

    private String bucket;

    private String accessKeyId;

    private String accessKeySecret;

    /** 下载域名（可空，为空时用 publicEndpoint + bucket） */
    private String downloadDomain;

    /** STS 角色，用于下发前端直传凭证 */
    private String stsRoleArn;
}
