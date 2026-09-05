package com.veteran.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veteran.common.Result;
import com.veteran.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final LoginTokenManager loginTokenManager;
    private final ObjectMapper objectMapper;

    /**
     * 无需登录即可访问的公开接口。
     * 这些接口即使携带了本地旧 token，也不做白名单校验，
     * 避免「在 A 设备登录后，B 设备携带过期 token 访问登录/注册接口被误判 401」。
     */
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/admin/login",
            "/api/app/student/login",
            "/api/app/student/register",
            "/api/app/sms/send",
            "/api/app/sms/verify"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        String requestURI = request.getRequestURI();

        if (PUBLIC_ENDPOINTS.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!StringUtils.hasText(token)) {
            // REST 接口已统一加 /api 前缀（见 WebMvcConfig）
            if (requestURI.startsWith("/api/admin/") && !requestURI.equals("/api/admin/login")) {
                log.debug("请求 {} 未携带Token", requestURI);
            }
        } else if (!jwtUtil.validateToken(token)) {
            log.warn("JWT校验失败, URI={}, token前20位={}", requestURI, token.substring(0, Math.min(20, token.length())));
        } else {
            Long userId = jwtUtil.getUserId(token);

            // ===== Redis 白名单校验：当前 token 是否已被其他设备顶替 =====
            if (!isCurrentLoginToken(requestURI, userId, token)) {
                log.info("Token 已被其他设备顶替, userId={}, uri={}", userId, requestURI);
                writeReplacedResponse(response);
                return; // 不继续 filterChain，直接返回 401
            }

            String username = jwtUtil.getUsername(token);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, userId, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT认证成功, user={}, uri={}", username, requestURI);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 按请求路径所属账号体系，校验 token 是否为当前有效 token。
     * - /api/admin/**  → 管理员白名单
     * - 其他（/api/app/** 等）→ 学生白名单
     */
    private boolean isCurrentLoginToken(String requestURI, Long userId, String token) {
        if (requestURI.startsWith("/api/admin/")) {
            return loginTokenManager.isAdminTokenValid(userId, token);
        }
        return loginTokenManager.isStudentTokenValid(userId, token);
    }

    /**
     * 被顶替时返回 401，前端据此清本地态并跳转登录页。
     */
    private void writeReplacedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(401, "账号已在其他设备登录，请重新登录")));
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
