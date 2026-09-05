package com.veteran.aop;

import com.veteran.common.utils.JwtUtil;
import com.veteran.mapper.OperationLogMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final JwtUtil jwtUtil;

    @Around("@annotation(com.veteran.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        com.veteran.entity.OperationLog logEntity = new com.veteran.entity.OperationLog();

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            com.veteran.annotation.OperationLog annotation = method.getAnnotation(com.veteran.annotation.OperationLog.class);

            logEntity.setAction(annotation.value());
            logEntity.setMethod(method.getDeclaringClass().getSimpleName() + "." + method.getName());

            // 获取请求信息和操作人
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setIp(getClientIp(request));

                // 从 JWT token 获取操作人用户名
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    try {
                        token = token.substring(7);
                        Claims claims = jwtUtil.parseToken(token);
                        logEntity.setOperator(claims.get("username", String.class));
                    } catch (Exception e) {
                        logEntity.setOperator("unknown");
                    }
                } else {
                    logEntity.setOperator("unknown");
                }
            } else {
                logEntity.setOperator("system");
            }

            // 提取请求参数（脱敏处理）
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (Object arg : args) {
                    if (arg != null && !(arg instanceof javax.servlet.http.HttpServletRequest)
                            && !(arg instanceof javax.servlet.http.HttpServletResponse)) {
                        String str = arg.toString();
                        if (str != null && str.length() > 500) {
                            str = str.substring(0, 500) + "...";
                        }
                        sb.append(str).append("; ");
                    }
                }
                String params = sb.toString();
                if (params.length() > 1000) {
                    params = params.substring(0, 1000);
                }
                logEntity.setParams(params);
            }

            // 执行目标方法
            Object result = joinPoint.proceed();

            logEntity.setStatus(1);
            logEntity.setCreateTime(LocalDateTime.now());

            try {
                operationLogMapper.insert(logEntity);
            } catch (Exception e) {
                log.warn("写入操作日志失败: {}", e.getMessage());
            }

            return result;
        } catch (Throwable e) {
            logEntity.setStatus(0);
            String errMsg = e.getMessage();
            if (errMsg != null && errMsg.length() > 500) {
                errMsg = errMsg.substring(0, 500);
            }
            logEntity.setErrorMsg(errMsg);
            logEntity.setCreateTime(LocalDateTime.now());

            try {
                operationLogMapper.insert(logEntity);
            } catch (Exception ex) {
                log.warn("写入操作日志失败: {}", ex.getMessage());
            }

            throw e;
        }
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
}
