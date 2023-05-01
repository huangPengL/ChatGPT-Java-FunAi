package com.gzhu.funai.service;

import com.gzhu.funai.api.openai.req.ChatGPTReq;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author zxw
 * @Desriiption: 文件对话业务
 */
public interface FileChatService {

    /**
     *  上传文件，并对文件进行处理后存入向量库，生成总结
     * @param file
     * @param userId
     * @param apiKey
     * @param chatGPTReq
     * @param usePinecone 是否使用pinecone向量库，如果是false则使用milvus向量库
     * @return
     */
    String uploadFile(MultipartFile file, String userId, String apiKey, ChatGPTReq chatGPTReq, boolean usePinecone);

    /**
     *  与文件进行对话
     * @param userId
     * @param sessionId
     * @param message
     * @param apiKey
     * @param chatGPTReq
     * @param usePinecone
     * @return
     */
    ChatGPTResp chatWithFile(String userId, Integer sessionId, String message, String apiKey, ChatGPTReq chatGPTReq, boolean usePinecone);

    /**
     *  删除向量库的索引，不可恢复
     * @param userId
     * @param sessionId
     * @param usePinecone
     * @return
     */
    boolean dropCollection(String userId, Integer sessionId, boolean usePinecone);

    /**
     *  与文件进行对话，流式返回
     * @param userId
     * @param sessionId
     * @param message
     * @param apiKey
     * @param chatGPTReq
     * @param sseEmitter
     * @param usePinecone
     */
    void streamChatWithFile(String userId, Integer sessionId, String message, String apiKey, ChatGPTReq chatGPTReq, SseEmitter sseEmitter, boolean usePinecone);

    /**
     *  手动刷新会话窗口缓存记录
     * @param sessionId
     */
    void refreshWindowRecordCache(Integer sessionId);
}
