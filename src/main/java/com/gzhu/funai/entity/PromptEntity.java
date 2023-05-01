package com.gzhu.funai.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/11 16:32
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "prompt")
public class PromptEntity {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * prompt的类型, 详情见枚举类PromptType
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * prompt的具体内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 对prompt的描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * prompt的简单文字标识
     */
    @TableField(value = "topic")
    private String topic;

    @JsonProperty("create_time")
    private Date createTime;

    @JsonProperty("update_time")
    private Date updateTime;

    /**
     * 是否被删除,  0: 未删除, 1: 已删除
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(value = "target")
    private Integer target;

}
