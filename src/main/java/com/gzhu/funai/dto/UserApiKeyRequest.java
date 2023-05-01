package com.gzhu.funai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/20 20:02
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserApiKeyRequest {

    /**
     * 用户ID
     */
    @JsonProperty(value = "user_id")
    private String userId;

    @Length(max = 200, message = "apiKey不能超过200字符！")
    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("api_type_no")
    private Integer apiTypeNo;

}
