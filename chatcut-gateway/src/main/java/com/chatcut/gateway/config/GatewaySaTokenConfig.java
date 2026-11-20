package com.chatcut.gateway.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.dao.SaTokenDao;
import com.chatcut.gateway.security.SharedSaTokenDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关侧 Sa-Token 会话存储配置。
 *
 * <p>为什么放在网关而不是公共模块：</p>
 * <ul>
 *   <li>servlet 服务（admin / api / enterprise / portal）已由 {@code chatcut-common-satoken}
 *       的 {@code SaTokenConfig#saTokenDao} 提供 {@code PlusSaTokenDao}，再注册一份就是重复覆盖；</li>
 *   <li>网关是 WebFlux，不能引入若依的 Servlet starter，因此在这里注册一份等价实现，
 *       读写的 key 与序列化格式和若依 {@code PlusSaTokenDao} 完全一致，登录态才能跨服务共享。</li>
 * </ul>
 *
 * <p>所有服务必须指向同一个 Redis 实例与 database，否则网关解析不到业务服务签发的 token——
 * 连接配置统一放在 Nacos 的 {@code chatcut-common-{profile}.yaml}。</p>
 */
@Slf4j
@Configuration
public class GatewaySaTokenConfig {

    /**
     * 把 Nacos 里的 {@code sa-token.*} 绑定进 sa-token 全局配置。
     *
     * <p>servlet 服务由 sa-token-spring-boot3-starter 自动完成这步；网关是 WebFlux，
     * 没有引入该 starter，不绑定的后果是 {@code SaManager} 用默认配置（jwt-secret-key 为空），
     * 解析管理端 JWT 时拿不到 loginId，/admin/** 会一律 401。</p>
     */
    @Bean
    @ConfigurationProperties(prefix = "sa-token")
    public SaTokenConfig saTokenConfigProperties() {
        SaTokenConfig config = new SaTokenConfig();
        SaManager.setConfig(config);
        return config;
    }

    /** 注册与若依 admin 使用同样 Redis 序列化的 DAO。 */
    @Bean
    public SmartInitializingSingleton saTokenDaoRegistrar() {
        return () -> {
            SaTokenDao dao = new SharedSaTokenDao();
            SaManager.setSaTokenDao(dao);
            log.info("网关 Sa-Token 会话存储：{}（与若依 PlusSaTokenDao 同序列化，跨服务共享）", dao.getClass().getSimpleName());
        };
    }
}
