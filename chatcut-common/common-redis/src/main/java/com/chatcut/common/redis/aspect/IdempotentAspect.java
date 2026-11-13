package com.chatcut.common.redis.aspect;

import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import com.chatcut.common.redis.annotation.Idempotent;
import com.chatcut.common.redis.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        String digest = userId() + ":" + Arrays.deepToString(pjp.getArgs()).hashCode();
        String key = RedisKeys.idempotent(idempotent.name(), digest);

        RBucket<String> bucket = redissonClient.getBucket(key);
        boolean acquired = bucket.trySet("1", idempotent.ttlSeconds(), TimeUnit.SECONDS);
        if (!acquired) {
            throw new BizException(CommonErrorCode.IDEMPOTENT_CONFLICT);
        }
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            // 业务失败允许立即重试
            bucket.delete();
            throw t;
        }
    }

    private String userId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "anonymous";
        }
        String userId = attrs.getRequest().getHeader("X-User-Id");
        return userId == null ? "anonymous" : userId;
    }
}
