package com.gzhu.funai.api.pinecone;

import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.gzhu.funai.api.pinecone.req.PineconeDeleteReq;
import com.gzhu.funai.api.pinecone.req.PineconeInsertReq;
import com.gzhu.funai.api.pinecone.req.PineconeQueryReq;
import com.gzhu.funai.api.pinecone.resp.PineconeQueryResp;
import com.gzhu.funai.exception.BaseException;
import com.gzhu.funai.utils.OkHttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author zxw
 * @Desriiption: Pinecone数据库的api
 */
@Slf4j
public class PineconeApi {
    /**
     *  使用Pinecone作为向量数据库需要填写(另外需要在表admin_apikey中插入一条记录，type为4，name为Pinecone的apikey)
     *  TODO
     */
    private static final String PINECONE_API_URL = "https://xxxxxx.pinecone.io";

    // 插入Pinecone向量库
    public static String insertEmbedding(PineconeInsertReq pineconeInsertReq, String apiKey){

        Request request = new Request.Builder()
                .url(PINECONE_API_URL + "/vectors/upsert")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(pineconeInsertReq).toString()))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Api-Key", apiKey)
                .build();
        Response response = null;
        try {
            response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                log.error("插入Pinecone向量库异常：{}", response.message());
                throw new BaseException(response.message());
            }

            String body = response.body().string();

            return body;

        } catch (IOException e) {
            log.error("okHttpClient异常! {}", e.getMessage());
        }
        finally {
            if(response != null){
                response.close();
            }
        }
        return "";
    }

    // 从Pinecone向量库中查询相似
    public static PineconeQueryResp queryEmbedding(PineconeQueryReq pineconeQueryReq, String apiKey){

        Request request = new Request.Builder()
                .url(PINECONE_API_URL + "/query")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(pineconeQueryReq).toString()))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Api-Key", apiKey)
                .build();
        Response response = null;
        try {
            response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                log.error("查询Pinecone向量库异常：{}", response.message());
                throw new BaseException(response.message());
            }

            String body = response.body().string();

            return JSONUtil.toBean(body, PineconeQueryResp.class);

        }
        catch (IOException e) {
            log.error("okHttpClient异常! {}", e.getMessage());
        }
        finally {
            if(response != null) {
                response.close();
            }
        }
        return null;
    }

    // 从Pinecone向量库中删除向量
    public static String deleteEmbedding(PineconeDeleteReq pineconeDeleteReq, String apiKey){

        Request request = new Request.Builder()
                .url(PINECONE_API_URL + "/vectors/delete")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(pineconeDeleteReq).toString()))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Api-Key", apiKey)
                .build();
        Response response = null;
        try {
            response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                log.error("删除Pinecone向量库异常：{}", response.message());
                throw new BaseException(response.message());
            }

            return "删除成功";

        }
        catch (IOException e) {
            log.error("okHttpClient异常! {}", e.getMessage());
        }
        finally {
            if(response != null){
                response.close();
            }
        }
        return "删除失败";
    }
}
