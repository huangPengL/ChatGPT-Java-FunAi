package com.gzhu.funai.api.openai;

import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.gzhu.funai.api.openai.constant.OpenAIConst;
import com.gzhu.funai.api.openai.enums.OpenAiRespError;
import com.gzhu.funai.api.openai.req.ChatGPTReq;
import com.gzhu.funai.api.openai.req.EmbeddingReq;
import com.gzhu.funai.api.openai.resp.BillingUsage;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import com.gzhu.funai.api.openai.resp.EmbeddingResp;
import com.gzhu.funai.api.openai.resp.CreditGrantsResp;
import com.gzhu.funai.exception.BaseException;
import com.gzhu.funai.global.constant.GlobalConstant;
import com.gzhu.funai.utils.DateTimeFormatterUtil;
import com.gzhu.funai.utils.OkHttpClientUtil;
import com.gzhu.funai.utils.ResultCode;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 10:57
 */

@Slf4j
public class ChatGPTApi{
    private static final String AUTHORIZATION_STR = "Authorization";
    private static Encoding enc;
    static {
        enc = Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);
    }

    /**
     * 一次对话
     * @param gpt
     * @param apiKey
     * @return
     */
    public static ChatGPTResp oneShotReq(ChatGPTReq gpt, String apiKey){
        return sessionReq(gpt, apiKey);
    }

    /**
     * 带上下文的对话
     * Ps：之前使用hutool的HttpRequest写请求，但遇到了handshake_failure 错误。目前换成了OKHttp
     * @param gpt
     * @param apiKey
     * @return
     */
    public static ChatGPTResp sessionReq(ChatGPTReq gpt, String apiKey) {

        Request request = new Request.Builder()
                .url(OpenAIConst.HOST + OpenAIConst.CHATGPT_MAPPING)
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(gpt).toString()))
                .header(AUTHORIZATION_STR, "Bearer " + apiKey)
                .build();
        Response response = null;
        try {
            response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());

                log.error("请求ChatGPT异常! {}", openAiRespError.msg);
                throw new BaseException(openAiRespError.msg);
            }

            String body = response.body().string();

            return JSONUtil.toBean(body, ChatGPTResp.class);

        }
        catch (IOException e) {
            log.error("okHttpClient异常! {}", e.getMessage());
        }
        finally {
            if(response != null){
                response.close();
            }
        }
        return null;
    }

    /**
     * 查询apiKey的余额
     * Ps：之前使用hutool的HttpRequest写请求，但遇到了handshake_failure 错误。目前换成了OKHttp
     * @param apiKey
     * @return
     */
    public static CreditGrantsResp creditGrants(String apiKey){

        Request request = new Request.Builder()
                .url(OpenAIConst.HOST + OpenAIConst.CREDIT_GRANTS_MAPPING)
                .get()
                .header(AUTHORIZATION_STR, "Bearer " + apiKey)
                .build();
        Response response = null;
        try {
            response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());
                log.error("请求ChatGPT异常! {}", openAiRespError.msg);
                throw new BaseException(openAiRespError.msg);
            }

            String body = response.body().string();
            log.info("{}调用查询余额请求,返回值：{}",apiKey, body);

            return JSONUtil.toBean(body, CreditGrantsResp.class);

        }
        catch (IOException e) {
            log.error("okHttpClient异常! {}", e.getMessage());
        }
        finally {
            if(response != null){
                response.close();
            }
        }

        return null;
    }

    /**
     * 以流式输出的方式进行多轮对话
     * @param chatGPTReq
     * @param apiKey
     * @param eventSourceListener
     */
    public static void streamSessionReq(ChatGPTReq chatGPTReq, String apiKey, EventSourceListener eventSourceListener){
        if (Objects.isNull(eventSourceListener)) {
            log.error("参数异常：EventSourceListener不能为空");
            throw new BaseException(ResultCode.EMPTY_PARAM.msg);
        }

        try {
            EventSource.Factory factory = EventSources.createFactory(OkHttpClientUtil.getClient());
            String requestBody = JSONUtil.parseObj(chatGPTReq).toString();
            Request request = new Request.Builder()
                    .url(OpenAIConst.HOST + OpenAIConst.CHATGPT_MAPPING)
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), requestBody))
                    .header(AUTHORIZATION_STR, "Bearer " + apiKey)
                    .build();

            // 绑定请求 和 事件监听器
            factory.newEventSource(request, eventSourceListener);
        }
        catch (Exception e) {
            log.error("请求参数解析异常：{}", e);
        }
    }

    /**
     *   文本编码
     * @param input
     * @param apiKey
     * @return
     */
    public static EmbeddingResp embeddings(List<String> input, String apiKey){
        EmbeddingReq embeddingReq = EmbeddingReq.builder().input(input).build();
        Request request = new Request.Builder()
                .url(OpenAIConst.HOST + OpenAIConst.EMBEDDING_MAPPING)
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(embeddingReq).toString()))
                .header(AUTHORIZATION_STR, "Bearer " + apiKey)
                .build();
        Response response = null;
        try {
            response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());
                log.error("Embedding异常! {}", openAiRespError.msg);
                throw new BaseException(openAiRespError.msg);
            }

            String body = response.body().string();

            return JSONUtil.toBean(body, EmbeddingResp.class);

        }
        catch (IOException e) {
            log.error("okHttpClient异常! {}", e.getMessage());
        }
        finally {
            if(response != null){
                response.close();
            }
        }
        return null;
    }

    /**
     * 估计字符串占多少个token
     * @param message
     * @return
     */
    public static int getTokenNum(String message){
        return enc.encode(message).size();
    }

    /**
     * 估计一轮上下文对话占多少个token
     * @param message
     * @return
     */
    public static int getMessageTokenNum(String message){
        return enc.encode("role: {user}, message: {" + message + "}").size();
    }

    /**
     * 获取apiKey的额度信息
     * @param apiKey
     * @return
     */
    public static BillingUsage getBillingUsage(String apiKey){
        Response subResponse = null;
        Response usageResponse = null;
        try {
            subResponse = OkHttpClientUtil.getClient()
                    .newCall(
                        new Request.Builder()
                        .url(OpenAIConst.HOST + OpenAIConst.SUBSCRIPTION_MAPPING)
                        .get()
                        .header(AUTHORIZATION_STR, "Bearer " + apiKey)
                        .build())
                    .execute();

            // openai请求错误
            if(!subResponse.isSuccessful()){
                OpenAiRespError openAiRespError = OpenAiRespError.get(subResponse.code());
                log.error("请求ChatGPT异常! {}", openAiRespError.msg);
                throw new BaseException(openAiRespError.msg);
            }

            // 判断账号是否过期
            Map subMap = JSON.parseObject(subResponse.body().string(), Map.class);
            long accessUntil = Long.parseLong(String.valueOf(subMap.get("access_until")));
            if(accessUntil * GlobalConstant.TEN_K < System.currentTimeMillis()){
                log.warn("检查到apiKey：{}过期，过期时间{}", apiKey,
                        Instant.ofEpochMilli(accessUntil * GlobalConstant.TEN_K).atZone(ZoneId.systemDefault()).toLocalDate());
                // 不抛异常，因为特殊的apiKey过期了还能使用
                //  throw new BaseException(OpenAiRespError.OPENAI_APIKEY_EXPIRED.code, OpenAiRespError.OPENAI_APIKEY_EXPIRED.msg);
            }

            // 获取总额度
            BigDecimal totalAmount  = BigDecimal.valueOf(Double.parseDouble(String.valueOf(subMap.get("hard_limit_usd"))));

            // 获取已使用额度 (滑动日期窗口获取，因为该死的openai一次只能拿100天的使用额度)
            BigDecimal totalUsage = new BigDecimal(0);
            LocalDate startDate = LocalDate.now().minusDays(95);
            LocalDate endDate = LocalDate.now().plusDays(1);
            while(true){
                // 查询日期范围内的使用额度
                String usageUrl = OpenAIConst.HOST + String.format(
                        OpenAIConst.USAGE_MAPPING,
                        DateTimeFormatterUtil.DFT.format(startDate),
                        DateTimeFormatterUtil.DFT.format(endDate));
                usageResponse = OkHttpClientUtil.getClient()
                        .newCall(new Request.Builder()
                                .url(usageUrl)
                                .get()
                                .header(AUTHORIZATION_STR, "Bearer " + apiKey)
                                .build())
                        .execute();
                Map usageMap = JSON.parseObject(usageResponse.body().string(), Map.class);
                BigDecimal curUsage = BigDecimal.valueOf(Double.parseDouble(String.valueOf(usageMap.get("total_usage"))));

                // 当在某次范围内查出的使用额度为0，说明此前长时间没使用过
                if(curUsage.compareTo(BigDecimal.ZERO) <= 0){
                    break;
                }

                // 累加使用额度
                totalUsage = totalUsage.add(curUsage);

                // 统计日期窗口向前滑动
                endDate = startDate;
                startDate = endDate.minusDays(95);
            }

            return new BillingUsage(
                    totalAmount,
                    totalUsage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP),
                    Instant.ofEpochMilli(accessUntil * GlobalConstant.TEN_K).atZone(ZoneId.systemDefault()).toLocalDate());

        }
        catch (IOException e) {
            log.error("okHttpClient异常! {}", e.getMessage());
        }

        finally {
            if(subResponse != null){
                subResponse.close();
            }
            if(usageResponse != null){
                usageResponse.close();
            }
        }
        return null;
    }


    private ChatGPTApi(){}
}
