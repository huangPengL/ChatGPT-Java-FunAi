package com.gzhu.funai.sse;

import cn.hutool.json.JSONUtil;
import com.google.common.collect.ImmutableList;
import com.gzhu.funai.api.openai.ChatGPTApi;
import com.gzhu.funai.api.openai.enums.OpenAiRespError;
import com.gzhu.funai.api.openai.enums.Role;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import com.gzhu.funai.entity.SessionChatRecordEntity;
import com.gzhu.funai.enums.SessionType;
import com.gzhu.funai.service.ChatService;
import com.gzhu.funai.service.FileChatService;
import com.gzhu.funai.service.SessionChatRecordService;
import com.gzhu.funai.utils.MilvusClientUtil;
import com.gzhu.funai.utils.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Objects;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/28 10:43
 */

@Slf4j
public class OpenAISessionChatSSEListener extends EventSourceListener {

    private static final String DONE_SIGNAL = "[DONE]";

    private SessionChatRecordService sessionChatRecordService;
    private ChatService chatService;
    private FileChatService fileChatService;

    private SseEmitter sseEmitter;
    private SessionChatRecordEntity askRecord;
    private StringBuilder respContent;
    private String collectionName;
    private SessionType sessionType;

    /**
     * 增加sessionType属性用于PDF对话释放连接
     */
    public OpenAISessionChatSSEListener(SseEmitter sseEmitter, SessionChatRecordEntity askRecord, String collectionName, SessionType sessionType) {
        this.sseEmitter = sseEmitter;
        this.askRecord = askRecord;
        this.respContent = new StringBuilder();
        this.collectionName = collectionName;
        this.sessionChatRecordService = SpringUtil.getBean("sessionChatRecordServiceImpl");
        this.chatService = SpringUtil.getBean("chatServiceImpl");
        this.fileChatService = SpringUtil.getBean("fileChatServiceImpl");
        this.sessionType = sessionType;
    }

    @SneakyThrows
    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        if (data.equals(DONE_SIGNAL)) {
            log.info("OpenAI返回数据结束");
            sseEmitter.send(SseEmitter.event()
                    .id(DONE_SIGNAL)
                    .data(DONE_SIGNAL)
                    .reconnectTime(3000));

            return;
        }

        ChatGPTResp resp = JSONUtil.toBean(data, ChatGPTResp.class);
        if(StringUtils.isEmpty(resp)){
            return;
        }
        String content = resp.getChoices().get(0).getDelta().getContent();

        if(StringUtils.isEmpty(content)){
            return;
        }

        // 记录流输出结果，用于后续持久化
        this.respContent.append(content);

        // 对流输出进行格式化，传入Emitter通道与前端进行交互
        content = content.replace(" ", "「`」");
        content = content.replace("\n", "「·」");
        content = content.replace("\t", "「~」");
        sseEmitter.send(SseEmitter.event()
                .data(content)
                .reconnectTime(3000));
    }

    @Override
    public void onClosed(EventSource eventSource) {

        // 构造回复数据对象，持久化
        String respContentStr = this.respContent.toString();
        SessionChatRecordEntity replyRecord = new SessionChatRecordEntity(
                this.askRecord.getSessionId(), Role.ASSISTANT.name,
                respContentStr, ChatGPTApi.getMessageTokenNum(respContentStr));
        sessionChatRecordService.saveBatch(ImmutableList.of(this.askRecord, replyRecord));

        // 刷新缓存
        chatService.refreshWindowRecordCache(this.askRecord.getSessionId());

        if(sessionType.type.equals(SessionType.PDF_CHAT.type)) {
            fileChatService.refreshWindowRecordCache(this.askRecord.getSessionId());
        }

        if(!StringUtils.isEmpty(collectionName)){
            // 释放连接
            MilvusClientUtil.releaseCollection(collectionName);
            log.info("向量库连接释放成功");
        }

        log.info("持久化应答数据成功！");
        log.info("OpenAI关闭sse连接...");
    }

    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {

        if(t != null){
            if(t instanceof IOException){
                log.error("网络错误！{}", t.getMessage());
            }
            else{
                log.error("服务错误！{}", t.getMessage());
            }
            eventSource.cancel();
            return ;
        }

        if(Objects.isNull(response)){
            log.error("OpenAI  sse连接异常:{}", t);
            eventSource.cancel();
            return;
        }

        ResponseBody body = response.body();
        if(Objects.nonNull(body)){
            if(response.code() == 200){
                eventSource.cancel();
                return ;
            }
            OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());

            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), openAiRespError.msg);
            sseEmitter.send(SseEmitter.event()
                    .data(openAiRespError.msg)
                    .reconnectTime(3000));
        }
        else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        sseEmitter.send(SseEmitter.event()
                .id(DONE_SIGNAL)
                .data(DONE_SIGNAL)
                .reconnectTime(3000));

        response.close();
        eventSource.cancel();
    }
}
