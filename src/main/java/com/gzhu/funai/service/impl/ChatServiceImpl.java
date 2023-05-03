package com.gzhu.funai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.gzhu.funai.api.openai.ChatGPTApi;
import com.gzhu.funai.api.openai.enums.Role;
import com.gzhu.funai.api.openai.req.ChatGPTReq;
import com.gzhu.funai.api.openai.req.ContextMessage;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import com.gzhu.funai.api.openai.resp.CreditGrantsResp;
import com.gzhu.funai.entity.SessionChatRecordEntity;
import com.gzhu.funai.entity.UserSessionEntity;
import com.gzhu.funai.enums.SessionType;
import com.gzhu.funai.service.*;
import com.gzhu.funai.sse.OpenAIOneShotChatSSEListener;
import com.gzhu.funai.sse.OpenAISessionChatSSEListener;
import com.gzhu.funai.service.helper.ExpertChatHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong / oujiajun
 * @Date: 2023/3/13 17:28
 */

@Service
@Slf4j
public class ChatServiceImpl implements ChatService{
    /**
         缓存某个会话的token总数不超过k的[会话窗口]
         Ps: 关于Caffeine Cache可以参考https://www.cnblogs.com/rickiyang/p/11074158.html
             1 expireAfterAccess(long, TimeUnit):在最后一次访问或者写入后开始计时，在指定的时间后过期。
             假如一直有请求访问该key，那么这个缓存将一直不会过期。
             2 expireAfterWrite(long, TimeUnit): 在最后一次写入缓存后开始计时，在指定的时间后过期。
             3 expireAfter(Expiry): 自定义策略，过期时间由Expiry实现独自计算。
     */
    private LoadingCache<Integer, Deque<SessionChatRecordEntity>> normalWindowRecordCache =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 手动设置10分钟缓存过期，
                    .expireAfterAccess(10L, TimeUnit.MINUTES)
                    //缓存填充策略：同步加载。缓存中有则用，没有则调用loadWindowRecordCache获得value并存入缓存中
                    .build(sessionId -> this.loadNormalWindowRecordCache(sessionId));

    private LoadingCache<Integer, Deque<SessionChatRecordEntity>> gameWindowRecordCache =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 手动设置10分钟缓存过期，
                    .expireAfterAccess(10L, TimeUnit.MINUTES)
                    //缓存填充策略：同步加载。缓存中有则用，没有则调用loadWindowRecordCache获得value并存入缓存中
                    .build(sessionId -> this.loadGameWindowRecordCache(sessionId));


    // 缓存某个会话窗口的token总数
    private Map<Integer, Integer> windowRecordTokensCache = new HashMap<>();

    @Resource
    private UserSessionService userSessionService;
    @Resource
    private SessionChatRecordService sessionChatRecordService;
    @Resource
    private TaskExecutor queueThreadPool;
    @Resource
    private ExpertChatHelper expertChatHelper;


    @Override
    public ChatGPTResp oneShotChat(String userId, ChatGPTReq chatGPTReq, String apiKey) {
        return ChatGPTApi.oneShotReq(chatGPTReq, apiKey);
    }


    @Override
    public void streamOneShotChat(String userId, ChatGPTReq chatGPTReq, String apiKey, SseEmitter sseEmitter) {

        ChatGPTApi.streamSessionReq(
                chatGPTReq,
                apiKey,
                new OpenAIOneShotChatSSEListener(sseEmitter, userId));
    }

    @Override
    public ChatGPTResp sessionChat(String userId, Integer sessionId, ChatGPTReq chatGPTReq,
                                   String message, String apiKey, SessionType sessionType) {
        // 从缓存中获取[会话窗口]
        Deque<SessionChatRecordEntity> windowRecords = getWindowRecordsBySessionTypeAndId(sessionType, sessionId);

        // 获取[会话窗口]的token总数
        int windowRecordsTokens = windowRecordTokensCache.getOrDefault(sessionId, 0);

        log.info("userId={}的用户获取session_id={}的会话窗口成功，上下文窗口大小为：{}," +
                        " 总tokens数为：{}，正在使用的apiKey为：{}， 当前询问的内容为：【{}】",
                userId, sessionId, windowRecords.size(), windowRecordsTokens, apiKey, message);

        String originMsg = message;
        // 专家会话的问题拼上要回复的语言
        if (sessionType.equals(SessionType.EXPERT_CHAT)) {
            message += "(用"+ expertChatHelper.getExpertChatLanguage(sessionId) +"回答)";
        }
        // 若[会话窗口]加入当前对话后，token总数一旦超过K，那就指定从第几个位置开始弹出记录。
        int askTokenNum = ChatGPTApi.getMessageTokenNum(message);
        int pollIndex = sessionType.equals(SessionType.NORMAL_CHAT) ? 0 : 1;
        pollWindowRecordsKeepKToken(windowRecords, askTokenNum + windowRecordsTokens,
                pollIndex, sessionType.maxContextToken);
        windowRecords.offerLast(new SessionChatRecordEntity(Role.USER.name, message));

        // 将[会话窗口]转换成ChatGPT接收的格式，并且发送请求
        List<ContextMessage> askContentList = windowRecords.stream()
                .map(item -> new ContextMessage(item.getRole(), item.getContent()))
                .collect(Collectors.toList());
        chatGPTReq.setMessages(askContentList);
        ChatGPTResp resp = ChatGPTApi.sessionReq(chatGPTReq, apiKey);

        // 请求失败，则把刚刚的问题撤回
        if(resp == null){
            windowRecords.pollLast();
            log.error("远方的ChatGPT对userId={}的用户在session_id={}的会话中应答失败！！");
            return null;
        }
        log.info("远方的ChatGPT对userId={}的用户在session_id={}的会话中应答成功，响应token数量为{}。正准备将响应信息给前端...",
                userId, sessionId, resp.getUsage().getCompletion_tokens());

        if (sessionType.equals(SessionType.EXPERT_CHAT)) {
            askTokenNum = ChatGPTApi.getMessageTokenNum(originMsg);
        }
        // 构造询问和应答的数据对象
        SessionChatRecordEntity askRecord = new SessionChatRecordEntity(
                sessionId, Role.USER.name, originMsg, askTokenNum);
        SessionChatRecordEntity replyRecord = new SessionChatRecordEntity(
                sessionId, Role.ASSISTANT.name, resp.getMessage(), resp.getUsage().getCompletion_tokens());

        // 异步处理持久化和缓存更新
        queueThreadPool.execute(()->{
            // 持久化数据
            sessionChatRecordService.saveBatch(ImmutableList.of(askRecord, replyRecord));

            // 更新[会话窗口]缓存数据。
            if(sessionType.equals(SessionType.NORMAL_CHAT)) {
                normalWindowRecordCache.refresh(sessionId);
            }
            else{
                gameWindowRecordCache.refresh(sessionId);
            }
        });

        return resp;
    }

    @Override
    public void streamSessionChat(String userId, Integer sessionId, ChatGPTReq chatGPTReq,
                                  String message, String apiKey, SseEmitter sseEmitter, SessionType sessionType) {
        // 从缓存中获取[会话窗口]
        Deque<SessionChatRecordEntity> windowRecords = getWindowRecordsBySessionTypeAndId(sessionType, sessionId);

        // 获取[会话窗口]的token总数
        int windowRecordsTokens = windowRecordTokensCache.getOrDefault(sessionId, 0);

        log.info("userId={}的用户获取session_id={}的会话窗口成功，上下文窗口大小为：{}," +
                        " 总tokens数为：{}，正在使用的apiKey为：{}， 当前询问的内容为：【{}】",
                userId, sessionId, windowRecords.size(), windowRecordsTokens, apiKey, message);


        // 若[会话窗口]加入当前对话后，token总数一旦超过K，那就指定从第几个位置开始弹出记录。
        String originMsg = message;
        // 专家会话的问题拼上要回复的语言
        if (sessionType.equals(SessionType.EXPERT_CHAT)) {
            message += "(用"+ expertChatHelper.getExpertChatLanguage(sessionId) +"回答)";
        }
        int askTokenNum = ChatGPTApi.getMessageTokenNum(message);
        int pollIndex = sessionType.equals(SessionType.NORMAL_CHAT) ? 0 : 1;
        pollWindowRecordsKeepKToken(windowRecords, askTokenNum + windowRecordsTokens,
                pollIndex, sessionType.maxContextToken);
        windowRecords.offerLast(new SessionChatRecordEntity(Role.USER.name, message));

        // 将[会话窗口]转换成ChatGPT接收的格式
        List<ContextMessage> askContentList = windowRecords.stream()
                .map(item -> new ContextMessage(item.getRole(), item.getContent()))
                .collect(Collectors.toList());
        chatGPTReq.setMessages(askContentList);
        windowRecords.pollLast();  // 预防请求失败，先弹出最后一条记录

        //  保存专家会话的原始问题的tokenNum
        if (sessionType.equals(SessionType.EXPERT_CHAT)) {
            askTokenNum = ChatGPTApi.getMessageTokenNum(originMsg);
        }
        // 构建管道
        SessionChatRecordEntity askRecord = new SessionChatRecordEntity(
                sessionId, Role.USER.name, originMsg, askTokenNum);

        ChatGPTApi.streamSessionReq(
                chatGPTReq,
                apiKey,
                new OpenAISessionChatSSEListener(sseEmitter,  askRecord, null, sessionType));
    }

    /**
     * 刷新缓存
     * @param sessionId
     */
    @Override
    public void refreshWindowRecordCache(Integer sessionId){

        UserSessionEntity userSessionEntity = userSessionService.getById(sessionId);
        if(userSessionEntity == null) {
            return;
        }
        SessionType sessionType = SessionType.get(userSessionEntity.getType());

        switch (sessionType){
            case NORMAL_CHAT:
                normalWindowRecordCache.refresh(sessionId);
                break;
            case EXPERT_CHAT:
            case GAME_CHAT:
                gameWindowRecordCache.refresh(sessionId);
                break;
            default:
        }
    }


    /**
     * 退出登录清除用户缓存
     * @param userId
     */
    @Override
    public void clearUserCache(String userId) {
        List<Integer> sessionIdList = userSessionService.list(
                new QueryWrapper<UserSessionEntity>()
                        .eq("user_id", userId))
                .stream().map(UserSessionEntity::getSessionId).collect(Collectors.toList());
        for (Integer sessionId : sessionIdList) {
            windowRecordTokensCache.remove(sessionId);
            normalWindowRecordCache.invalidate(sessionId);
            gameWindowRecordCache.invalidate(sessionId);
        }
    }

    @Override
    public CreditGrantsResp creditGrants(String apiKey) {
        return ChatGPTApi.creditGrants(apiKey);
    }


    /**
     * 将最近的 [普通聊天] 记录加载到缓存中， 并且保证聊天对话的token总数不超过k
     * @param sessionId
     * @return Deque<SessionChatRecordEntity>
     */
    private Deque<SessionChatRecordEntity> loadNormalWindowRecordCache(Integer sessionId){
        List<SessionChatRecordEntity> sessionRecords = sessionChatRecordService.getSessionRecord(sessionId);
        Deque<SessionChatRecordEntity> windowRecords = new LinkedList<>();

        if(CollectionUtils.isEmpty(sessionRecords)){
            return windowRecords;
        }

        int curSessionTokens = 0;
        int size = sessionRecords.size();
        for(int i=size-1; i>=0; i--){
            SessionChatRecordEntity record = sessionRecords.get(i);
            int tokenNum = record.getTokenNum();
            // 保证加上当前轮次的聊天对话时token总数不超过最大数量限制
            if(curSessionTokens + tokenNum > SessionType.NORMAL_CHAT.maxContextToken){
                break;
            }
            windowRecords.offerFirst(record);
            curSessionTokens += tokenNum;
        }
        windowRecordTokensCache.put(sessionId, curSessionTokens);
        return windowRecords;
    }

    /**
     * 将最近的 [游戏/专家系统] 聊天记录加载到缓存中，并保留第一条聊天记录， 并且保证聊天对话的token总数不超过k
     * @param sessionId
     * @return Deque<SessionChatRecordEntity>
     */
    private Deque<SessionChatRecordEntity> loadGameWindowRecordCache(Integer sessionId){
        List<SessionChatRecordEntity> sessionRecords = sessionChatRecordService.getSessionRecord(sessionId);
        Deque<SessionChatRecordEntity> windowRecords = new LinkedList<>();

        if(CollectionUtils.isEmpty(sessionRecords)){
            return windowRecords;
        }

        SessionChatRecordEntity firstRecord = sessionRecords.get(0);
        int curSessionTokens = firstRecord.getTokenNum();
        int size = sessionRecords.size();
        for(int i=size-1; i>=1; i--){
            SessionChatRecordEntity record = sessionRecords.get(i);
            int tokenNum = record.getTokenNum();
            // 保证加上当前轮次的聊天对话时token总数不超过K
            if(curSessionTokens + tokenNum > SessionType.GAME_CHAT.maxContextToken){
                break;
            }
            windowRecords.offerFirst(record);
            curSessionTokens += tokenNum;
        }
        windowRecords.offerFirst(firstRecord);
        windowRecordTokensCache.put(sessionId, curSessionTokens);
        return windowRecords;
    }

    /**
     * 指定windowRecords， 从下标为index开始弹出多余聊天记录，保证当前窗口总token数curTokenNum不超过k
     * @param windowRecords
     * @param curTokenNum
     * @param index
     * @return 弹出操作后剩余的窗口总token数
     */
    private int pollWindowRecordsKeepKToken(Deque<SessionChatRecordEntity> windowRecords,
                                            int curTokenNum, int index, int maxTokenNum){
        // 下标为index前的记录先暂时弹出
        Deque<SessionChatRecordEntity> temp = new LinkedList<>();
        while(!windowRecords.isEmpty() && index-- != 0){
            temp.offerLast(windowRecords.pollFirst());
        }

        // 维护窗口记录
        while(!windowRecords.isEmpty() &&  curTokenNum > maxTokenNum){
            curTokenNum -= windowRecords.pollFirst().getTokenNum();
        }

        // 还原下标为index前的记录
        while(!temp.isEmpty()){
            windowRecords.offerFirst(temp.pollLast());
        }
        return curTokenNum;
    }

    /**
     * 根据SessionType和sessionId获取带上下文窗口的聊天记录
     * @param sessionType
     * @param sessionId
     */
    private Deque<SessionChatRecordEntity> getWindowRecordsBySessionTypeAndId(SessionType sessionType, Integer sessionId){
        switch (sessionType){
            case NORMAL_CHAT:
                return new LinkedList<>(normalWindowRecordCache.get(sessionId));
            case EXPERT_CHAT:
            case GAME_CHAT:
                return  new LinkedList<>(gameWindowRecordCache.get(sessionId));
            default:
        }
        return new LinkedList<>();
    }
}
