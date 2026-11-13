package com.chatcut.admin.common;

import com.chatcut.common.core.domain.Result;
import com.chatcut.common.security.web.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class InternalController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("app", appName);
        body.put("ts", Instant.now().toString());
        return Result.ok(body);
    }

    @GetMapping("/whoami")
    public Result<Map<String, Object>> whoami() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("adminId", CurrentUser.adminId());
        return Result.ok(body);
    }
}
