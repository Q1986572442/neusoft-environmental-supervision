package org.nep.feedback.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁
 *
 * <h3>实现原理</h3>
 * <ul>
 *   <li>使用 Redis SETNX + EXPIRE 实现互斥锁</li>
 *   <li>锁持有者标识 = UUID，防止误解锁</li>
 *   <li>支持自动续期（看门狗）防止业务执行超时锁被释放</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * String lockId = distributedLock.tryLock("task:escalation", Duration.ofMinutes(5));
 * if (lockId != null) {
 *     try {
 *         // 执行业务逻辑
 *     } finally {
 *         distributedLock.unlock("task:escalation", lockId);
 *     }
 * }
 * }</pre>
 *
 * @author NEP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 锁的持有者标识（UUID，确保释放时只释放自己的锁） */
    private final ThreadLocal<String> lockHolder = new ThreadLocal<>();

    /**
     * 尝试获取分布式锁（非阻塞）
     *
     * @param lockKey  锁的 key
     * @param timeout  锁的持有超时时间（防止死锁）
     * @return 锁标识（用于释放），获取失败返回 null
     */
    public String tryLock(String lockKey, Duration timeout) {
        String lockValue = UUID.randomUUID().toString();
        String fullKey = "lock:" + lockKey;

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(fullKey, lockValue, timeout.toSeconds(), TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(acquired)) {
            lockHolder.set(lockValue);
            log.debug("Distributed lock acquired: {} (holder={}, TTL={})", lockKey, lockValue, timeout);
            return lockValue;
        }
        log.debug("Distributed lock FAILED: {} (already held)", lockKey);
        return null;
    }

    /**
     * 尝试获取分布式锁（阻塞等待）
     *
     * @param lockKey     锁的 key
     * @param timeout     锁的持有超时
     * @param waitMillis  最大等待毫秒数
     * @return 锁标识，超时仍获取失败返回 null
     */
    public String tryLock(String lockKey, Duration timeout, long waitMillis) {
        long deadline = System.currentTimeMillis() + waitMillis;
        String lockValue = null;
        while (System.currentTimeMillis() < deadline) {
            lockValue = tryLock(lockKey, timeout);
            if (lockValue != null) return lockValue;
            try {
                Thread.sleep(200 + (long) (Math.random() * 300)); // 随机退避 200-500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        log.warn("Distributed lock timeout after {}ms: {}", waitMillis, lockKey);
        return null;
    }

    /**
     * 释放分布式锁
     * <p>
     * 使用 Lua 脚本保证原子性：只有持有者本人才能释放锁。
     *
     * @param lockKey   锁的 key
     * @param lockValue 获取锁时返回的标识
     * @return true = 成功释放
     */
    public boolean unlock(String lockKey, String lockValue) {
        String fullKey = "lock:" + lockKey;

        // Lua 原子脚本：检查 value 是否匹配，匹配则删除
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end""";

        Long result = redisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
                java.util.Collections.singletonList(fullKey),
                lockValue
        );

        boolean released = result != null && result > 0;
        if (released) {
            log.debug("Distributed lock released: {}", lockKey);
        }
        lockHolder.remove();
        return released;
    }

    /**
     * 锁续期（看门狗）
     * <p>
     * 在业务执行时间可能超过锁超时时间时调用，延长锁的持有时间。
     *
     * @param lockKey   锁的 key
     * @param lockValue 锁标识
     * @param extendTo  新的超时秒数
     */
    public boolean extend(String lockKey, String lockValue, long extendTo) {
        String fullKey = "lock:" + lockKey;
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('expire', KEYS[1], ARGV[2])
                else
                    return 0
                end""";

        Long result = redisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
                java.util.Collections.singletonList(fullKey),
                lockValue, String.valueOf(extendTo)
        );

        return result != null && result > 0;
    }

    /**
     * 强制释放锁（管理操作，慎用）
     */
    public boolean forceUnlock(String lockKey) {
        return Boolean.TRUE.equals(redisTemplate.delete("lock:" + lockKey));
    }
}
