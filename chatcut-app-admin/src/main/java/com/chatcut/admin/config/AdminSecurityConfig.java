package com.chatcut.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器。BCrypt strength 取 12（单次校验约 200~300ms），在暴力破解成本与登录响应之间取平衡。
 *
 * <p>调 strength 不影响存量密码校验——哈希串里自带参数，只有新写入的密码才用新强度。</p>
 */
@Configuration
public class AdminSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
