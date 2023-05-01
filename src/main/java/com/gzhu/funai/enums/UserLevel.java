package com.gzhu.funai.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/17 9:32
 */
public enum  UserLevel {
    /**
     *  管理员：每天限制无限制聊天次数、PDF上传次数
     *
     *  普通用户：每天50次聊天次数，3次PDF上传
     *
     *  vip用户：每天无限制聊天次数、PDF上传次数
     *
     *  游客用户： 每天限制10次聊天次数， 1次PDF上传
     */

    ADMIN("管理员", 0, Integer.MAX_VALUE, Integer.MAX_VALUE),
    NORMAL("用户", 1, 50, 3),
    VIP("vip用户", 2, Integer.MAX_VALUE, Integer.MAX_VALUE),
    VISITOR("游客", 3, 10, 1)
    ;

    public final String levelName;
    public final Integer levelNo;
    public final int dailyChatLimit;
    public final int dailyFileUploadLimit;

    private static final Map<Integer, UserLevel> MAP = Arrays.stream(values())
            .collect(Collectors.toMap(item->item.levelNo, item->item));

    UserLevel(String levelName, int levelNo, int dailyChatLimit, int dailyFileUploadLimit) {
        this.levelName = levelName;
        this.levelNo = levelNo;
        this.dailyChatLimit = dailyChatLimit;
        this.dailyFileUploadLimit = dailyFileUploadLimit;
    }

    public static boolean contains(Integer levelNo){
        return MAP.containsKey(levelNo);
    }
    public static UserLevel get(Integer levelNo){
        return MAP.get(levelNo);
    }
}
