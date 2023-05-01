package com.gzhu.funai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: oujiajun
 * @Date: 2023/3/30 10:00
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslationRequest {

    @Length(max = 2500, message = "消息不能超过2500字！")
    private String message;

    private String language;
    /**
     * 该注解用于标注 Java 对象的属性与 JSON 数据中的字段之间的映射关系，用于序列化与反序列化
     */
    @JsonProperty("user_id")
    private String userId;
}
