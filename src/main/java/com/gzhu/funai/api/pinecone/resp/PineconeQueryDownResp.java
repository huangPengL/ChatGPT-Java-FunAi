package com.gzhu.funai.api.pinecone.resp;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zxw
 * @Desriiption: 响应对象的下一级
 */
@Data
public class PineconeQueryDownResp {

    /**
     * 向量id
     */
    private String id;

    /**
     *  相似度分数
     */
    private Float score;

    /**
     *  向量
     */
    private List<Float> values;

    /**
     *  向量的元数据，存放对应文本
     */
    private Map<String, String> metadata;
}
