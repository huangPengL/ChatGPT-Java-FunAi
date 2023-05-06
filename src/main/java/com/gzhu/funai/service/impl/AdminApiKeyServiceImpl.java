package com.gzhu.funai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.gzhu.funai.api.openai.ChatGPTApi;
import com.gzhu.funai.api.openai.enums.OpenAiRespError;
import com.gzhu.funai.api.openai.resp.BillingUsage;
import com.gzhu.funai.entity.AdminApiKeyEntity;
import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.exception.BaseException;
import com.gzhu.funai.global.constant.TimeInterval;
import com.gzhu.funai.mapper.AdminApiKeyMapper;
import com.gzhu.funai.redis.AdminApiKeyRedisHelper;
import com.gzhu.funai.service.AdminApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/10 22:59
 */
@Slf4j
@Service
public class AdminApiKeyServiceImpl extends ServiceImpl<AdminApiKeyMapper, AdminApiKeyEntity> implements AdminApiKeyService {

    @Resource
    private TaskExecutor queueThreadPool;
    @Resource
    private AdminApiKeyRedisHelper adminApiKeyRedisHelper;

    private Map<Integer, List<AdminApiKeyEntity>> cache = ImmutableMap.of();
    private int[] roundRobinIndex;

    @Override
    public List<AdminApiKeyEntity> getListByType(ApiType apiTypes) {
        return CollectionUtils.isEmpty(cache) ? null : cache.get(apiTypes.typeNo);
    }

    @Override
    public boolean contains(ApiType apiTypes, String apikey) {
        List<AdminApiKeyEntity> list = this.getListByType(apiTypes);
        for(AdminApiKeyEntity entity: list){
            if(entity.getName().equals(apikey)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String roundRobinGetByType(ApiType apiTypes) {
        String apiKeyName = null;
        if(!CollectionUtils.isEmpty(cache)) {
            // 根据apikey类型获取对应的apikey列表
            List<AdminApiKeyEntity> adminApiKeyEntities = cache.get(apiTypes.typeNo);

            // 只有一个key，无需轮询
            if(adminApiKeyEntities.size() == 1){
                return adminApiKeyEntities.get(0).getName();
            }

            synchronized (AdminApiKeyServiceImpl.class) {
                AdminApiKeyEntity adminApiKeyEntity = null;
                int index;

                int roundTime = 0;
                do {
                    // 根据apikey类型获取轮询下标
                    index = roundRobinIndex[apiTypes.typeNo];

                    // 下标即将越界置为0
                    if (index == Integer.MAX_VALUE) {
                        index = 0;
                    }

                    adminApiKeyEntity = adminApiKeyEntities.get(index % adminApiKeyEntities.size());

                    roundRobinIndex[apiTypes.typeNo] = ++index;
                    roundTime++;
                    if(roundTime > adminApiKeyEntities.size()){
                        return null;
                    }
                }
                // 若当前请求类型是openai且轮询到的key是免费类型的，那么需要判断该key是否受限。(是则继续轮询，否则跳出轮询使用该key)
                while(ApiType.OPENAI.equals(apiTypes) && adminApiKeyEntity.getIsFree() == 1
                        && adminApiKeyRedisHelper.judgeOpenAiFreeKeyLimit(adminApiKeyEntity.getId()));

                apiKeyName = adminApiKeyEntity.getName();

                // 若当前请求类型是openai的轮询到的key是免费类型的，那么需要记录限制信息（1分钟不能超过3次请求）
                if(ApiType.OPENAI.equals(apiTypes) && adminApiKeyEntity.getIsFree() == 1){
                    adminApiKeyRedisHelper.incrOpenAiFreeKeyLimit(adminApiKeyEntity.getId(), 1);
                }
                log.info("正在轮询获取apikey类型为{}的apikey, 当前下标为:{}, apikey为:{}", apiTypes.typeName, index, apiKeyName);
            }
        }
        return apiKeyName;
    }

    /**
     * 1 初始化数据并按照apikey的类型分组
     * 2 重置轮询下标
     * 定时任务：每隔60分钟执行一次
     */
    @Scheduled(initialDelay = TimeInterval.ZERO, fixedRate = TimeInterval.ONE_HOUR)
    @Override
    public void load(){
        Map<Integer, List<AdminApiKeyEntity>> collect = baseMapper.selectList(
                new QueryWrapper<AdminApiKeyEntity>().orderByDesc("priority")).stream()
                .filter(item -> filterInValidOpenAiApiKey(item))
                .collect(Collectors.groupingBy(AdminApiKeyEntity::getType)
                );
        this.cache = ImmutableMap.copyOf(collect);

        if(CollectionUtils.isEmpty(this.cache)){
            this.roundRobinIndex = new int[2];
            return;
        }

        // 若apiKey类型一般不会超过26个，这边手动设置
        this.roundRobinIndex = new int[26];

        log.info("加载AdminApiKey库缓存成功！, 有效的openai的apikey数量:{}",
                cache.get(ApiType.OPENAI.typeNo).size());
    }

    @Override
    public String getBestByType(ApiType apiTypes) {
        if(!CollectionUtils.isEmpty(cache)) {
            // 根据apikey类型获取对应的apikey列表
            List<AdminApiKeyEntity> adminApiKeyEntities = cache.get(apiTypes.typeNo);

            return CollectionUtils.isEmpty(adminApiKeyEntities) ? null : adminApiKeyEntities.get(0).getName();
        }
        return null;
    }

    /**
     * 判断当前的openai的apiKey是否有效
     *
     * @param adminApiKeyEntity
     * @return
     */
    private boolean filterInValidOpenAiApiKey(AdminApiKeyEntity adminApiKeyEntity){
        // 非openai类型，放行
        if(!ApiType.OPENAI.typeNo.equals(adminApiKeyEntity.getType())){
            return true;
        }

        // 判断该APIKey是否有余额，余额不足则删掉apiKey，不加载到缓存
        try {
            BillingUsage billingUsage = ChatGPTApi.getBillingUsage(adminApiKeyEntity.getName());

            // IO异常大概率是网络问题，暂时不删除apiKey，暂时放行
            if(billingUsage == null){
                return true;
            }

            // 余额不足
            if(billingUsage.getTotalAmount().compareTo(billingUsage.getTotalUsage()) <= 0){
                log.error("{}的额度使用完毕！", adminApiKeyEntity.getName());
                queueThreadPool.execute(() -> baseMapper.deleteById(adminApiKeyEntity.getId()));
                return false;
            }

            // 余额充足，更新信息
            adminApiKeyEntity.setTotalAmount(billingUsage.getTotalAmount());
            adminApiKeyEntity.setTotalUsage(billingUsage.getTotalUsage());
            adminApiKeyEntity.setExpiredTime(billingUsage.getExpiredTime());

            queueThreadPool.execute(() -> baseMapper.updateById(adminApiKeyEntity));
            return true;
        }
        // 捕获 请求openai错误的异常, 删掉这个apiKey，不加载到缓存
        catch (BaseException e){
            log.error("apiKey:{}, error:{}",adminApiKeyEntity.getName(), e.getMsg());
            if(e.getCode() != OpenAiRespError.OPENAI_LIMIT_ERROR.code){
                queueThreadPool.execute(() -> baseMapper.deleteById(adminApiKeyEntity.getId()));
            }

            return false;
        }
    }
}
