package com.chatcut.common.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 客户端配置。
 *
 * <p>为什么不直接用 redisson-spring-boot-starter 的自动装配：它把
 * {@code spring.data.redis.password} 直接透传给 Redisson，而 yml 里
 * {@code password: ${REDIS_PASSWORD:}} 在未设置环境变量时是<b>空字符串</b>（不是 null），
 * Redisson 会认为"有密码"并发送 {@code AUTH ""}，无密码的 Redis 直接拒绝：
 * {@code ERR AUTH <password> called without any password configured}。
 * 所以这里显式判断空白串，只有真正配置了密码才 set。</p>
 *
 * <p>当前只装配单机模式。以后要上哨兵/集群，在这里按
 * {@code RedisProperties#getSentinel()} / {@code getCluster()} 分支扩展。</p>
 */
@Configuration
public class RedissonConfig {

    /**
     * 构建 RedissonClient：地址、库号、超时都取自 {@code spring.data.redis.*}，
     * 密码/用户名空白时不设置（对应无鉴权的本地 Redis）。
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties properties) {
        Config config = new Config();
        // 锁对象用 JSON 序列化，便于在 redis-cli 里直接看内容
        config.setCodec(new JsonJacksonCodec());

        SingleServerConfig single = config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setDatabase(properties.getDatabase());

        if (properties.getTimeout() != null) {
            single.setTimeout((int) properties.getTimeout().toMillis());
        }

        // 关键：空白密码不设置，避免无鉴权 Redis 收到 AUTH 而报错
        if (StringUtils.hasText(properties.getPassword())) {
            single.setPassword(properties.getPassword());
        }

        if (StringUtils.hasText(properties.getUsername())) {
            single.setUsername(properties.getUsername());
        }

        return Redisson.create(config);
    }
}
