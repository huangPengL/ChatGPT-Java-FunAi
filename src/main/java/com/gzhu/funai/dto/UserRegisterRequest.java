package com.gzhu.funai.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author :wuxiaodong
 * @Date: 2023/3/16 15:08
 * @Description:用户注册信息
 */
@Data
public class UserRegisterRequest {
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 2, max = 10, message="用户名长度在2-10字符")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 2,max = 18,message = "密码必须是2—18位字符")
    private String password;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number")
    @NotEmpty(message = "手机号不能为空")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;

    private String email;
}
