package com.gzhu.funai.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/26 17:05
 */
public enum LoginType {
    /**
     *  登录类型：
     *  0 普通登录（账号|手机号）
     *  1 微信
     *  2 游客
     */
    NORMAL(0),
    WECHAT(1),
    VISITOR(2),
    ;
    public final Integer  typeNo;

    private static final Map<Integer, LoginType> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(item->item.typeNo, item->item));

    LoginType(Integer typeNo){
        this.typeNo = typeNo;
    }

    public static LoginType get(Integer typeNo){
        return MAP.get(typeNo);
    }
}
