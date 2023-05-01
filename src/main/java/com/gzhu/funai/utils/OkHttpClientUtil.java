package com.gzhu.funai.utils;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/8 10:00
 */
public class OkHttpClientUtil {

    private OkHttpClientUtil(){}

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .connectionPool(new ConnectionPool(50, 1L, TimeUnit.MINUTES)).build();

    public static OkHttpClient getClient(){
        return OK_HTTP_CLIENT;
    }
}
