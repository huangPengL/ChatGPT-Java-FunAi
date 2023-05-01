package com.gzhu.funai.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@Author :oujiajun
 *@Date: 2023/4/29 16:48
 *@Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "user_advices")
public class UserAdvicesEntity {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 用户建议
     */
    @TableField(value = "user_advice")
    private String advice;

}
