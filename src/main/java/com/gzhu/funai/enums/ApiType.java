package com.gzhu.funai.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/13 17:16
 */
public enum ApiType {
    /**
     * 0: openai
     * 1: microsoft
     * 2: 百度
     * 3: 梦网
     * 4: Pinecone
     */
    OPENAI("openai", 0),
    MICROSOFT("microsoft", 1),
    BAIDU("baidu", 2),
    MENGWANG("mengwang", 3),
    PINECONE("pinecone", 4);

    public final String typeName;
    public final Integer typeNo;

    ApiType(String typeName, Integer typeNo){
        this.typeName = typeName;
        this.typeNo = typeNo;
    }

    private static final Map<Integer, ApiType> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(item->item.typeNo, item->item));

    public static boolean contains(Integer type){
        return MAP.containsKey(type);
    }
    public static ApiType get(Integer type){
        return MAP.get(type);
    }
}
