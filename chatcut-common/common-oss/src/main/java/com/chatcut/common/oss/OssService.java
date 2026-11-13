package com.chatcut.common.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.time.Duration;
import java.util.Date;

/**
 * 对象存储的操作收敛在这里，业务层不直接依赖 OSS SDK。
 *
 * <p>本类不挂 @Service：由 {@link OssConfig} 在 OSS 客户端存在时才注册成 bean，
 * 未配置 AK 的环境下这个 bean 根本不存在，业务侧请用
 * {@code ObjectProvider<OssService>} 或 @ConditionalOnBean 自行兜底。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class OssService {

    private final OSS ossClient;
    private final OssProperties properties;

    public long sizeOf(String key) {
        ObjectMetadata meta = ossClient.getObjectMetadata(properties.getBucket(), key);
        return meta.getContentLength();
    }

    public boolean exists(String key) {
        return ossClient.doesObjectExist(properties.getBucket(), key);
    }

    public String signedUrl(String key, Duration ttl) {
        Date expiration = new Date(System.currentTimeMillis() + ttl.toMillis());
        URL url = ossClient.generatePresignedUrl(properties.getBucket(), key, expiration);
        return url.toString();
    }

    public void delete(String key) {
        ossClient.deleteObject(properties.getBucket(), key);
        log.info("oss object deleted, key={}", key);
    }
}
