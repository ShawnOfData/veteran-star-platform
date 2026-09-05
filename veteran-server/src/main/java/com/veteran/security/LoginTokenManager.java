package com.veteran.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录态白名单管理（基于 Redis）
 *
 * <p>用途：实现「同账号同类型多端登录互踢」。
 * 同一用户每次登录，会用新 token 覆盖 Redis 中的旧 token；
 * 旧端的 token 虽未过期，但已不在白名单中 → 后续请求被拒（401）→ 前端自动登出。
 *
 * <h3>Key 设计</h3>
 * <ul>
 *   <li>{@code login:admin:{userId}}    管理员</li>
 *   <li>{@code login:student:{userId}}  学生</li>
 * </ul>
 * 管理员与学生是不同账号体系，互不影响；
 * 同类型多端（如学生的 mobile 与 miniapp）共用同一 key，会互相顶替。
 *
 * <p>TTL 与 JWT 过期时间一致，token 过期后白名单自动清理，无需手动维护。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginTokenManager {

    private final StringRedisTemplate redisTemplate;

    public static final String ADMIN_PREFIX = "login:admin:";
    public static final String STUDENT_PREFIX = "login:student:";

    @Value("${jwt.expiration}")
    private long ttlMillis;

    // ===== 写入（登录时调用） =====

    public void saveAdminToken(Long adminId, String token) {
        save(ADMIN_PREFIX + adminId, token);
    }

    public void saveStudentToken(Long studentId, String token) {
        save(STUDENT_PREFIX + studentId, token);
    }

    private void save(String key, String token) {
        redisTemplate.opsForValue().set(key, token, ttlMillis, TimeUnit.MILLISECONDS);
    }

    // ===== 校验（Filter 每次请求调用） =====

    public boolean isAdminTokenValid(Long adminId, String token) {
        return isCurrent(ADMIN_PREFIX + adminId, token);
    }

    public boolean isStudentTokenValid(Long studentId, String token) {
        return isCurrent(STUDENT_PREFIX + studentId, token);
    }

    private boolean isCurrent(String key, String token) {
        if (token == null) {
            return false;
        }
        String current = redisTemplate.opsForValue().get(key);
        return token.equals(current);
    }

    // ===== 清除（登出时调用，可选；TTL 到期会自动清除） =====

    public void removeAdminToken(Long adminId) {
        redisTemplate.delete(ADMIN_PREFIX + adminId);
    }

    public void removeStudentToken(Long studentId) {
        redisTemplate.delete(STUDENT_PREFIX + studentId);
    }
}
