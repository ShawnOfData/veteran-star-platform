package com.veteran.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 阿里云短信服务 — 生产环境
 *
 * 需在阿里云控制台完成：
 * 1. 开通短信服务
 * 2. 申请签名（如"戎归星辉"）
 * 3. 申请模板（如 SMS_123456789，模板内容需含 ${code}）
 * 4. 获取 RAM 用户 AccessKey
 */
@Slf4j
@Service
@Profile("prod")
public class AliyunSmsService extends AbstractSmsService {

    @Value("${aliyun.sms.access-key:}")
    private String accessKey;

    @Value("${aliyun.sms.secret-key:}")
    private String secretKey;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    private static final String REGION = "cn-hangzhou";
    private static final String DOMAIN = "dysmsapi.aliyuncs.com";
    private static final String VERSION = "2017-05-25";
    private static final Gson GSON = new Gson();

    private IAcsClient client;

    @PostConstruct
    public void init() {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            log.warn("阿里云短信 AccessKey 未配置，SMS 将降级为 Mock 模式");
            return;
        }
        DefaultProfile profile = DefaultProfile.getProfile(REGION, accessKey, secretKey);
        this.client = new DefaultAcsClient(profile);
        log.info("阿里云短信客户端初始化成功 (region={})", REGION);
    }

    @Override
    protected String doSend(String phone, String code) {
        if (client == null) {
            log.warn("\n========================================\n" +
                     "  阿里云短信配置缺失，降级为 Mock 模式\n" +
                     "  手机号: {}\n  验证码: {}\n" +
                     "========================================", phone, code);
            return "";
        }

        try {
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain(DOMAIN);
            request.setSysVersion(VERSION);
            request.setSysAction("SendSms");
            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", signName);
            request.putQueryParameter("TemplateCode", templateCode);
            request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");

            CommonResponse response = client.getCommonResponse(request);
            Map<String, String> result = GSON.fromJson(response.getData(), Map.class);

            if ("OK".equals(result.get("Code"))) {
                log.info("短信发送成功: phone={}, bizId={}", phone, result.get("BizId"));
                return "";
            } else {
                log.error("短信发送失败: phone={}, code={}, msg={}",
                        phone, result.get("Code"), result.get("Message"));
                return mapError(result.get("Code"), result.get("Message"));
            }
        } catch (ClientException e) {
            log.error("短信发送异常: phone={}, errCode={}, msg={}", phone, e.getErrCode(), e.getErrMsg(), e);
            // 发送失败，清除已生成的验证码
            redisTemplate.delete(CODE_PREFIX + phone);
            redisTemplate.delete(LIMIT_PREFIX + phone);
            return "短信发送失败，请稍后重试";
        }
    }

    /**
     * 将阿里云错误码映射为用户可读中文
     */
    private String mapError(String code, String message) {
        if (code == null) return "短信发送失败";
        switch (code) {
            case "isv.BUSINESS_LIMIT_CONTROL":
                return "短信发送频率过高，请稍后再试";
            case "isv.MOBILE_NUMBER_ILLEGAL":
                return "手机号码格式不正确";
            case "isv.TEMPLATE_MISSING_PARAMETERS":
                return "短信模板参数缺失，请联系管理员";
            case "isv.SIGN_NAME_ILLEGAL":
                return "短信签名不合法，请联系管理员";
            case "isv.AMOUNT_NOT_ENOUGH":
                return "短信账户余额不足";
            case "isp.SYSTEM_ERROR":
            case "isv.SYSTEM_ERROR":
                return "短信服务异常，请稍后重试";
            default:
                return "短信发送失败: " + (message != null ? message : code);
        }
    }
}
