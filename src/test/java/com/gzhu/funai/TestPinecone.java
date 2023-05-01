package com.gzhu.funai;

import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.gzhu.funai.api.openai.enums.OpenAiRespError;
import com.gzhu.funai.api.pinecone.req.PineconeDeleteReq;
import com.gzhu.funai.api.pinecone.req.PineconeInsertReq;
import com.gzhu.funai.api.pinecone.req.PineconeQueryReq;
import com.gzhu.funai.api.pinecone.req.PineconeVectorsReq;
import com.gzhu.funai.api.pinecone.resp.PineconeQueryResp;
import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.exception.BaseException;
import com.gzhu.funai.service.AdminApiKeyService;
import com.gzhu.funai.utils.OkHttpClientUtil;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author zxw
 * @Desriiption: 测试使用API Reference连接Pinocone向量库
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPinecone {

    @Resource
    private AdminApiKeyService adminApiKeyService;

    private String PINECONE_URL = "https://docemb-55f2510.svc.us-west4-gcp.pinecone.io";
    @Test
    public void testDescribeIndexStats(){

        Request request = new Request.Builder()
                .url(PINECONE_URL + "/describe_index_stats")
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Api-Key","xxx")
                .build();
        try {
            Response response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());
                throw new BaseException(openAiRespError.msg);
            }
            String body = response.body().string();
            System.out.println(body);
        } catch (IOException e) {
        }
    }

    @Test
    public void testInsert(){
        String apiKey = adminApiKeyService.getBestByType(ApiType.PINECONE);
        int count = 340;
        List<PineconeVectorsReq> vectors = new ArrayList<>();
        List<List<Float>> values = generateFloatVectors(count);
        List<String> ids = generateIDs(count);
        List<Map<String, String>> contents = generateContent(count);

        for(int i = 0; i < count; i++){

            vectors.add(new PineconeVectorsReq(ids.get(i), values.get(i), contents.get(i)));
        }

        for(int i = 0; i < 1; i++){
            PineconeInsertReq pineconeIndexReq = PineconeInsertReq.builder().vectors(vectors).namespace("userIdsessionId_" + i).build();

            Request request = new Request.Builder()
                    .url(PINECONE_URL + "/vectors/upsert")
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(pineconeIndexReq).toString()))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("Api-Key",apiKey)
                    .build();

            try {
                Response response = OkHttpClientUtil.getClient().newCall(request).execute();

                if(!response.isSuccessful()){
                    OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());
                    throw new BaseException(openAiRespError.msg);
                }

                String body = response.body().string();

                System.out.println(body);

            } catch (IOException e) {
            }
        }
    }

    /**
     *  AsyncHttpClient client = new DefaultAsyncHttpClient();
     client.prepare("POST", "https://docemb-55f2510.svc.us-west4-gcp.pinecone.io/query")
     .setHeader("accept", "application/json")
     .setHeader("content-type", "application/json")
     .setHeader("Api-Key", "fabdfedd-344e-4ce4-b0c2-d4a1169ffafd")
     .setBody("{\"includeValues\":\"false\",\"includeMetadata\":true,\"namespace\":\"userIdsessionId_0\",\"topK\":20}")
     .execute()
     .toCompletableFuture()
     .thenAccept(System.out::println)
     .join();
     ​
     client.close();

     */
    @Test
    public void testQuery(){
        Random ran = new Random();
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < 1536; ++i) {
            vector.add(ran.nextFloat());
        }
        PineconeQueryReq pineconeQueryReq = PineconeQueryReq.builder().namespace("userIdsessionId_0").topK(20).includeMetadata(true).vector(vector).build();

        Request request = new Request.Builder()
                .url(PINECONE_URL + "/query")
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(pineconeQueryReq).toString()))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Api-Key","xxx")
                .build();
        try {
            Response response = OkHttpClientUtil.getClient().newCall(request).execute();

            if(!response.isSuccessful()){
                OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());
                throw new BaseException(openAiRespError.msg);
            }

            String body = response.body().string();

            System.out.println(body);

            PineconeQueryResp pineconeQueryResp = JSONUtil.toBean(body, PineconeQueryResp.class);



            System.out.println(pineconeQueryResp);

        } catch (IOException e) {
        }
    }


    @Test
    public void testDelete(){
        for(int i = 0; i < 1; i++){
            PineconeDeleteReq pineconeDeleteReq = PineconeDeleteReq.builder().deleteAll(true).namespace("userIdsessionId_" + i).build();

            Request request = new Request.Builder()
                    .url(PINECONE_URL + "/vectors/delete")
                    .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), JSONUtil.parseObj(pineconeDeleteReq).toString()))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("Api-Key","fabdfedd-344e-4ce4-b0c2-d4a1169ffafd")
                    .build();
            try {
                Response response = OkHttpClientUtil.getClient().newCall(request).execute();

                if(!response.isSuccessful()){
                    OpenAiRespError openAiRespError = OpenAiRespError.get(response.code());
                    throw new BaseException(openAiRespError.msg);
                }

                String body = response.body().string();

                System.out.println(body);

            } catch (IOException e) {
            }
        }
    }

    private List<Map<String, String>> generateContent(int count){
        List<Map<String, String>> contents = new ArrayList<>();

        for(int i = 0; i < count; i++){
            HashMap<String, String> map = new HashMap<>();
            map.put("content", "content: " + i);
            contents.add(map);
        }
        return contents;
    }

    private List<String> generateIDs(int count){
        List<String> ids = new ArrayList<>();
        for (long i = 0L; i < count; ++i) {
            ids.add("id_" + i);
        }
        return ids;
    }


    private List<List<Float>> generateFloatVectors(int count) {
        Random ran = new Random();
        List<List<Float>> vectors = new ArrayList<>();
        for (int n = 0; n < count; ++n) {
            List<Float> vector = new ArrayList<>();
            for (int i = 0; i < 1536; ++i) {
                vector.add(ran.nextFloat());
            }
            vectors.add(vector);
        }

        return vectors;
    }
}
