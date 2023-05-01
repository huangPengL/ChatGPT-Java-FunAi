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
public class RenameSessionRequest {
    @JsonProperty("session_id")
    private Integer sessionId;

    @JsonProperty("session_name")
    @Length(max = 100, message = "会话名不能超过100字！")
    private String sessionName;
}
