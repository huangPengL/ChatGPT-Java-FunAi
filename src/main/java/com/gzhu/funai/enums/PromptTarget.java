package com.gzhu.funai.enums;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/17 0:20
 */
public enum  PromptTarget {

    /**
     * 0: 管理员
     * 1: 用户
     */
    ADMIN("管理员", 0),
    USER("用户", 1)
    ;

    public final String targetName;
    public final int targetNo;

    PromptTarget(String targetName, int targetNo){
        this.targetName = targetName;
        this.targetNo = targetNo;
    }
}
