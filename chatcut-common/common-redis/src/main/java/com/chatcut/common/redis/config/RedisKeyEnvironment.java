package com.chatcut.common.redis.config;

import com.chatcut.common.redis.constant.RedisKeys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 启动时把当前 profile 写进 Redis Key 前缀（{@code cc:{env}:{biz}:...}），
 * 避免多环境共用一个 Redis 时 key 串号。
 *
 * <p><b>环境标识只有 spring.profiles.active 一个来源</b>，不再单独维护
 * {@code chatcut.env}。之前两者并存时，切环境要改两处，而且极易错配成
 * 「profile=dev、key 前缀还是 local」，排查起来很难发现。</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisKeyEnvironment {

    /** profile 为空时的兜底标识，避免 key 退化成 cc::biz:xxx */
    private static final String DEFAULT_ENV = "default";

    private final Environment environment;

    /**
     * 取第一个激活的 profile 作为环境标识。
     * 多 profile（如 dev,canary）时用第一个，保证前缀里不会出现逗号。
     */
    @PostConstruct
    public void init() {
        String[] profiles = environment.getActiveProfiles();
        String env = profiles.length == 0 ? DEFAULT_ENV : profiles[0];
        RedisKeys.initEnv(env);
        log.info("Redis Key 前缀已绑定环境：cc:{}:*", env);
    }
}
