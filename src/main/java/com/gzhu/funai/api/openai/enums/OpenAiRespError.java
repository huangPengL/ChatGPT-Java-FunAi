package com.gzhu.funai.api.openai.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/28 0:23
 */


public enum  OpenAiRespError {
    //官方的错误码列表：https://platform.openai.com/docs/guides/error-codes/api-errors
    OPENAI_BAD_REQUEST_ERROR(400, "错误的请求"),
    OPENAI_AUTHENTICATION_ERROR(401, "身份验证无效/提供的 API 密钥不正确"),
    OPENAI_FORBIDDEN(403, "禁止访问"),
    OPENAI_LIMIT_ERROR(429 , "达到请求的速率限制/您超出了当前配额，请检查您的计划和帐单详细信息/发动机当前过载，请稍后重试"),
    OPENAI_SERVER_ERROR(500, "服务器在处理您的请求时出错"),


    OPENAI_APIKEY_EXPIRED(1000, "APIKey过期")
    ;

    public final int code;
    public final String msg;

    private static final Map<Integer, OpenAiRespError> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(item->item.code, item->item));

    public static boolean contain(Integer code){
        return MAP.containsKey(code);
    }
    public static OpenAiRespError get(Integer code){
        return MAP.get(code);
    }
    OpenAiRespError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
