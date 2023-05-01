package com.gzhu.funai.api.openai.resp;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 15:56
 */

@Data
public class ChatGPTResp {
    private String id;
    private String object;
    private Long created;
    private String model;                   // 模型名字
    private Usage usage;                    // 当次请求使用的tokens
    private List<ChoiceMessages> choices;   // ChatGPT返回的结果列表，一般仅有1个返回，所以获取数据只需choices.get(0)

    public String getMessage(){
        if(!CollectionUtils.isEmpty(choices)){
            return choices.get(0).getMessage().getContent();
        }
        return "";
    }
}
