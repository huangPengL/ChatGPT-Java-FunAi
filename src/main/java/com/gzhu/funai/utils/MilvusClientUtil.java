package com.gzhu.funai.utils;

import com.google.common.collect.ImmutableList;
import com.gzhu.funai.entity.DataSqlEntity;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.GetCollStatResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: huangpenglong / zxw
 * @Date: 2023/4/11 13:53
 */
@Slf4j
public class MilvusClientUtil {

    private MilvusClientUtil(){}

    private static final Integer SEARCH_K = 25;
    private static final String SEARCH_PARAM = "{\"nprobe\":1024}";
    private static final Integer VECTOR_DIM = 1536;
    private static final Integer MAX_LENGTH = 8192;
    private static final String ID_FIELD = "docID";
    private static final String CONTENT_FIELD = "docContent";
    private static final String VECTOR_FIELD = "docEmbed";
    private static final Integer SHARDS_NUM = 8;

    /**
     * 使用milvus作为向量数据库需要填写，否则使用Pinecone向量库
     * TODO
     */
    private static final MilvusServiceClient milvusClient = new MilvusServiceClient(
            ConnectParam.newBuilder()
                    .withHost("xx.xx.xx.xx")
                    .withPort(19530)
                    .build());


    public static MilvusServiceClient getMilvusClient() {
        return milvusClient;
    }


    /**
     *  创建用户的库
     */
    public static R<RpcStatus> createCollection(long timeoutMilliseconds, String collectionName) {

        FieldType docIdentification = FieldType.newBuilder()
                .withName(ID_FIELD)
                .withDescription("doc identification")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType docEmbedding = FieldType.newBuilder()
                .withName(VECTOR_FIELD)
                .withDescription("doc embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(VECTOR_DIM)
                .build();

        FieldType docContent = FieldType.newBuilder()
                .withName(CONTENT_FIELD)
                .withDescription("doc content")
                .withDataType(DataType.VarChar)
                .withMaxLength(MAX_LENGTH)
                .build();


        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("doc info")
                .withShardsNum(SHARDS_NUM)
                .addFieldType(docIdentification)
                .addFieldType(docEmbedding)
                .addFieldType(docContent)
                .build();
        R<RpcStatus> response = milvusClient.withTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS)
                .createCollection(createCollectionReq);
        handleResponseStatus(response);
        return response;
    }

    // 判断是否有了这个连接
    public static boolean hasCollection(String collectionName) {
        R<Boolean> response = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        handleResponseStatus(response);
        return response.getData();
    }

    /*
   加载连接
 */
    public static R<RpcStatus> loadCollection(String collectionName) {
        R<RpcStatus> response = milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        handleResponseStatus(response);
        return response;
    }

    /**
     *  将文章向量插入向量库中
     */
    public static R<MutationResult> insert(DataSqlEntity dataSqlEntity, String collectionName) {
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(CONTENT_FIELD, dataSqlEntity.getContent()));
        fields.add(new InsertParam.Field(VECTOR_FIELD, dataSqlEntity.getLl()));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        R<MutationResult> response = milvusClient.insert(insertParam);
        handleResponseStatus(response);
        return response;
    }

    /**
     *  释放连接
     */
    public static R<RpcStatus> releaseCollection(String collectionName) {
        R<RpcStatus> response = milvusClient.releaseCollection(ReleaseCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        handleResponseStatus(response);
        return response;
    }

    /**
     *  搜索相似文档
     * @return
     */
    public static R<SearchResults> searchContent(List<List<Float>> vectors, String collectionName) {
        long begin = System.currentTimeMillis();

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withMetricType(MetricType.IP)
                .withOutFields(ImmutableList.of(CONTENT_FIELD))
                .withTopK(SEARCH_K)
                .withVectors(vectors)
                .withVectorFieldName(VECTOR_FIELD)
                .withParams(SEARCH_PARAM)
                .withGuaranteeTimestamp(Constant.GUARANTEE_EVENTUALLY_TS)
                .build();

        R<SearchResults> response = milvusClient.search(searchParam);
        long end = System.currentTimeMillis();
        long cost = (end - begin);
        log.info("search time cost: {}", cost);

        handleResponseStatus(response);

        return response;
    }

    public static void handleResponseStatus(R<?> r) {
        if (r.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException(r.getMessage());
        }
    }

    // 统计当前连接的数据条目
    public static R<GetCollectionStatisticsResponse> getCollectionStatistics(String collectionName) {
        milvusClient.flushAll(true, 1000, 5000);

        R<GetCollectionStatisticsResponse> response = milvusClient.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        handleResponseStatus(response);
        GetCollStatResponseWrapper wrapper = new GetCollStatResponseWrapper(response.getData());
        System.out.println("Collection row count: " + wrapper.getRowCount());
        return response;
    }
}
