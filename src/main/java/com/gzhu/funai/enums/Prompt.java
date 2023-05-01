package com.gzhu.funai.enums;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/12 10:34
 */
public enum  Prompt {
    /**
     *
     */
    GRAMMARLY("语法纠错"),
    TRANSLATE("牛逼翻译"),
    QA_PROMPT_TEMPLATE("PDF对话优化问题"),
    FINAL_PROMPT_TEMPLATE("PDF对话最终问题"),
    PDF_SUMMARY_TEMPLATE("总结PDF提示"),
    GAME_START("文字冒险游戏-开始"),
    ;

    public final String topic;

    Prompt(String topic){
        this.topic = topic;
    }
}
