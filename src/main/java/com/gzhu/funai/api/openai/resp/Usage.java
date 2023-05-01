package com.gzhu.funai.api.openai.resp;

import lombok.Data;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 16:01
 */

@Data
public class Usage {
    private Integer prompt_tokens;          // 请求的tokens
    private Integer completion_tokens;      // 响应的tokens
    private Integer total_tokens;
}
