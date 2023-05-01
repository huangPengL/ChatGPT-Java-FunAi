package com.gzhu.funai.api.openai.req;

import lombok.Data;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 10:42
 */

@Data
public class ContextMessage {
    /**
     * 对话角色，三种形式：system(初始化)、user、assistant
     */
    private String role;
    private String content;

    public ContextMessage() {
    }

    public ContextMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
