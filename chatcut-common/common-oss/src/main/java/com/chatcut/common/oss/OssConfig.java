package com.chatcut.common.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对象存储装配。
 *
 * <p>只在 {@code chatcut.oss.access-key-id} <b>真正有值</b>时才创建客户端：
 * yml 里写的是 {@code access-key-id: ${OSS_AK:}}，没配环境变量时得到的是空字符串，
 * 而 {@code @ConditionalOnProperty} 只判断"属性是否存在"，空串同样命中，
 * 结果就是本地无凭证也去 new OSSClient 并抛
 * {@code Access key id should not be null or empty}。所以这里改用 SpEL 判空。</p>
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    /**
     * OSS 客户端：未配置 AK 时不创建 bean，保证本地/无对象存储的环境也能启动。
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${chatcut.oss.access-key-id:}')")
    public OSS ossClient(OssProperties properties) {
        return new OSSClientBuilder().build(
                properties.getInternalEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());
    }

    /**
     * OssService 与 OSS 客户端同生共死。
     *
     * <p>不用 @Service + @ConditionalOnBean 的原因：@ConditionalOnBean 放在
     * 被组件扫描的类上评估时机太早，OSS 的 bean 定义此时可能还没注册，
     * 结果直接抛 UnsatisfiedDependency。放在 @Configuration 里由 @Bean 方法注册，
     * 顺序是确定的。</p>
     */
    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${chatcut.oss.access-key-id:}')")
    public OssService ossService(OSS ossClient, OssProperties properties) {
        return new OssService(ossClient, properties);
    }
}
