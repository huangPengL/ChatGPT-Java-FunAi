package com.gzhu.funai.exception;

/**
 * @Author :wuxiaodong
 * @Date: 2023/3/16 15:56
 * @Description:
 */
public class EmailException extends RuntimeException{
    public EmailException() {
        super("存在相同的邮箱");
    }
}
