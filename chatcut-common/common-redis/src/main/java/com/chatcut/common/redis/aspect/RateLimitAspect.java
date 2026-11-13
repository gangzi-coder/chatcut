package com.chatcut.common.redis.aspect;

import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import com.chatcut.common.redis.annotation.RateLimit;
import com.chatcut.common.redis.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String identity = "IP".equalsIgnoreCase(rateLimit.dimension()) ? clientIp() : currentUserId();
        String key = RedisKeys.rateLimit(rateLimit.name(), identity);

        RRateLimiter limiter = redissonClient.getRateLimiter(key);
        limiter.trySetRate(RateType.OVERALL, rateLimit.permits(), rateLimit.windowSeconds(), RateIntervalUnit.SECONDS);

        if (!limiter.tryAcquire(1)) {
            log.warn("rate limited key={}", key);
            throw new BizException(CommonErrorCode.RATE_LIMITED);
        }
        return pjp.proceed();
    }

    private String currentUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "anonymous";
        }
        String userId = attrs.getRequest().getHeader("X-User-Id");
        return userId == null || userId.isBlank() ? clientIp() : userId;
    }

    private String clientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        String forwarded = attrs.getRequest().getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? attrs.getRequest().getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
