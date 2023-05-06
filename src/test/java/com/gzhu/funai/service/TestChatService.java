package com.gzhu.funai.service;


import com.google.common.collect.ImmutableList;
import com.gzhu.funai.api.openai.constant.OpenAIConst;
import com.gzhu.funai.api.openai.enums.Role;
import com.gzhu.funai.api.openai.req.ChatGPTReq;
import com.gzhu.funai.api.openai.req.ContextMessage;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import com.gzhu.funai.api.openai.resp.CreditGrantsResp;

import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.enums.SessionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/17 23:22
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestChatService {
    @Autowired
    private ChatService chatService;

    @Autowired
    private TaskExecutor queueThreadPool;

    @Resource
    private AdminApiKeyService adminApiKeyService;

    @Test
    public void chatOneShot(){

        System.out.println("正在加载apiKey...");
        adminApiKeyService.load();
        System.out.println("加载apiKey成功");
        ChatGPTReq chatGPTReq = ChatGPTReq.builder()
                .messages(ImmutableList.of(new ContextMessage(Role.USER.name, "你好")))
                .model(OpenAIConst.MODEL_NAME_CHATGPT_3_5)
                .max_tokens(1000)
                .build();
        ChatGPTResp resp = chatService.oneShotChat("", chatGPTReq,
                adminApiKeyService.roundRobinGetByType(ApiType.OPENAI));
        System.out.println(resp.getMessage());
    }

    @Test
    public void chatSession(){
        adminApiKeyService.load();
        ChatGPTReq chatGPTReq = ChatGPTReq.builder().build();
        ChatGPTResp resp = chatService.sessionChat(
                "", 1, chatGPTReq,"我刚刚问了什么",
                adminApiKeyService.roundRobinGetByType(ApiType.OPENAI), SessionType.NORMAL_CHAT);
        System.out.println(resp.getMessage());
    }

    /**
     * 改方法已废弃
     */
    @Test
    public void testCreditGrants(){
        adminApiKeyService.load();
        CreditGrantsResp openAiCreditGrantsResp = chatService.creditGrants(adminApiKeyService.roundRobinGetByType(ApiType.OPENAI));
        System.out.println(openAiCreditGrantsResp);
    }

    @Test
    public void testStreamSessionReq(){
        adminApiKeyService.load();
        String msg = "你好";
        ChatGPTReq chatGPTReq = ChatGPTReq.builder()
                .messages(ImmutableList.of(new ContextMessage(Role.USER.name, msg)))
                .stream(true)
                .build();

        SseEmitter sseEmitter = new SseEmitter(0L);
        chatService.streamSessionChat(
                "", 92, chatGPTReq,msg,
                adminApiKeyService.roundRobinGetByType(ApiType.OPENAI), sseEmitter, SessionType.NORMAL_CHAT);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try{
            countDownLatch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStreamOneShotReq(){
        adminApiKeyService.load();
        String msg = "你好呀";
        ChatGPTReq chatGPTReq = ChatGPTReq.builder()
                .messages(ImmutableList.of(new ContextMessage(Role.USER.name, msg)))
                .stream(true)
                .build();

        SseEmitter sseEmitter = new SseEmitter(0L);
        chatService.streamOneShotChat(
                "", chatGPTReq,
                adminApiKeyService.roundRobinGetByType(ApiType.OPENAI), sseEmitter);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
