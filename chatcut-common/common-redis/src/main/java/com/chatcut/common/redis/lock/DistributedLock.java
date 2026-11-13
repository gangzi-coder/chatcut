package com.chatcut.common.redis.lock;

import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import com.chatcut.common.redis.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 * 分布式锁模板：拿不到锁就快速失败，不阻塞线程池。
 * 锁 key 统一由 RedisKeys.lock 生成；因显式指定了 leaseSeconds，Redisson 看门狗不会自动续期，
 * 业务执行时长必须明显小于 leaseSeconds，否则锁会提前释放。
 */
@Component
@RequiredArgsConstructor
public class DistributedLock {

    /** 默认最多等锁 3 秒：拿不到按"操作频繁"快速失败 */
    public static final long DEFAULT_WAIT_SECONDS = 3;
    /** 默认持锁 10 秒自动释放（防死锁兜底）：业务耗时必须明显小于该值，超过才用全参重载调大 */
    public static final long DEFAULT_LEASE_SECONDS = 10;

    private final RedissonClient redissonClient;

    /**
     * 抢到锁才执行 action，执行完或异常后释放；抢锁失败直接抛异常快速返回。
     * 使用默认等待/持锁时长（等 3s / 持 10s），绝大多数写操作用这个重载即可。
     */
    public <T> T execute(String biz, Object id, Supplier<T> action) {
        return execute(biz, id, DEFAULT_WAIT_SECONDS, DEFAULT_LEASE_SECONDS, action);
    }

    /**
     * 全参版本：仅当业务耗时确实超过默认持锁时长（如批量任务）时才用，
     * 调大 leaseSeconds 时要评估"持锁时间 = 用户等待时间"的上限。
     */
    public <T> T execute(String biz, Object id, long waitSeconds, long leaseSeconds, Supplier<T> action) {
        // biz + id 组成互斥维度，同一维度全局串行
        RLock lock = redissonClient.getLock(RedisKeys.lock(biz, id));
        boolean locked = false;
        try {
            // 最多等 waitSeconds 秒抢锁；抢到后 leaseSeconds 秒自动释放（兜底防死锁）
            locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            if (!locked) {
                // 没抢到就快速失败，用限流语义提示稍后重试，不让线程干等
                throw new BizException(CommonErrorCode.RATE_LIMITED, "操作过于频繁，请稍后再试");
            }
            return action.get();
        } catch (InterruptedException e) {
            // 等待被中断：恢复中断标志位交还上层，按系统异常处理
            Thread.currentThread().interrupt();
            throw new BizException(CommonErrorCode.SYSTEM_ERROR);
        } finally {
            // 仅释放本次线程持有的锁，避免误解锁或抛 IllegalMonitorStateException
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
