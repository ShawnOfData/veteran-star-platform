package com.veteran.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 全局限流拦截器 — 小服务器资源保护
 * /api/app/** 和 /api/admin/** 路径：同 IP 每秒最多 100 次请求，防止恶意刷接口
 * 注意：所有 REST 接口已统一加 /api 前缀（见 WebMvcConfig）
 *
 * 阈值说明：前端单页加载（如首页）会并行触发 10+ 个接口请求，
 * 20 req/s 过低会导致正常浏览被限流，调整为 100 req/s 兼顾防护与可用性。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final StringRedisTemplate redisTemplate;

    private static final int GLOBAL_LIMIT_RPS = 100;
    private static final String LIMIT_KEY_PREFIX = "rate:global:";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                     Object handler) throws Exception {
                String path = request.getRequestURI();
                // 仅限 /api/app/** 和 /api/admin/** 路径（REST 接口统一 /api 前缀）
                if (!path.startsWith("/api/app/") && !path.startsWith("/api/admin/")) {
                    return true;
                }

                String ip = getClientIp(request);
                // 本地开发环境豁免限流，避免压测/调试时误报 429（生产由 Nginx 层防护）
                if ("127.0.0.1".equals(ip) || "localhost".equals(ip)
                        || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                    return true;
                }
                String key = LIMIT_KEY_PREFIX + ip;

                Long count = redisTemplate.opsForValue().increment(key);
                // 修复：确保键始终有 TTL，防止因后端重启/并发竞争导致 expire 丢失后
                // 计数永久累积（TTL=-1），使所有后续请求被永久限流
                if (count != null && count == 1) {
                    redisTemplate.expire(key, 1, TimeUnit.SECONDS);
                } else {
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl == null || ttl < 0) {
                        redisTemplate.expire(key, 1, TimeUnit.SECONDS);
                    }
                }

                if (count != null && count > GLOBAL_LIMIT_RPS) {
                    log.warn("[限流] IP: {} 超过全局限制 {} req/s, path: {}", ip, GLOBAL_LIMIT_RPS, path);
                    response.setStatus(429);
                    response.setContentType("application/json;charset=UTF-8");
                    // 与 Result 结构保持一致，前端拦截器可统一解析
                    response.getWriter().write(
                        "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
                    return false;
                }

                return true;
            }

            private String getClientIp(HttpServletRequest request) {
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }).order(0);
    }
}
