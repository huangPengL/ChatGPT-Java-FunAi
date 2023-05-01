package com.gzhu.funai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzhu.funai.entity.SessionChatRecordEntity;
import com.gzhu.funai.mapper.SessionChatRecordMapper;
import com.gzhu.funai.service.SessionChatRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/17 22:12
 */
@Service
public class SessionChatRecordServiceImpl
        extends ServiceImpl<SessionChatRecordMapper, SessionChatRecordEntity>
        implements SessionChatRecordService {

    @Override
    public List<SessionChatRecordEntity> getSessionRecord(Integer sessionId) {
        return baseMapper.selectList(new QueryWrapper<SessionChatRecordEntity>()
                .eq("session_id", sessionId)
                .orderByAsc("session_chat_id")
        );
    }

    @Override
    public void truncateSessionChatRecord(Integer sessionId) {
        baseMapper.delete(new QueryWrapper<SessionChatRecordEntity>().eq("session_id", sessionId));
    }
}
