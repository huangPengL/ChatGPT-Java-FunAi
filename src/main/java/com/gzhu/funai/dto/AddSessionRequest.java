package com.gzhu.funai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/17 18:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSessionRequest {
    @JsonProperty("user_id")
    private String userId;

    @Length(max = 100, message = "会话名不能超过100字！")
    @JsonProperty("session_name")
    private String sessionName;

    @JsonProperty("type")
    private Integer type;
}
