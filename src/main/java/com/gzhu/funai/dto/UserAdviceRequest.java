package com.gzhu.funai.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: oujiajun
 * @Date: 2023/4/29 16:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdviceRequest {
    /**
     * 用户ID
     */
    @JsonProperty(value = "user_id")
    private String userId;

    /**
     * 用户ID
     */
    @JsonProperty(value = "username")
    private String username;

    /**
     * 用户建议
     */
    @Length(max = 500, message = "建议内容不能超过500个字符")
    @JsonProperty("user_advice")
    private String userAdvice;
}
