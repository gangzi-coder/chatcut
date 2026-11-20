package com.chatcut.portal.internal;

import com.chatcut.common.core.domain.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 运维与冒烟用接口，不承载业务。本服务无登录态，只有 health 没有 whoami。
 * 端点路径 /internal/** 被网关白名单引用，不要随意改。
 */
@RestController
@RequestMapping("/internal")
public class InternalController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("app", appName);
        body.put("ts", Instant.now().toString());
        return R.ok(body);
    }
}
