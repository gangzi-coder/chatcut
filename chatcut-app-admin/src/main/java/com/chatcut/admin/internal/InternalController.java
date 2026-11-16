package com.chatcut.admin.internal;

import com.chatcut.common.core.domain.Result;
import com.chatcut.common.security.web.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务内部端点：探活与身份回显，给网关转发、部署巡检、联调用，不对前端开放。
 *
 * <p>不走 actuator 的原因：这两处要返回统一 {@code Result} 结构，并带业务字段（whoami 回显解析出的
 * adminId）。真正的健康检查仍以 {@code /actuator/health} 为准，那是 Spring Boot 的健康聚合。</p>
 *
 * <p>鉴权上不归 RBAC 管：admin 侧 Sa-Token 白名单放行 {@code /internal/**}，网关侧只放行
 * {@code /internal/health}（whoami 要登录后才有意义）。生产部署不要把服务端口直接对公网暴露。</p>
 */
@RestController
@RequestMapping("/internal")
public class InternalController {

    @Value("${spring.application.name}")
    private String appName;

    /** 探活：进程能处理请求即 UP，不探测数据库/Redis（那是 actuator 的职责）。 */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("app", appName);
        body.put("ts", Instant.now().toString());
        return Result.ok(body);
    }

    /** 身份回显：确认网关透传的凭证被正确解析成 adminId；未带凭证时为 null。 */
    @GetMapping("/whoami")
    public Result<Map<String, Object>> whoami() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("adminId", CurrentUser.adminId());
        return Result.ok(body);
    }
}
