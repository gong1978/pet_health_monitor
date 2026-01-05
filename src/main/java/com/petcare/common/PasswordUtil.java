package com.petcare.common;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 */
@Slf4j
@Component
public class PasswordUtil {

    /**
     * 加密密码
     */
    public static String encryptPassword(String password) {
        return SecureUtil.md5(password + "pet-health-monitor-salt");
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return encryptPassword(rawPassword).equals(encryptedPassword);
    }
}
