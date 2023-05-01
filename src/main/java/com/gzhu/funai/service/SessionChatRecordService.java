package com.gzhu.funai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gzhu.funai.entity.SessionChatRecordEntity;

import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/17 22:11
 */
public interface SessionChatRecordService extends IService<SessionChatRecordEntity> {
    /**
     * 获取聊天记录
     * @param sessionId
     * @return
     */
    List<SessionChatRecordEntity> getSessionRecord(Integer sessionId);

    /**
     * 清空聊天记录
     * @param sessionId
     */
    void truncateSessionChatRecord(Integer sessionId);
}
