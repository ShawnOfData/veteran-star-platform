package com.veteran.service;

/**
 * 短信验证码服务接口
 */
public interface SmsService {

    /**
     * 发送验证码到指定手机号
     * @param phone 手机号
     * @return 频率限制文案，空串表示发送成功
     */
    String sendCode(String phone);

    /**
     * 发送验证码到指定手机号（带客户端 IP 限流）
     * @param phone    手机号
     * @param clientIp 客户端 IP
     * @return 频率限制文案，空串表示发送成功
     */
    String sendCode(String phone, String clientIp);

    /**
     * 校验验证码
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return true=验证通过, false=验证失败或已过期
     */
    boolean verifyCode(String phone, String code);
}
