package com.gzhu.funai.api.openai.req;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 10:41
 */

@Builder
@Data
public class ChatGPTReq{
    @Builder.Default
    private String model = "gpt-3.5-turbo";     // 模型名字
    private List<ContextMessage> messages;       // 区分上下文，实现多轮对话x
    @Builder.Default
    private String user = "xxx";               // 用户的唯一标识符，可以帮助 OpenAI 监控和检测滥用行为
    @Builder.Default
    private Double temperature = 0.8;           // 控制结果的随机性，值在[0,1]之间，越大表示回复越具有不确定性
    @Builder.Default
    private Boolean stream = false;             // 是否流式输出
    private List<String> stop;                    // 停止输出
    @Builder.Default
    private Integer max_tokens = 500;            // 回复最大的字符数
    @Builder.Default
    private Double frequency_penalty = 0.0;      // [-2,2]之间，该值越大则更倾向于产生不同的内容
    @Builder.Default
    private Double presence_penalty = 0.0;       // [-2,2]之间，该值越大则更倾向于产生不同的内容

}
