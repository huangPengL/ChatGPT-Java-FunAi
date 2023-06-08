package com.gzhu.funai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.gzhu.funai.api.openai.ChatGPTApi;
import com.gzhu.funai.api.openai.enums.Role;
import com.gzhu.funai.api.openai.req.*;
import com.gzhu.funai.api.openai.resp.*;
import com.gzhu.funai.api.pinecone.PineconeApi;
import com.gzhu.funai.api.pinecone.req.PineconeDeleteReq;
import com.gzhu.funai.api.pinecone.req.PineconeInsertReq;
import com.gzhu.funai.api.pinecone.req.PineconeQueryReq;
import com.gzhu.funai.api.pinecone.req.PineconeVectorsReq;
import com.gzhu.funai.api.pinecone.resp.PineconeQueryResp;
import com.gzhu.funai.entity.DataSqlEntity;
import com.gzhu.funai.entity.SessionChatRecordEntity;
import com.gzhu.funai.entity.UserSessionEntity;
import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.enums.Prompt;
import com.gzhu.funai.enums.SessionType;
import com.gzhu.funai.exception.BaseException;
import com.gzhu.funai.redis.ChatRedisHelper;
import com.gzhu.funai.service.*;
import com.gzhu.funai.sse.OpenAISessionChatSSEListener;
import com.gzhu.funai.utils.MilvusClientUtil;
import com.gzhu.funai.utils.RecursiveCharacterTextSplitter;
import com.gzhu.funai.utils.ResultCode;
import com.gzhu.funai.utils.VerificationCodeGenerator;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zxw
 * @Desriiption: 文件对话业务实现
 */
@Service
@Slf4j
public class FileChatServiceImpl implements FileChatService {

    @Resource
    private UserSessionService userSessionService;
    @Autowired
    private SessionChatRecordService sessionChatRecordService;
    @Resource
    private AdminApiKeyService adminApiKeyService;
    @Resource
    private PromptService promptService;
    @Autowired
    private TaskExecutor queueThreadPool;
    @Resource
    private ChatRedisHelper chatRedisHelper;

    //缓存填充策略：同步加载。缓存中有则用，没有则调用loadWindowRecordCache获得value并存入缓存中
    private LoadingCache<Integer, Deque<SessionChatRecordEntity>> windowRecordCache =
            Caffeine.newBuilder().initialCapacity(1024)
                    .expireAfterAccess(10L, TimeUnit.MINUTES)  // 手动设置10分钟缓存过期，
                    .build(sessionId -> this.loadWindowRecordCache(sessionId));

    // 缓存某个会话窗口的token总数
    private Map<Integer, Integer> windowRecordTokensCache = new HashMap<>();

    private static final int K = SessionType.PDF_CHAT.maxContextToken;
    private static final int MAX_HISTORY_TOKENS = 500;
    private static final int MAX_VECTOR_CONTENT_TOKENS = K - MAX_HISTORY_TOKENS;

    private static final int TOP_K = 20;

    private static final String CONTENT_FIELD = "docContent";
    private static final String VECTOR_FIELD = "docEmbed";
    private static final String INDEX_NAME = "docEmbedIndex";
    private static final IndexType INDEX_TYPE = IndexType.IVF_FLAT;
    private static final String INDEX_PARAM = "{\"nlist\":1024}";

    // 初始化向量库
    private static final MilvusServiceClient milvusClient = MilvusClientUtil.getMilvusClient();

    /**
     * 将上传的文件处理并存入向量库中
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String uploadFile(MultipartFile multipartFile, String userId, String apiKey, ChatGPTReq chatGPTReq, boolean usePinecone) {

        PDDocument document;
        String doc = "";
        String originalFilename = "";
        String prefixStr;

        File file;
        try {
            originalFilename = multipartFile.getOriginalFilename();
            String[] filename = originalFilename.split("\\.");

            prefixStr = String.join(".", Arrays.copyOfRange(filename, 0, filename.length - 1));

            file = File.createTempFile(VerificationCodeGenerator.generateCode(6), "." + filename[filename.length-1]);
            multipartFile.transferTo(file);

            document = PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            doc = pdfStripper.getText(document);

            file.deleteOnExit();
            document.close();

        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

        String sessionName = prefixStr.length() < 100
                ? prefixStr
                : prefixStr.substring(0, 90) + "...";
        UserSessionEntity userSessionEntity = new UserSessionEntity(userId, sessionName, SessionType.PDF_CHAT.type);
        if(!userSessionService.save(userSessionEntity)){
            log.error("开启新的文件聊天会话失败");
            return null;
        }

        String collectionName = "id_" + userId + "_" + userSessionEntity.getSessionId();

        // 生成向量数据
        DataSqlEntity dataSqlEntity = docToEmbed(doc, apiKey, usePinecone);
        if(dataSqlEntity == null){
            log.error("生成向量数据失败");
            // 用于事务回滚防止保存了多余的userSession
            throw new BaseException(ResultCode.UPLOAD_FILE_ERROR.msg);
        }

        String result = "";
        if(usePinecone){
            int count = dataSqlEntity.getLl().size();
            List<PineconeVectorsReq> vectors = new ArrayList<>();
            List<String> ids = generateIDs(count);
            List<Map<String, String>> contents = generateContent(dataSqlEntity.getContent());
            for(int i = 0; i < count; i++){
                vectors.add(new PineconeVectorsReq(ids.get(i), dataSqlEntity.getLl().get(i), contents.get(i)));
            }
            PineconeInsertReq pineconeIndexReq = PineconeInsertReq.builder().vectors(vectors).namespace(collectionName).build();
            result = PineconeApi.insertEmbedding(pineconeIndexReq, adminApiKeyService.roundRobinGetByType(ApiType.PINECONE));
            log.info("插入{}条数据成功！", result);
        }else {
            MilvusClientUtil.createCollection(8000, collectionName);
            R<MutationResult> insertResp = MilvusClientUtil.insert(dataSqlEntity, collectionName);
            MilvusClientUtil.handleResponseStatus(insertResp);
            // 生成索引
            milvusClient.createIndex(CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName(VECTOR_FIELD)
                    .withIndexName(INDEX_NAME)
                    .withIndexType(INDEX_TYPE)
                    .withMetricType(MetricType.IP)
                    .withExtraParam(INDEX_PARAM)
                    .withSyncMode(Boolean.TRUE)
                    .build());
            result = "处理成功";
        }

        String summaryMessage = getContext(collectionName, promptService.getByTopic(Prompt.PDF_SUMMARY_TEMPLATE.topic), apiKey, null, usePinecone);
        chatGPTReq.setMessages(Collections.singletonList(new ContextMessage(Role.USER.name, summaryMessage)));
        ChatGPTResp resp = ChatGPTApi.sessionReq(chatGPTReq, apiKey);

        log.info("userId:{}, summaryMessage:{} ",userId, summaryMessage);

        if(resp == null){
            // 移除会话
            userSessionService.removeById(userSessionEntity.getSessionId());
            sessionChatRecordService.truncateSessionChatRecord(userSessionEntity.getSessionId());
            return null;
        }
        log.info("远方的ChatGPT对userId={}的用户在session_id={}的会话中应答成功，" +
                        "响应token数量为{}, 响应信息为{}",
                userId, userSessionEntity.getSessionId(),
                resp.getUsage().getCompletion_tokens(), resp.getMessage());

        // 构造应答的数据对象
        SessionChatRecordEntity replyRecord = new SessionChatRecordEntity(
                userSessionEntity.getSessionId(), Role.ASSISTANT.name, resp.getMessage(), resp.getUsage().getCompletion_tokens());

        // 持久化数据
        sessionChatRecordService.save(replyRecord);

        if(!usePinecone){
            // 异步处理持久化和缓存更新
            queueThreadPool.execute(()->
                // 释放连接
                MilvusClientUtil.releaseCollection(collectionName)
            );
        }

        return result;
    }


    /**
     *  和文件对话
     * @param userId
     * @param sessionId
     * @param message
     * @param apiKey
     * @return
     */
    @Override
    public ChatGPTResp chatWithFile(String userId, Integer sessionId, String message, String apiKey, ChatGPTReq chatGPTReq, boolean usePinecone) {

        String collectionName = "id_" + userId +"_" + sessionId;
        Deque<SessionChatRecordEntity> windowRecords = new LinkedList<>(windowRecordCache.get(sessionId));

        // 优化问题
        String newQaPrompt = getStandaloneQuestion(message, windowRecords, sessionId, apiKey, userId);

        ChatGPTReq cgr = ChatGPTReq.builder().temperature(0.0).messages(
                Collections.singletonList(new ContextMessage(Role.USER.name, newQaPrompt))).build();
        String newMessage;
        ChatGPTResp qaresp = ChatGPTApi.sessionReq(cgr, apiKey);

        if(qaresp == null || qaresp.getMessage().isEmpty()){
            newMessage = message;
            log.error("远方的ChatGPT对userId={}的用户在session_id={}的会话中优化问题失败！！");
        }
        else {
            newMessage = qaresp.getMessage();
            log.info("问题优化成功，新的问题是：{}", newMessage);
        }

        // 根据优化后的问题 和 索引信息，构造文件对话问题。如果需要用回v1版本只需要将windowRecords设置为null
        String dialogueMessage = getContext(collectionName, newMessage, apiKey, windowRecords, usePinecone);
        log.info("dialogueMessage:{} ", dialogueMessage);
        chatGPTReq.setMessages(Collections.singletonList(new ContextMessage(Role.USER.name, dialogueMessage)));
        ChatGPTResp resp = ChatGPTApi.sessionReq(chatGPTReq, apiKey);

        // 请求失败，则把刚刚的问题撤回
        if(resp == null){
            log.error("远方的ChatGPT对userId={}的用户在session_id={}的会话中应答失败！！");
            return null;
        }
        log.info("远方的ChatGPT对userId={}的用户在session_id={}的会话中应答成功，响应token数量为{}。正准备将响应信息给前端...",
                userId, sessionId, resp.getUsage().getCompletion_tokens());

        // 构造询问和应答的数据对象
        SessionChatRecordEntity askRecord = new SessionChatRecordEntity(
                sessionId, Role.USER.name, message, ChatGPTApi.getMessageTokenNum(message));
        SessionChatRecordEntity replyRecord = new SessionChatRecordEntity(
                sessionId, Role.ASSISTANT.name, resp.getMessage(), resp.getUsage().getCompletion_tokens());

        // 异步处理持久化和缓存更新
        queueThreadPool.execute(()->{
            // 持久化数据
            sessionChatRecordService.saveBatch(ImmutableList.of(askRecord, replyRecord));

            // 更新[会话窗口]缓存数据。
            windowRecordCache.refresh(sessionId);

            if(!usePinecone){
                // 释放连接
                MilvusClientUtil.releaseCollection(collectionName);
            }
        });

        return resp;
    }

    // 文件流式对话
    @Override
    public void streamChatWithFile(String userId, Integer sessionId, String message, String apiKey, ChatGPTReq chatGPTReq, SseEmitter sseEmitter, boolean usePinecone){

        String collectionName = "id_" + userId +"_" + sessionId;

        /* ↓优化问题 */
        // 从缓存中获取[会话窗口]
        Deque<SessionChatRecordEntity> windowRecords = new LinkedList<>(windowRecordCache.get(sessionId));

        String newQaPrompt = getStandaloneQuestion(message, windowRecords, sessionId, apiKey, userId);

        ChatGPTReq cgr = ChatGPTReq.builder().temperature(0.0).messages(
                Collections.singletonList(new ContextMessage(Role.USER.name, newQaPrompt))).build();
        String newMessage;
        ChatGPTResp qaresp = ChatGPTApi.sessionReq(cgr, apiKey);

        if(qaresp == null || qaresp.getMessage().isEmpty()){
            newMessage = message;
            log.error("远方的ChatGPT对userId={}的用户在session_id={}的会话中优化问题失败！！");
        }
        else {
            newMessage = qaresp.getMessage();
            log.info("问题优化成功，新的问题是：{}", newMessage);
        }

        // 根据优化后的问题 和 索引信息，构造文件对话问题。如果需要用回v1版本只需要将windowRecords设置为null
        String dialogueMessage = getContext(collectionName, newMessage, apiKey, windowRecords, usePinecone);
        log.info("dialogueMessage:{} ", dialogueMessage);
        chatGPTReq.setMessages(Collections.singletonList(new ContextMessage(Role.USER.name, dialogueMessage)));

        // 构建管道
        SessionChatRecordEntity askRecord = new SessionChatRecordEntity(
                sessionId, Role.USER.name, message, ChatGPTApi.getMessageTokenNum(message));

        if(usePinecone){
            collectionName = null;
        }

        ChatGPTApi.streamSessionReq(
                chatGPTReq,
                apiKey,
                new OpenAISessionChatSSEListener(sseEmitter, askRecord, collectionName, SessionType.PDF_CHAT));
    }

    // 获取一个独立的问题，确保ChatGPT不会回答文章无关的内容
    private String getStandaloneQuestion(String message, Deque<SessionChatRecordEntity> windowRecords, Integer sessionId, String apiKey, String userId){

        message = message.trim().replace("\n", " ");
        String qaPromptTemplate = promptService.getByTopic(Prompt.QA_PROMPT_TEMPLATE.topic);
        if(windowRecords == null){
            return null;
        }

        if(CollUtil.isEmpty(windowRecords)){
            return String.format(qaPromptTemplate, "", message);
        }

        // 获取[会话窗口]的token总数
        int windowRecordsTokens = windowRecordTokensCache.getOrDefault(sessionId, 0);

        log.info("userId={}的用户获取session_id={}的会话窗口成功，上下文窗口大小为：{}," +
                        " 总tokens数为：{}，正在使用的apiKey为：{}， 当前询问的内容为：【{}】",
                userId, sessionId, windowRecords.size(), windowRecordsTokens, apiKey, message);

        // 若[会话窗口]加入当前对话后，token总数一旦超过K，那就把前面的记录弹出。 windowRecords.size() * 2 是对应的"user"
        int askTokenNum = ChatGPTApi.getMessageTokenNum(message);
        int qaPromptNum = ChatGPTApi.getTokenNum(qaPromptTemplate);
        SessionChatRecordEntity firstRecord = windowRecords.pollFirst();
        while(!windowRecords.isEmpty() &&  windowRecordsTokens + askTokenNum + qaPromptNum + (windowRecords.size() + 1) * 2 > K ){
            windowRecordsTokens -= windowRecords.pollFirst().getTokenNum();
        }
        windowRecords.offerFirst(firstRecord);
        StringBuilder chatHistory = new StringBuilder();
        for(SessionChatRecordEntity sessionChatRecordEntity : windowRecords){
            chatHistory.append(sessionChatRecordEntity.getRole() + ": "
                    + sessionChatRecordEntity.getContent() + "\n");
        }
        windowRecords.offerLast(new SessionChatRecordEntity(Role.USER.name, message));

        // 优化问题
        return String.format(qaPromptTemplate, chatHistory, message);
    }

    /**
     *  搜索相似上下文，并且构造最终问题
     * @param collectionName
     * @param message
     * @param apiKey
     * @return
     */
    private String getContext(String collectionName, String message, String apiKey, Deque<SessionChatRecordEntity> windowRecords, boolean usePinecone) {
        /* ↓检索文档 */
        EmbeddingResp embed = ChatGPTApi.embeddings(Collections.singletonList(message), apiKey);
        if(embed == null){
            return null;
        }
        List<String> orderedCandidates;
        if(usePinecone){
            List<Float> qaEmbed = embed.getData().get(0).getEmbedding();
            PineconeQueryReq pineconeQueryReq = PineconeQueryReq.builder().namespace(collectionName).topK(TOP_K).includeMetadata(true).vector(qaEmbed).build();
            PineconeQueryResp pineconeQueryResp = PineconeApi.queryEmbedding(pineconeQueryReq, adminApiKeyService.getBestByType(ApiType.PINECONE));
            orderedCandidates = pineconeQueryResp.getMatches().stream()
                    .map(match -> match.getMetadata().get("content"))
                    .collect(Collectors.toList());
        }else{
            MilvusClientUtil.loadCollection(collectionName);
            List<List<Float>> qaEmbed = embed.getData().stream().map(Embedding::getEmbedding).collect(Collectors.toList());
            R<SearchResults> searchResults = MilvusClientUtil.searchContent(qaEmbed, collectionName);
            SearchResultsWrapper wrapper = new SearchResultsWrapper(searchResults.getData().getResults());
            orderedCandidates = (List<String>) wrapper.getFieldData(CONTENT_FIELD, 0);
        }

        /* ↓构造问答 */
        // 添加历史记录在最终问答
        int historyWindowRecordsTokens = 0;
        StringBuilder chatHistory = new StringBuilder();
        if(!CollUtil.isEmpty(windowRecords)){

            // 最后一个问题不要，因为最后一个问题是刚问的
            windowRecords.pollLast();

            // 保留第一个问题，控制历史记录总token数不超过 maxHistoryTokens
            Deque<String> tempList = new LinkedList<>();
            SessionChatRecordEntity firstRecord = windowRecords.pollFirst();
            historyWindowRecordsTokens += firstRecord.getTokenNum();

            while(!windowRecords.isEmpty() &&  historyWindowRecordsTokens < MAX_HISTORY_TOKENS){
                SessionChatRecordEntity sessionChatRecordEntity = windowRecords.pollLast();
                if(sessionChatRecordEntity.getRole().equals(Role.USER.name)){
                    historyWindowRecordsTokens += sessionChatRecordEntity.getTokenNum();
                    tempList.offerFirst(sessionChatRecordEntity.getRole() + ": "
                            + sessionChatRecordEntity.getContent() + "\n");
                }
            }
            tempList.offerFirst(firstRecord.getRole() + ": "
                    + firstRecord.getContent() + "\n");
            for(String str : tempList){
                chatHistory.append(str);
            }
        }
        log.info("当前历史记录总token数为{}", historyWindowRecordsTokens);

        // 添加相关上下文在最终问答
        StringBuilder context = new StringBuilder();
        int contextRecordsTokens =
                ChatGPTApi.getTokenNum(promptService.getByTopic(Prompt.FINAL_PROMPT_TEMPLATE.topic)) +
                ChatGPTApi.getTokenNum(message);
        int count = 0;
        for(String candidate: orderedCandidates){
            int curCandidateToken = ChatGPTApi.getTokenNum(candidate);
            if(curCandidateToken + contextRecordsTokens > MAX_VECTOR_CONTENT_TOKENS){
                break;
            }
            context.append(candidate);
            contextRecordsTokens += curCandidateToken;
            count++;
        }
        log.info("当前向量相关上下文总token数为{}，向量个数为{}", contextRecordsTokens, count);

        return String.format(promptService.getByTopic(Prompt.FINAL_PROMPT_TEMPLATE.topic), context.toString(), chatHistory.toString(), message);
    }

    /**
     *   退出登录或者关闭聊天界面的时候删除向量库连接和索引
     * @param userId
     * @return
     */
    @Override
    public boolean dropCollection(String userId, Integer sessionId, boolean usePinecone) {
        String collectionName = "id_" + userId +"_" + sessionId;
        if(usePinecone){
            PineconeDeleteReq pineconeDeleteReq = PineconeDeleteReq.builder().deleteAll(true).namespace(collectionName).build();
            PineconeApi.deleteEmbedding(pineconeDeleteReq, adminApiKeyService.getBestByType(ApiType.PINECONE));
        }else {
            R<RpcStatus> response1 = milvusClient.dropIndex(DropIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withIndexName(INDEX_NAME)
                    .build());
            R<RpcStatus> response2 = milvusClient.dropCollection(DropCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build());
            MilvusClientUtil.handleResponseStatus(response1);
            MilvusClientUtil.handleResponseStatus(response2);
        }
        return true;
    }




    /**
     *  文章转成向量
     * @param doc
     * @return
     */
    private DataSqlEntity docToEmbed(String doc, String apiKey, boolean usePinecone) {

        DataSqlEntity dse = new DataSqlEntity();
        int tokens = 0;
        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter(null, 1000, 200);

        List<String> content = textSplitter.splitText(doc);

        if(usePinecone && content.size() >= 320){
            return null;
        }

        EmbeddingResp embeddingResp = ChatGPTApi.embeddings(content, apiKey);
        if(embeddingResp == null){
            return null;
        }
        List<List<Float>> ll = embeddingResp.getData().stream().map(Embedding::getEmbedding).collect(Collectors.toList());
        tokens += embeddingResp.getUsage().getTotal_tokens();

        dse.setLl(ll);
        dse.setContent(content);
        dse.setTotal_token(tokens);
        return dse;
    }




    /**
     * 将最近的聊天记录加载到缓存中， 并且保证聊天对话的token总数不超过k
     * @param sessionId
     * @return
     */
    private Deque<SessionChatRecordEntity> loadWindowRecordCache(Integer sessionId){
        List<SessionChatRecordEntity> sessionRecords = sessionChatRecordService.getSessionRecord(sessionId);
        Deque<SessionChatRecordEntity> windowRecords = new LinkedList<>();

        int curSessionTokens = 0;
        int size = sessionRecords.size();
        for(int i=size-1; i>=0; i--){
            SessionChatRecordEntity record = sessionRecords.get(i);
            int tokenNum = record.getTokenNum();
            // 保证加上当前轮次的聊天对话时token总数不超过K
            if(curSessionTokens + tokenNum > K){
                break;
            }
            windowRecords.offerFirst(record);
            curSessionTokens += tokenNum;
        }
        this.windowRecordTokensCache.put(sessionId, curSessionTokens);
        return windowRecords;
    }

    /**
     * 刷新缓存
     * @param sessionId
     */
    @Override
    public void refreshWindowRecordCache(Integer sessionId){
        windowRecordCache.refresh(sessionId);
    }

    // 生成每个向量的id
    private List<String> generateIDs(int count){
        List<String> ids = new ArrayList<>();
        for (long i = 0L; i < count; ++i) {
            ids.add("id_" + i);
        }
        return ids;
    }

    // 生成每个向量对应的文本
    private List<Map<String, String>> generateContent(List<String> contents){
        List<Map<String, String>> finalcontents = new ArrayList<>();

        for(int i = 0; i < contents.size(); i++){
            HashMap<String, String> map = new HashMap<>();
            map.put("content", contents.get(i));
            finalcontents.add(map);
        }
        return finalcontents;
    }
}
