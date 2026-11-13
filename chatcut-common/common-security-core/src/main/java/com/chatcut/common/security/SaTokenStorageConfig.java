package com.chatcut.common.security;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoRedisJackson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 会话存储：显式指定为 Redis（{@link SaTokenDaoRedisJackson}），不使用默认内存实现。
 *
 * <p>为什么必须显式做这件事：</p>
 * <ul>
 *   <li>sa-token 默认的 {@code SaTokenDaoDefaultImpl} 把登录态放在 JVM 内存里，
 *       多节点部署时 A 节点登录、B 节点校验必然失败，重启即全部掉线；</li>
 *   <li>把 Dao 注册进 {@code SaManager} 的动作在 {@code sa-token-spring-boot3-starter} 的
 *       {@code SaBeanInject} 里，而该 starter 会带上 {@code spring-boot-starter-web}，
 *       <b>网关是 WebFlux，不能引它</b>；</li>
 *   <li>因此这里统一从容器里取 {@code SaTokenDao} bean（由 {@code sa-token-redis-jackson}
 *       自动装配）注册进 {@code SaManager}，网关与业务服务走同一条路径。</li>
 * </ul>
 *
 * <p>三个服务必须指向同一个 Redis 实例与同一个 database，否则登录态不共享——
 * 连接配置统一放在 Nacos 的 {@code chatcut-common-{profile}.yaml}。</p>
 */
@Slf4j
@Configuration
public class SaTokenStorageConfig {

    /** 容器里必须存在 Redis 版 Dao，否则直接启动失败（宁可起不来，也不要静默退化成内存会话）。 */
    @Bean
    public SmartInitializingSingleton saTokenDaoRegistrar(ObjectProvider<SaTokenDao> daoProvider) {
        return () -> {
            SaTokenDao dao = daoProvider.getIfAvailable();
            if (dao == null) {
                throw new IllegalStateException(
                        "未找到 SaTokenDao：请确认已引入 sa-token-redis-jackson 并配置了 spring.data.redis");
            }
            if (!(dao instanceof SaTokenDaoRedisJackson)) {
                throw new IllegalStateException(
                        "Sa-Token 会话存储必须是 Redis，当前为 " + dao.getClass().getName()
                                + "；多节点部署会导致登录态无法共享");
            }
            SaManager.setSaTokenDao(dao);
            log.info("Sa-Token 会话存储：{}（Redis 持久化，多节点共享）", dao.getClass().getSimpleName());
        };
    }
}
