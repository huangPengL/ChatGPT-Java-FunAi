package com.gzhu.funai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zxw
 * @Desriiption: chat with file
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatWithFileRequest {

    private MultipartFile file;

    @JsonProperty("user_id") // 该注解用于标注 Java 对象的属性与 JSON 数据中的字段之间的映射关系，用于序列化与反序列化
    private String userId;

}
