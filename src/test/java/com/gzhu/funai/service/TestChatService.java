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
        ChatGPTReq chatGPTReq = ChatGPTReq.builder()
                .messages(ImmutableList.of(new ContextMessage(Role.USER.name, "你好，我想在class为chat-text的div元素右侧放入一个高度相同，宽度为10px的按钮块。vue代码如下: <div class=\"chat-friend\" v-if=\"item.role == 'assistant'\"> <div class=\"info-time\"> <img :src=\"item.headImg\" alt=\"\" /> <span>ChatGPT</span> <template v-if=\"!isMobile\"> <span>{{ item.create_time }}</span> </template> </div> <div class=\"chat-text\"> <template v-if=\"isSend && index == chatList.length - 1\"> <span class=\"flash_cursor\"></span> </template> <template v-else> <!-- {{ item.content }} --> <vue-markdown :key='forceRefreshKey'>{{ item.content }}</vue-markdown> </template> </div> </div> Css代码如下：.chat-friend { width: 100%; float: left; margin-bottom: 20px; display: flex; flex-direction: column; justify-content: flex-start; align-items: flex-start; .chat-text { max-width: 90%; padding: 20px; border-radius: 20px 20px 20px 5px; background-color: rgb(56, 60, 75); color: #fff; font-size: 13px; &:hover { background-color: rgb(39, 42, 55); } } .chat-img { img { width: 100px; height: 100px; } } .info-time { margin: 10px 0; color: #fff; font-size: 12px; img { width: 30px; height: 30px; border-radius: 50%; vertical-align: middle; margin-right: 10px; } span:last-child { color: rgb(101, 104, 115); margin-left: 10px; vertical-align: middle; } } }\n")))
                .model(OpenAIConst.MODEL_NAME_CHATGPT_4)
                .max_tokens(4096)
                .build();
        ChatGPTResp resp = chatService.oneShotChat("87429347-0187-42934726-4028f481-0000", chatGPTReq,
                adminApiKeyService.roundRobinGetByType(ApiType.OPENAI));
        System.out.println(resp.getMessage());
    }

    @Test
    public void chatSession(){
        ChatGPTReq chatGPTReq = ChatGPTReq.builder().build();
        ChatGPTResp resp = chatService.sessionChat(
                "", 1, chatGPTReq,"我刚刚问了什么",
                adminApiKeyService.roundRobinGetByType(ApiType.OPENAI), SessionType.NORMAL_CHAT);
        System.out.println(resp.getMessage());
    }

    @Test
    public void testCreditGrants(){
        CreditGrantsResp openAiCreditGrantsResp = chatService.creditGrants(adminApiKeyService.roundRobinGetByType(ApiType.OPENAI));
        System.out.println(openAiCreditGrantsResp);
    }

    @Test
    public void testStreamSessionReq(){
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
