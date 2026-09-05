package com.veteran.controller.app;

import com.veteran.common.Result;
import com.veteran.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 短信验证码接口
 */
@Slf4j
@RestController
@RequestMapping("/app/sms")
@Tag(name = "短信验证码")
public class AppSmsController {

    @Autowired
    private SmsService smsService;

    @Operation(summary = "发送短信验证码")
    @PostMapping("/send")
    public Result<String> sendCode(@RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        String clientIp = getClientIp(request);
        String limitMsg = smsService.sendCode(phone, clientIp);
        if (!limitMsg.isEmpty()) {
            return Result.fail(limitMsg);
        }
        return Result.ok("验证码已发送");
    }

    /**
     * 获取客户端真实 IP（考虑 Nginx 反向代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Operation(summary = "校验短信验证码")
    @PostMapping("/verify")
    public Result<Boolean> verifyCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        if (phone == null || code == null) {
            return Result.fail("参数不完整");
        }
        boolean valid = smsService.verifyCode(phone, code);
        if (valid) {
            return Result.ok(true);
        }
        return Result.fail("验证码错误或已过期");
    }
}
