package com.gzhu.funai.api.pinecone.resp;

import lombok.Data;

import java.util.List;

/**
 * @author zxw
 * @Desriiption: 向量库查询响应
 */
@Data
public class PineconeQueryResp {

    private List<String> results;

    /**
     *  匹配的结果
     */
    private List<PineconeQueryDownResp> matches;

    /**
     *  命名空间
     */
    private String namespace;
}
