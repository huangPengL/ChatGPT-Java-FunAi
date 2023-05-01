package com.gzhu.funai.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong / zxw
 * @Date: 2023/4/8 17:12
 */
public enum SessionType {
    // 普通聊天
    NORMAL_CHAT(0, 3000),

    // pdf聊天
    PDF_CHAT(1, 3500),

    // 冒险游戏聊天
    GAME_CHAT(2, 3500),

    // 专家聊天
    EXPERT_CHAT(3, 3500);

    public final Integer type;
    /**
     *  聊天上下文窗口的token最大数量
     */
    public final Integer maxContextToken;

    private static final Map<Integer, SessionType> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(item->item.type, item->item));

    public static boolean contains(Integer type){
        return MAP.containsKey(type);
    }
    public static SessionType get(Integer type){
        return MAP.get(type);
    }
    SessionType(int type, int maxContextToken){
        this.type = type;
        this.maxContextToken = maxContextToken;
    }
}
