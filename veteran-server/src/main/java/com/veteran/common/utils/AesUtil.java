package com.veteran.common.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AesUtil {

    @Value("${aes.key}")
    private String aesKey;

    @Value("${aes.iv}")
    private String aesIv;

    private AES aes;

    @PostConstruct
    public void init() {
        aes = new AES("CBC", "PKCS5Padding",
                aesKey.getBytes(StandardCharsets.UTF_8),
                aesIv.getBytes(StandardCharsets.UTF_8));
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        return aes.encryptBase64(plainText);
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        return aes.decryptStr(cipherText);
    }

    public String hashPhone(String phone) {
        if (phone == null) {
            return null;
        }
        return SecureUtil.sha256(phone);
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}