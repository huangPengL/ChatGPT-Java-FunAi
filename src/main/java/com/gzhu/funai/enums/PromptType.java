package com.gzhu.funai.enums;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/11 16:35
 */
public enum PromptType {
    /**
     * 0: chatgpt
     * 1: midjourney
     */
    CHATGPT("chatgpt", 0),
    MIDJOURNEY("midjourney", 1)
    ;

    public final String typeName;
    public final int typeNo;

    PromptType(String typeName, int typeNo){
        this.typeName = typeName;
        this.typeNo = typeNo;
    }
}
