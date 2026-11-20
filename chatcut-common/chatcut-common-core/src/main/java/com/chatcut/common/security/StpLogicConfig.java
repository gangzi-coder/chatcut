package com.chatcut.common.security;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多账号体系的 StpLogic 注册：
 * <ul>
 *   <li>loginType 不同（user / enterprise / login），token 前缀、有效期、存储 key 全部隔离；</li>
 *   <li>C 端 token 打到 /enterprise/** 或 /admin/** 一律无效，反之亦然。</li>
 * </ul>
 *
 * <p><b>为什么不用 @Bean 声明 StpLogic：</b>sa-token-spring-boot3-starter 的
 * {@code SaBeanInject#setStpLogic(StpLogic)} 是按类型注入"唯一的一个"StpLogic，
 * 一旦容器里存在多个同类型 bean，启动直接报
 * {@code required a single bean, but 2 were found}。
 * 正确做法是像这里一样：new 出来交给 {@code SaManager.putStpLogic(...)} 注册，
 * 让 sa-token 按 loginType 取用，而不是暴露成 Spring bean。</p>
 *
 * <p><b>与若依的分工：</b>管理端登录类型 {@code login} 由若依的
 * {@code SaTokenConfig#getStpLogicJwt} 提供，servlet 服务（admin / api / enterprise / portal）
 * 直接复用，本类不再重复注册；网关是 WebFlux，拿不到若依的 satoken starter，
 * 所以由 {@link GatewayAdminLogicConfig} 补一份同类型的 JWT 逻辑。</p>
 *
 * <p>{@code chatcut.security.type=ruoyi} 时整体关闭（admin 完全走若依单账号体系）。</p>
 */
@Slf4j
@Configuration
@ConditionalOnExpression("!('ruoyi'.equals('${chatcut.security.type:}'))")
public class StpLogicConfig {

    /**
     * 容器里所有单例就绪后注册 C 端与 B 端两套 StpLogic。
     *
     * <p>用 SmartInitializingSingleton 而不是 @PostConstruct：确保此时 SaManager 已完成
     * 自身初始化（DAO 已由若依 {@code SaTokenConfig} 的 {@code PlusSaTokenDao} 接管）。</p>
     */
    @Bean
    public SmartInitializingSingleton stpLogicRegistrar() {
        return () -> {
            StpLogic userLogic = new StpLogic(LoginType.USER.value());
            StpUserUtil.bind(userLogic);
            SaManager.putStpLogic(userLogic);

            StpLogic enterpriseLogic = new StpLogic(LoginType.ENTERPRISE.value());
            StpEnterpriseUtil.bind(enterpriseLogic);
            SaManager.putStpLogic(enterpriseLogic);

            log.info("Sa-Token 多账号体系就绪：loginType={} / {}（管理端 login 由若依提供）",
                    LoginType.USER.value(), LoginType.ENTERPRISE.value());
        };
    }

    /**
     * 网关（WebFlux）专用：补注册管理端 {@code login} 体系。
     *
     * <p>条件限定为 Reactive 应用，servlet 服务不会命中，避免与若依
     * {@code StpLogicJwtForSimple} 重复注册同一个 loginType。</p>
     */
    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class GatewayAdminLogicConfig {

        /** 注册 loginType=login 的 JWT 逻辑，与若依 admin 签发的 token 兼容。 */
        @Bean
        public SmartInitializingSingleton stpAdminLogicRegistrar() {
            return () -> {
                StpLogic adminLogic = new StpLogicJwtForSimple();
                StpAdminUtil.bind(adminLogic);
                SaManager.putStpLogic(adminLogic);
                log.info("Sa-Token 网关管理端体系就绪：loginType={}", LoginType.ADMIN.value());
            };
        }
    }
}
