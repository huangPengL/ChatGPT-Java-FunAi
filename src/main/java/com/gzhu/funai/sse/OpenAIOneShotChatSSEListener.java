package com.gzhu.funai.sse;

import cn.hutool.json.JSONUtil;
import com.gzhu.funai.api.openai.enums.OpenAiRespError;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import com.gzhu.funai.redis.ChatRedisHelper;
import com.gzhu.funai.utils.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/28 10:43
 */

@Slf4j
public class OpenAIOneShotChatSSEListener extends EventSourceListener {

    private static final String DONE_SIGNAL = "[DONE]";

    private String userId;

    private SseEmitter sseEmitter;
    public OpenAIOneShotChatSSEListener(SseEmitter sseEmitter, String userId) {
        this.sseEmitter = sseEmitter;
        this.userId = userId;
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
        log.info(data);

        ChatGPTResp resp = JSONUtil.toBean(data, ChatGPTResp.class);
        if(StringUtils.isEmpty(resp)){
            return;
        }
        String content = resp.getChoices().get(0).getDelta().getContent();

        if(StringUtils.isEmpty(content)){
            return;
        }
        content = content.replace(" ", "「`」");
        content = content.replace("\n", "「·」");
        content = content.replace("\t", "「~」");
        sseEmitter.send(SseEmitter.event()
                .data(content)
                .reconnectTime(3000));
    }

    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }

    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if(Objects.isNull(response)){
            log.error("OpenAI  sse连接异常:{}", t);
            eventSource.cancel();
            return;
        }

        ResponseBody body = response.body();
        if(Objects.nonNull(body)){
            OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), openAiRespError.msg);
            sseEmitter.send(SseEmitter.event()
                    .id("error")
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
