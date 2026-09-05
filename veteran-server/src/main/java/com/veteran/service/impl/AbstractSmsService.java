package com.veteran.service.impl;

import com.veteran.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务抽象基类 — 封装 Redis 限流 + 验证码生成/存储逻辑
 * 子类只需实现 doSend(String phone, String code) 即可切换短信渠道
 */
public abstract class AbstractSmsService implements SmsService {

    @Autowired
    protected StringRedisTemplate redisTemplate;

    protected static final SecureRandom RANDOM = new SecureRandom();
    protected static final String CODE_PREFIX = "sms:code:";
    protected static final String LIMIT_PREFIX = "sms:limit:";
    protected static final String DAILY_PREFIX = "sms:daily:";
    protected static final String IP_PREFIX = "sms:ip:";
    protected static final int CODE_TTL_MINUTES = 5;
    protected static final int SEND_INTERVAL_SECONDS = 60;
    protected static final int DAILY_MAX = 10;
    protected static final int IP_PER_MINUTE_MAX = 3;

    @Override
    public String sendCode(String phone) {
        return sendCodeInternal(phone, null);
    }

    @Override
    public String sendCode(String phone, String clientIp) {
        return sendCodeInternal(phone, clientIp);
    }

    private String sendCodeInternal(String phone, String clientIp) {
        // 1. IP 限流
        if (clientIp != null && !clientIp.isEmpty()) {
            String ipKey = IP_PREFIX + clientIp;
            Long ipCount = redisTemplate.opsForValue().increment(ipKey);
            if (ipCount != null && ipCount == 1) {
                redisTemplate.expire(ipKey, 60, TimeUnit.SECONDS);
            }
            if (ipCount != null && ipCount > IP_PER_MINUTE_MAX) {
                return "请求过于频繁，请稍后再试";
            }
        }

        // 2. 60 秒内不可重复发送
        if (Boolean.TRUE.equals(redisTemplate.hasKey(LIMIT_PREFIX + phone))) {
            Long ttl = redisTemplate.getExpire(LIMIT_PREFIX + phone, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? "请" + ttl + "秒后再试" : "请稍后再试";
        }

        // 3. 每日上限
        String dailyKey = DAILY_PREFIX + phone + ":" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Long dailyCount = redisTemplate.opsForValue().increment(dailyKey);
        if (dailyCount != null && dailyCount == 1) {
            redisTemplate.expire(dailyKey, 1, TimeUnit.DAYS);
        }
        if (dailyCount != null && dailyCount > DAILY_MAX) {
            return "今日发送次数已达上限";
        }

        // 4. 生成 6 位验证码
        String code = String.format("%06d", RANDOM.nextInt(1000000));

        // 5. 存入 Redis（5 分钟有效）
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 6. 设置发送间隔
        redisTemplate.opsForValue().set(LIMIT_PREFIX + phone, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 7. 实际发送（由子类实现）
        return doSend(phone, code);
    }

    /**
     * 实际发送短信，子类实现
     * @return 空串 = 发送成功；非空 = 错误提示
     */
    protected abstract String doSend(String phone, String code);

    @Override
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }
        String key = CODE_PREFIX + phone;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null && stored.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
