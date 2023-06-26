package com.gzhu.funai.controller;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.gzhu.funai.api.openai.constant.OpenAIConst;
import com.gzhu.funai.api.openai.enums.Role;
import com.gzhu.funai.api.openai.req.ChatGPTReq;
import com.gzhu.funai.api.openai.req.ContextMessage;
import com.gzhu.funai.dto.StartGameStreamSessionRequest;
import com.gzhu.funai.dto.StreamOneShotChatRequest;
import com.gzhu.funai.dto.StreamSessionChatRequest;
import com.gzhu.funai.entity.UserApiKeyEntity;
import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.enums.Prompt;
import com.gzhu.funai.enums.SessionType;
import com.gzhu.funai.service.AdminApiKeyService;
import com.gzhu.funai.service.ChatService;
import com.gzhu.funai.service.PromptService;
import com.gzhu.funai.service.UserApiKeyService;
import com.gzhu.funai.utils.ResultCode;
import com.gzhu.funai.utils.ReturnResult;
import com.gzhu.funai.utils.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * @Author: huangpenglong / oujiajun
 * @Date: 2023/3/28 15:00
 */

@RestController
@CrossOrigin
@Slf4j
public class StreamChatController {
    @Resource
    private ChatService chatService;
    @Resource
    private AdminApiKeyService adminApiKeyService;
    @Resource
    private PromptService promptService;
    @Resource
    private UserApiKeyService userApiKeyService;

    private LoadingCache<Long, SseEmitter> sseEmitterMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 手动设置2分钟缓存过期，一次流式请求不可能超过2分钟
                    .expireAfterAccess(2L, TimeUnit.MINUTES)
                    //缓存填充策略
                    .build(sseId -> new SseEmitter(0L));

    /**
     * 获取SSE连接，返回sseID给前端
     * @return
     */
    @GetMapping("/chat/getSseEmitter")
    public SseEmitter getSseEmitter() {

        // 默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 生成sseID 并且通过SseEmitter传递给前端，后续前端通过sseID来发送消息（实现双向通讯）
        long sseEmitterId = SnowflakeIdGenerator.nextId();

        try {
            sseEmitter.send(SseEmitter.event()
                    .id(String.valueOf(sseEmitterId))
                    .data(sseEmitterId)
                    .reconnectTime(3000));

        } catch (IOException e) {
            log.error("获取SSE连接失败！");
            return null;
        }

        sseEmitterMap.put(sseEmitterId, sseEmitter);
        log.info("获取SSE连接成功！");
        return sseEmitter;
    }

    /**
     * 多轮流式对话
     * @param req
     * @return
     */
    @PostMapping(path = "/chat/streamSessionChat")
    public ReturnResult streamSessionChat(@RequestBody @Valid StreamSessionChatRequest req){
        // 若用户上传了apikey则使用用户的，否则采用本系统的
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(req.getUserId(), ApiType.OPENAI);
        String apiKey = userApiKeyEntity != null && !StringUtils.isEmpty(userApiKeyEntity.getApikey())
                ? userApiKeyEntity.getApikey()
                : adminApiKeyService.roundRobinGetByType(ApiType.OPENAI);
        if(apiKey == null){
            return ReturnResult.error().codeAndMessage(ResultCode.ADMIN_APIKEY_NULL);
        }

        SessionType sessionType = SessionType.get(req.getSessionType());
        ChatGPTReq gptReq  = ChatGPTReq.builder()
                .model(OpenAIConst.MODEL_NAME_CHATGPT_3_5)
                .max_tokens(OpenAIConst.MAX_TOKENS - sessionType.maxContextToken)
                .stream(true)
                .build();

        // 获取指定的sseEmitter, 将响应信息通过sseEmitter发送出去
        SseEmitter sseEmitter = sseEmitterMap.get(req.getSseEmitterId());
        if(sseEmitter == null){
            return ReturnResult.error();
        }
        chatService.streamSessionChat(
                req.getUserId(), req.getSessionId(), gptReq, req.getMessage()
                , apiKey, sseEmitter, SessionType.get(req.getSessionType()));

        // 清除缓存
        sseEmitterMap.invalidate(req.getSseEmitterId());
        return ReturnResult.ok();
    }

    /**
     * 单轮流式对话
     * @param req
     * @return
     */
    @PostMapping(path = "/chat/streamOneShotChat")
    public ReturnResult streamOneShotChat(@RequestBody @Valid StreamOneShotChatRequest req){
        // 若用户上传了apikey则使用用户的，否则采用本系统的
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(req.getUserId(), ApiType.OPENAI);
        String apiKey = userApiKeyEntity != null && !StringUtils.isEmpty(userApiKeyEntity.getApikey())
                ? userApiKeyEntity.getApikey()
                : adminApiKeyService.roundRobinGetByType(ApiType.OPENAI);
        if(apiKey == null){
            return ReturnResult.error().codeAndMessage(ResultCode.ADMIN_APIKEY_NULL);
        }

        SessionType sessionType = SessionType.get(req.getSessionType());
        ChatGPTReq gptReq  = ChatGPTReq.builder()
                .model(OpenAIConst.MODEL_NAME_CHATGPT_3_5)
                .messages(ImmutableList.of(new ContextMessage(Role.USER.name, req.getMessage())))
                .max_tokens(OpenAIConst.MAX_TOKENS - sessionType.maxContextToken)
                .stream(true)
                .build();

        // 获取指定的sseEmitter, 将响应信息通过sseEmitter发送出去
        SseEmitter sseEmitter = sseEmitterMap.get(req.getSseEmitterId());
        if(sseEmitter == null){
            return ReturnResult.error();
        }
        chatService.streamOneShotChat(req.getUserId(), gptReq, apiKey, sseEmitter);

        // 清除缓存
        sseEmitterMap.invalidate(req.getSseEmitterId());
        return ReturnResult.ok();
    }

    /**
     * 开始游戏
     * @param req
     * @Arthor: oujiajun
     * @return
     */
    @PostMapping("/chat/game/startGameSession")
    public ReturnResult startGameSession(@RequestBody @Valid StartGameStreamSessionRequest req) {

        // 若用户上传了apikey则使用用户的，否则采用本系统的
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(req.getUserId(), ApiType.OPENAI);
        String apiKey = userApiKeyEntity != null && !StringUtils.isEmpty(userApiKeyEntity.getApikey())
                ? userApiKeyEntity.getApikey()
                : adminApiKeyService.getBestByType(ApiType.OPENAI);
        if(apiKey == null){
            return ReturnResult.error().codeAndMessage(ResultCode.ADMIN_APIKEY_NULL);
        }

        String storyType = req.getStoryType() == null ? "冒险": req.getStoryType();
        String gameStartPrompt = String.format(promptService.getByTopic(Prompt.GAME_START.topic), storyType);

        SessionType sessionType = SessionType.get(req.getSessionType());
        ChatGPTReq gptReq  = ChatGPTReq.builder()
                .model(OpenAIConst.MODEL_NAME_CHATGPT_3_5)
                .max_tokens(OpenAIConst.MAX_TOKENS - sessionType.maxContextToken)
                .stream(true)
                .build();

        // 获取指定的sseEmitter, 将响应信息通过sseEmitter发送出去
        SseEmitter sseEmitter = sseEmitterMap.get(req.getSseEmitterId());
        if(sseEmitter == null){
            return ReturnResult.error();
        }
        chatService.streamSessionChat(req.getUserId(), req.getSessionId(), gptReq,
                gameStartPrompt, apiKey, sseEmitter, sessionType);

        // 清除缓存
        sseEmitterMap.invalidate(req.getSseEmitterId());
        return ReturnResult.ok();
    }

}
