package com.gzhu.funai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/10 22:54
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "admin_apikey")
public class AdminApiKeyEntity {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * api的类型编号, 详情见枚举类ApiType
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 具体的apikey
     */
    @TableField(value = "name")
    private String name;

    /**
     * 是否被删除,  0: 未删除, 1: 已删除
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(value = "priority")
    private Integer priority;

    @TableField(value = "total_amount")
    private BigDecimal totalAmount;

    @TableField(value = "total_usage")
    private BigDecimal totalUsage;

    @TableField(value = "expired_time")
    private LocalDate expiredTime;

    @TableField(value = "is_free")
    private Integer isFree;
}
