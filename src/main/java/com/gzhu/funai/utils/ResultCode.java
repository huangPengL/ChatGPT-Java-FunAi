package com.gzhu.funai.utils;


/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 15:44
 */

public enum ResultCode {
    /**
     *
     */
    SUCCESS(20000, "成功"),
    ERROR(20001, "服务器内部错误"),
    EMPTY_PARAM(20003, "非空参数需要传递"),
    BAD_PARAM(20004, "参数错误"),

    USER_REGISTER_PARAMS_REPEAT(10001,"用户注册信息重复"),
    USER_NOT_LOGIN(10002,"用户未登录"),
    USER_NOT_EXIST(10003,"用户手机号未注册"),
    USER_LOCKED(10004,"账号已被锁定，联系管理员"),

    USER_CHAT_LIMITED(30001, "用户当日聊天功能已达到上限！"),
    USER_FILE_UPLOAD_LIMITED(30002, "用户当日文件上传功能已达到上限！"),


    ADMIN_OPERATE_FORBIDDEN(40001, "禁止操作管理员权限的功能！"),
    ADMIN_APIKEY_NULL(40002, "系统API-Key使用繁忙！请稍后再试~"),

    UPLOAD_FILE_ERROR(50001, "文件处理失败，请检查文件页数！"),




    ;

    public final int code;
    public final String msg;

    ResultCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }
}
