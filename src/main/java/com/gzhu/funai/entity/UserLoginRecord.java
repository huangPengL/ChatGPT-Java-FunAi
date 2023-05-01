package com.gzhu.funai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/26 16:58
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_login_record")
public class UserLoginRecord {

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
     * 登录时间
     */
    @TableField(value = "login_time")
    private Date loginTime;

    /**
     * 登录IP
     */
    @TableField(value = "login_ip")
    private String loginIp;

    /**
     * 登录类型，详情见枚举类LoginType
     */
    @TableField(value = "login_type")
    private Integer loginType;
}
