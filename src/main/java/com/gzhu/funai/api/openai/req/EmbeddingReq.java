package com.gzhu.funai.api.openai.req;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zxw
 * @Desriiption: 文本编码请求
 */
@Builder
@Data
public class EmbeddingReq {

    @Builder.Default
    /**
     * 模型名字
     */
    private String model = "text-embedding-ada-002";

    private List<String> input;

    /**
     * 用户的唯一标识符，可以帮助 OpenAI 监控和检测滥用行为
     */
    @Builder.Default
    private String user = "xxx";
}
