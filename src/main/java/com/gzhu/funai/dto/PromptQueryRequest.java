package com.gzhu.funai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/11 18:48
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromptQueryRequest {

    /**
     * Prompt类型，详情请见PromptType
     */
    private Integer type;

    /**
     * 查询字段
     */
    private String content;

    /**
     * Prompt面向的目标用户，详情请见PromptTarget
     */
    private Integer target;
}
