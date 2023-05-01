package com.gzhu.funai.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * @Author :wuxiaodong
 * @Date: 2023/3/16 15:04
 * @Description:前端用户登录信息
 */
@Data
public class UserLoginRequest {
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 2, max = 15, message="用户名/手机号长度在2-15字符")
    private String loginAcct;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 2,max = 18,message = "密码必须是2—18位字符")
    private String password;

    /**
     * 登录类型，详情见LoginType枚举类
     */
    private Integer loginType;
}
