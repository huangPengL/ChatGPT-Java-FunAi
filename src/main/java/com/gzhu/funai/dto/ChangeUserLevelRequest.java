package com.gzhu.funai.dto;

import lombok.Data;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/26 16:45
 * @Description:
 */
@Data
public class ChangeUserLevelRequest {
    private String userId;
    private Integer originalLevel;
    private Integer level;
}
