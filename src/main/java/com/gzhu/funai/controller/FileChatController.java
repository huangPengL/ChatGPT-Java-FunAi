package com.gzhu.funai.controller;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.gzhu.funai.api.openai.constant.OpenAIConst;
import com.gzhu.funai.api.openai.req.ChatGPTReq;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import com.gzhu.funai.dto.ChatWithFileRequest;
import com.gzhu.funai.dto.SessionChatRequest;
import com.gzhu.funai.dto.StreamSessionChatRequest;
import com.gzhu.funai.entity.UserApiKeyEntity;
import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.enums.SessionType;
import com.gzhu.funai.service.AdminApiKeyService;
import com.gzhu.funai.service.FileChatService;
import com.gzhu.funai.service.UserApiKeyService;
import com.gzhu.funai.utils.ResultCode;
import com.gzhu.funai.utils.ReturnResult;
import com.gzhu.funai.utils.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author zxw
 * @Desriiption: 文件对话
 */
@RestController
@CrossOrigin
@Slf4j
public class FileChatController {

    @Resource
    private FileChatService fileChatService;
    @Resource
    private UserApiKeyService userApiKeyService;
    @Resource
    private AdminApiKeyService adminApiKeyService;

    private static final String NAME_MESSAGE = "message";
    private LoadingCache<Long, SseEmitter> sseEmitterMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 手动设置2分钟缓存过期，一次流式请求不可能超过2分钟
                    .expireAfterAccess(2L, TimeUnit.MINUTES)
                    //缓存填充策略
                    .build(sseId -> new SseEmitter(0L));
    private static final Boolean usePinecone = true;

    /**
     * 获取SSE连接，返回sseID给前端
     * @return
     */
    @GetMapping("/chatFile/getSseEmitter")
    public SseEmitter getSseEmitter() {

        // 默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);

        // 生成sseID 并且通过SseEmitter传递给前端，后续前端通过sseID来发送消息（实现双向通讯）
        long sseEmitterId = SnowflakeIdGenerator.nextId();
        sseEmitterMap.put(sseEmitterId, sseEmitter);

        try {
            sseEmitter.send(SseEmitter.event()
                    .id(String.valueOf(sseEmitterId))
                    .data(sseEmitterId)
                    .reconnectTime(3000));

        } catch (IOException e) {
            log.error("获取SSE连接失败！");
            return null;
        }

        log.info("获取SSE连接成功！");
        return sseEmitter;
    }

    /**
     *  上传文件并生成总结内容
     * @param req
     * @return
     */
    @PostMapping("/file/chatPdfUpload")
    public ReturnResult uploadFile(ChatWithFileRequest req){

        if (ObjectUtils.isEmpty(req.getFile()) || req.getFile().getSize() <= 0 || req.getUserId() == null) {
            return ReturnResult.error().codeAndMessage(ResultCode.EMPTY_PARAM);
        }

        // 若用户上传了apikey则使用用户的，否则采用本系统的
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(req.getUserId(), ApiType.OPENAI);
        String apiKey = userApiKeyEntity != null && !StringUtils.isEmpty(userApiKeyEntity.getApikey())
                ? userApiKeyEntity.getApikey()
                : adminApiKeyService.roundRobinGetByType(ApiType.OPENAI);
        if(apiKey == null){
            return ReturnResult.error().codeAndMessage(ResultCode.ADMIN_APIKEY_NULL);
        }

        ChatGPTReq gptReq = ChatGPTReq.builder().model(OpenAIConst.MODEL_NAME_CHATGPT_3_5).build();
        String result = fileChatService.uploadFile(req.getFile(), req.getUserId(), apiKey, gptReq, usePinecone);
        if(StringUtils.isEmpty(result)){
            return ReturnResult.error().message(ResultCode.UPLOAD_FILE_ERROR.msg);
        }

        return ReturnResult.ok();
    }

    /**
     *  文件对话
     * @param req
     * @return
     */
    @PostMapping("/file/chatWithFile")
    public ReturnResult chatWithFile(@RequestBody @Valid SessionChatRequest req){

        if(StringUtils.isEmpty(req.getMessage()) || req.getUserId() == null  || req.getSessionId() == null){
            return ReturnResult.error().codeAndMessage(ResultCode.EMPTY_PARAM);
        }

        // 若用户上传了apikey则使用用户的，否则采用本系统的
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(req.getUserId(), ApiType.OPENAI);
        String apiKey = userApiKeyEntity != null && !StringUtils.isEmpty(userApiKeyEntity.getApikey())
                ? userApiKeyEntity.getApikey()
                : adminApiKeyService.getBestByType(ApiType.OPENAI);
        if(apiKey == null){
            return ReturnResult.error().codeAndMessage(ResultCode.ADMIN_APIKEY_NULL);
        }

        SessionType sessionType = SessionType.get(req.getSessionType());
        ChatGPTReq gptReq  = ChatGPTReq.builder()
                .model(OpenAIConst.MODEL_NAME_CHATGPT_3_5)
                .max_tokens(OpenAIConst.MAX_TOKENS - sessionType.maxContextToken)
                .build();

        ChatGPTResp resp = fileChatService.chatWithFile(
                req.getUserId(), req.getSessionId(), req.getMessage(), apiKey, gptReq, usePinecone);

        if(resp == null){
            return ReturnResult.error();
        }
        return ReturnResult.ok().data(NAME_MESSAGE, resp.getMessage());
    }

    /**
     *  文件对话,流式
     * @param req
     * @return
     */
    @PostMapping(path = "/file/streamChatWithFile")
    public ReturnResult streamChatWithFile(@RequestBody @Valid StreamSessionChatRequest req){

        if(StringUtils.isEmpty(req.getMessage()) || req.getUserId() == null  || req.getSessionId() == null || req.getSseEmitterId() == null){
            return ReturnResult.error().codeAndMessage(ResultCode.EMPTY_PARAM);
        }

        // 若用户上传了apikey则使用用户的，否则采用本系统的
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(req.getUserId(), ApiType.OPENAI);
        String apiKey = userApiKeyEntity != null && !StringUtils.isEmpty(userApiKeyEntity.getApikey())
                ? userApiKeyEntity.getApikey()
                : adminApiKeyService.getBestByType(ApiType.OPENAI);
        if(apiKey == null){
            return ReturnResult.error().codeAndMessage(ResultCode.ADMIN_APIKEY_NULL);
        }

        ChatGPTReq chatGPTReq = ChatGPTReq.builder()
                .model(OpenAIConst.MODEL_NAME_CHATGPT_3_5)
                .stream(true)
                .build();

        // 获取指定的sseEmitter, 将响应信息通过sseEmitter发送出去
        SseEmitter sseEmitter = sseEmitterMap.get(req.getSseEmitterId());
        if(sseEmitter == null){
            return ReturnResult.error();
        }

        fileChatService.streamChatWithFile(req.getUserId(), req.getSessionId(),
                req.getMessage(), apiKey, chatGPTReq, sseEmitter, usePinecone);

        // 清除缓存
        sseEmitterMap.invalidate(req.getSseEmitterId());
        return ReturnResult.ok();
    }

    /**
     *  删除索引
     * @param userId
     * @return
     */
    @DeleteMapping("/file/dropCollection")
    public ReturnResult dropCollection(@RequestParam(value = "userId") String userId,
                                       @RequestParam(value = "sessionId") Integer sessionId){

        if(StringUtils.isEmpty(userId) && StringUtils.isEmpty(sessionId)){
            return ReturnResult.error().codeAndMessage(ResultCode.EMPTY_PARAM);
        }

        return fileChatService.dropCollection(userId, sessionId, usePinecone) ? ReturnResult.ok() : ReturnResult.error();
    }
}
