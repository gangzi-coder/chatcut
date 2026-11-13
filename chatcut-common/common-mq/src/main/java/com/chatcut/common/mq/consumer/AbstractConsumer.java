package com.chatcut.common.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * 消费基类：Redis 快速去重 + 业务返回成功后才算消费完成。
 * 真正的幂等仍要靠数据库唯一索引，这里的 Redis 只是挡住重复消费的大头。
 */
@Slf4j
public abstract class AbstractConsumer<T> implements RocketMQListener<T> {

    private final ObjectProvider<StringRedisTemplate> redisProvider;

    protected AbstractConsumer(ObjectProvider<StringRedisTemplate> redisProvider) {
        this.redisProvider = redisProvider;
    }

    @Override
    public void onMessage(T message) {
        String dedupKey = dedupKey(message);
        if (dedupKey != null) {
            StringRedisTemplate redis = redisProvider.getIfAvailable();
            if (redis != null) {
                Boolean first = redis.opsForValue().setIfAbsent(dedupKey, "1", Duration.ofDays(2));
                if (Boolean.FALSE.equals(first)) {
                    log.info("duplicate message ignored, key={}", dedupKey);
                    return;
                }
            }
        }
        try {
            handle(message);
        } catch (Exception e) {
            log.error("consume failed, will be retried, key={}", dedupKey, e);
            throw e;
        }
    }

    /** 返回 null 表示不做 Redis 去重（例如纯通知类消息）。 */
    protected abstract String dedupKey(T message);

    protected abstract void handle(T message);
}
