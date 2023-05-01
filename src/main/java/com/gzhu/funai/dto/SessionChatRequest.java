package com.gzhu.funai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/17 21:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionChatRequest {

    @Length(max = 2500, message = "消息不能超过2500字！")
    private String message;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private Integer sessionId;

    @JsonProperty("session_type")
    private Integer sessionType = 0;
}
