package com.gzhu.funai.api.openai.resp;

import com.gzhu.funai.api.openai.req.ContextMessage;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 15:59
 */

@Data
@ToString
public class ChoiceMessages {
    private ContextMessage message;     // ChatGPT返回的内容
    private String finish_reason;      // stop 表示内容返回完毕
    private ContextMessage delta;       // 流式输出返回的内容
    private Integer index;
}
