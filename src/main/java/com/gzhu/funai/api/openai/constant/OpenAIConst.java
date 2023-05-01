package com.gzhu.funai.api.openai.constant;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/28 0:11
 */
public class OpenAIConst {
    public static final String HOST = "https://api.openai.com/";
    public static final String CHATGPT_MAPPING = "v1/chat/completions";
    /**
     * 查询普通账单
     */
    public static final String CREDIT_GRANTS_MAPPING = "/dashboard/billing/credit_grants";

    /**
     * 查询是否订阅，总额度
     */
    public static final String SUBSCRIPTION_MAPPING = "/v1/dashboard/billing/subscription";

    /**
     * 查询使用量（跨度不能超过100天）
     */
    public static final String USAGE_MAPPING =
            "/v1/dashboard/billing/usage?start_date=%s&end_date=%s";


    public static final String EMBEDDING_MAPPING = "v1/embeddings";

    /**
     * 模型选择
     */
    public static final String MODEL_NAME_CHATGPT_3_5 = "gpt-3.5-turbo";
    public static final String MODEL_NAME_CHATGPT_4 = "gpt-4";

    public static final int MAX_TOKENS = 4000;

    private OpenAIConst(){}
}
