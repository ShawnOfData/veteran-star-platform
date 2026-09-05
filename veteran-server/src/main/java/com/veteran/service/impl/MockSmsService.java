package com.veteran.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Mock 短信服务 — 开发/测试环境，验证码打印到控制台
 */
@Slf4j
@Service
@Profile("!prod")
public class MockSmsService extends AbstractSmsService {

    @Override
    protected String doSend(String phone, String code) {
        log.info("\n========================================\n" +
                 "  【Mock短信】手机号: {}\n" +
                 "  验证码: {}\n" +
                 "  有效期: {}分钟\n" +
                 "========================================",
                phone, code, CODE_TTL_MINUTES);
        return "";
    }
}
