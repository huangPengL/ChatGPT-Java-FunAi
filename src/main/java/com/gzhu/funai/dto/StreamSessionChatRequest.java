package com.gzhu.funai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/7 15:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamSessionChatRequest {

    @Length(max = 2500, message = "消息不能超过2500字！")
    private String message;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private Integer sessionId;

    @JsonProperty("sse_emitter_id")
    private Long sseEmitterId;

    @JsonProperty("session_type")
    private Integer sessionType = 0;
}
