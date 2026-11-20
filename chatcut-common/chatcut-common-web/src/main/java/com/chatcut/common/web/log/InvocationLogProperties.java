package com.chatcut.common.web.log;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 出入参日志切面配置。挂在 Nacos 的 chatcut-common-{profile}.yaml，可按环境覆盖；
 * 本地默认值即开，重启后生效（未开配置刷新，改 Nacos 需重启服务）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "chatcut.log.invocation")
public class InvocationLogProperties {

    /** 总开关。prod 排查压力大时可整体关闭。 */
    private boolean enabled = true;

    /** 耗时超过该值（毫秒）单独打 WARN（SLOW 标记），便于告警规则直接抓慢接口。 */
    private long slowThresholdMs = 1000;

    /**
     * 脱敏字段名黑名单（匹配时统一转小写比较）。
     * 命中的字段值序列化为 "***"。只作用于 JavaBean 属性（BeanSerializerModifier），
     * Map 的 key 不在覆盖范围内——登录等敏感入参请用对象承接。
     */
    private Set<String> sensitiveFields = new LinkedHashSet<>(List.of(
            "password", "oldpassword", "newpassword", "confirmpassword",
            "token", "authorization", "secret", "smscode",
            "idcard", "phone", "mobile"));

    /** 不打印出入参的路径（Ant 风格），文档/监控/内部探活都是高频无价值噪音。 */
    private Set<String> excludePaths = new LinkedHashSet<>(List.of(
            "/actuator/**", "/v3/api-docs/**", "/doc.html", "/swagger-ui/**",
            "/swagger-ui.html", "/webjars/**", "/favicon.ico", "/internal/**", "/error"));
}
