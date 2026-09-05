package com.veteran.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的防刷服务
 */
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "rate:submit:";
    private static final int MAX_SUBMISSIONS = 10;       // 最大提交次数
    private static final int WINDOW_SECONDS = 60;        // 时间窗口（秒）
    private static final long COOLDOWN_SECONDS = 10;     // 单次提交冷却（秒）

    /**
     * 检查是否允许提交
     * @param studentId 学生ID
     * @return true=允许提交, false=触发频率限制
     */
    public boolean trySubmit(Long studentId) {
        String key = KEY_PREFIX + studentId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            // 首次请求，设置过期时间
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        return count <= MAX_SUBMISSIONS;
    }

    /**
     * 获取剩余可用次数
     */
    public long getRemainingAttempts(Long studentId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + studentId);
        if (value == null) {
            return MAX_SUBMISSIONS;
        }
        return Math.max(0, MAX_SUBMISSIONS - Long.parseLong(value));
    }

    /**
     * 获取冷却剩余秒数
     */
    public long getCooldownSeconds(Long studentId) {
        String key = KEY_PREFIX + studentId;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
