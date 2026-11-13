package com.chatcut.common.security;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpLogic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * C 端用户与管理端管理员各一套 StpLogic：
 * <ul>
 *   <li>loginType 不同（user / admin），token 前缀、有效期、存储 key 全部隔离；</li>
 *   <li>C 端 token 打到 /admin/** 一律无效，反之亦然。</li>
 * </ul>
 *
 * <p><b>为什么不用 @Bean 声明 StpLogic：</b>sa-token-spring-boot3-starter 的
 * {@code SaBeanInject#setStpLogic(StpLogic)} 是按类型注入"唯一的一个"StpLogic，
 * 一旦容器里存在 userStpLogic 和 adminStpLogic 两个同类型 bean，启动直接报
 * {@code required a single bean, but 2 were found}。
 * 正确做法是像这里一样：new 出来交给 {@code SaManager.putStpLogic(...)} 注册，
 * 让 sa-token 按 loginType 取用，而不是暴露成 Spring bean。</p>
 */
@Slf4j
@Configuration
public class StpLogicConfig {

    /**
     * 容器里所有单例就绪后注册两套 StpLogic。
     *
     * <p>用 SmartInitializingSingleton 而不是 @PostConstruct：确保此时 SaManager 已完成
     * 自身初始化（Dao 也已由 {@link SaTokenStorageConfig} 换成 Redis 实现）。</p>
     */
    @Bean
    public SmartInitializingSingleton stpLogicRegistrar() {
        return () -> {
            StpLogic userLogic = new StpLogic(LoginType.USER.value());
            StpUserUtil.bind(userLogic);
            SaManager.putStpLogic(userLogic);

            StpLogic adminLogic = new StpLogic(LoginType.ADMIN.value());
            StpAdminUtil.bind(adminLogic);
            SaManager.putStpLogic(adminLogic);

            log.info("Sa-Token 双账号体系就绪：loginType={} / {}",
                    LoginType.USER.value(), LoginType.ADMIN.value());
        };
    }
}
