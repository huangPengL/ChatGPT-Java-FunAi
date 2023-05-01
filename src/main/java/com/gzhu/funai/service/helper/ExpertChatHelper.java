package com.gzhu.funai.service.helper;

import com.gzhu.funai.api.openai.ChatGPTApi;
import com.gzhu.funai.api.openai.enums.Role;
import com.gzhu.funai.entity.SessionChatRecordEntity;
import com.gzhu.funai.entity.UserSessionEntity;
import com.gzhu.funai.service.PromptService;
import com.gzhu.funai.service.SessionChatRecordService;
import com.gzhu.funai.service.UserSessionService;
import org.springframework.stereotype.Component;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/24 19:18
 * @Description:会话名拆解、prompt构建、持久化聊天记录这些逻辑
 */
@Component
public class ExpertChatHelper {
    private ExpertChatHelper() {}
    public static boolean handleSessionSystemRecord(UserSessionEntity userSessionEntity,SessionChatRecordService sessionChatRecordService,PromptService promptService) {
        String[] split = userSessionEntity.getSessionName().split(":");
        String prompt = promptService.getByTopic(split[split.length - 2]);
        SessionChatRecordEntity sessionChatRecordEntity = new SessionChatRecordEntity(
                userSessionEntity.getSessionId(), Role.SYSTEM.name, prompt, ChatGPTApi.getMessageTokenNum(prompt));
        return sessionChatRecordService.save(sessionChatRecordEntity);
    }

    public static String getExpertChatLanguage(Integer sessionId,UserSessionService userSessionService) {
        UserSessionEntity userSessionEntity = userSessionService.getById(sessionId);
        String[] split = userSessionEntity.getSessionName().split(":");
        return split[split.length - 1];
    }
}
