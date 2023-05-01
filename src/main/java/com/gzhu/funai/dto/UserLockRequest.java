package com.gzhu.funai.dto;

import lombok.Data;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/26 13:58
 * @Description:
 */
@Data
public class UserLockRequest {
    private String userId;
    private Byte status;
}
