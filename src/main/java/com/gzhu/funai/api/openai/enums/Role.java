package com.gzhu.funai.api.openai.enums;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 11:09
 */
public enum Role {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    public final String name;

    Role(String name){
        this.name = name;
    }
}
