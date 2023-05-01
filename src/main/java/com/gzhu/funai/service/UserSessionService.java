package com.gzhu.funai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gzhu.funai.entity.UserSessionEntity;
import com.gzhu.funai.enums.SessionType;

import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/17 15:33
 */
public interface UserSessionService extends IService<UserSessionEntity>{

    /**
     * 新增会话
     * @param userId
     * @param sessionName
     * @param sessionType
     * @return
     */
    UserSessionEntity save(String userId, String sessionName, SessionType sessionType);

    /**
     * 查询会话
     * @param userId
     * @param sessionType
     * @return
     */
    List<UserSessionEntity> getSessionList(String userId, SessionType sessionType);
}
