package com.gzhu.funai.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/26 11:19
 * @Description:
 */
@Data
public class UserListRequest {
//    查询条件，用户名/手机号
    private String key;
    private Date startTime;
    private Date endTime;
    private Integer level;
    private Integer status;
}
