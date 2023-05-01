package com.gzhu.funai.api.pinecone.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zxw
 * @Desriiption: 需要插入Pinecone向量库的向量对象
 */

@Data
public class PineconeVectorsReq {

    /**
     *  每条向量的id
     */
    private String id;

    /**
     *  分段后每一段的向量
     */
    private List<Float> values;

    /**
     * 向量稀疏数据。表示为索引列表和对应值列表，它们必须具有相同的长度。
     */
    private Map<String, String> sparseValues;

    /**
     *  元数据，可以用来存储向量对应的文本 { key: "content", value: "对应文本" }
     */
    private Map<String, String> metadata;

    public PineconeVectorsReq(){
    }

    public PineconeVectorsReq(String id, List<Float> values, Map<String, String> metadata){
        this.id = id;
        this.values = values;
        this.metadata = metadata;
    }
}
