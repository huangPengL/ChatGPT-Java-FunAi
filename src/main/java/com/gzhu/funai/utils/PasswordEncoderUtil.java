package com.gzhu.funai.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Author :wuxiaodong
 * @Date: 2023/3/30 13:26
 * @Description:用户密码加密工具类
 */
public class PasswordEncoderUtil {
    public static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private PasswordEncoderUtil() {

    }
}
