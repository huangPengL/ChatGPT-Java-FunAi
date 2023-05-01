package com.gzhu.funai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gzhu.funai.entity.AdminApiKeyEntity;
import com.gzhu.funai.enums.ApiType;

import java.util.List;


/**
 * @Author: huangpenglong
 * @Date: 2023/4/10 22:58
 */
public interface AdminApiKeyService extends IService<AdminApiKeyEntity> {

    /**
     * 根据枚举类ApiType获取apikey列表
     * @param apiTypes
     * @return
     */
    List<AdminApiKeyEntity> getListByType(ApiType apiTypes);

    /**
     * 判断当前类型的apikey是否在库内
     * @param apiTypes
     * @param apiKey
     * @return
     */
    boolean contains(ApiType apiTypes, String apiKey);

    /**
     * 根据apikey的类型，使用轮询算法获取一个apiKey
     * @param apiTypes
     * @return
     */
    String roundRobinGetByType(ApiType apiTypes);


    /**
     * 刷新缓存
     */
    void load();

    /**
     * 根据apikey的类型，获取优先级最高的apikey
     * @param apiTypes
     * @return
     */
    String getBestByType(ApiType apiTypes);
}
