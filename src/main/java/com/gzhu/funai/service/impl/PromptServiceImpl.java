package com.gzhu.funai.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.gzhu.funai.dto.PromptQueryRequest;
import com.gzhu.funai.entity.PromptEntity;
import com.gzhu.funai.enums.PromptType;
import com.gzhu.funai.global.constant.TimeInterval;
import com.gzhu.funai.mapper.PromptMapper;
import com.gzhu.funai.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/11 16:38
 */

@Service
@Slf4j
public class PromptServiceImpl extends ServiceImpl<PromptMapper, PromptEntity> implements PromptService{

    private Map<String, PromptEntity> topicCache = ImmutableMap.of();
    private Map<Integer, List<PromptEntity>> typeCache = ImmutableMap.of();

    @Override
    public IPage<PromptEntity> list(int pageNum, int limit, PromptQueryRequest req) {

        Page<PromptEntity> page = new Page<>(pageNum, limit);

        QueryWrapper<PromptEntity> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(req.getContent())){
            wrapper.and(w -> w.like("content", req.getContent()).or().like("topic", req.getContent()));
        }
        if(req.getType() != null){
            wrapper.and(w -> w.eq("type", req.getType()));
        }
        if(req.getTarget() != null){
            wrapper.and(w -> w.eq("target", req.getTarget()));
        }
        wrapper.orderByDesc("update_time");

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public String getByTopic(String topic) {
        PromptEntity promptEntity = this.topicCache.get(topic);
        return promptEntity == null ? null : promptEntity.getContent();
    }

    @Override
    public List<PromptEntity> getByType(PromptType promptType) {
        return this.typeCache.get(promptType.typeNo);
    }

    @Scheduled(initialDelay = TimeInterval.ZERO, fixedRate = TimeInterval.ONE_HOUR)
    @Override
    public void load() {
        // 加载主键为topic的缓存
        this.topicCache = ImmutableMap.copyOf(
                baseMapper.selectList(null).stream()
                    // 按照topic先分组 -> Map<String, List<PromptEntity>>
                    .collect(Collectors.groupingBy(PromptEntity::getTopic))
                    // 对Map的value进行操作，取得第一个元素
                    .entrySet().stream().collect(
                            Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().findFirst().orElse(null))));

        // 加载主键为type的缓存
        this.typeCache = ImmutableMap.copyOf(
                baseMapper.selectList(null).stream()
                    .collect(Collectors.groupingBy(PromptEntity::getType)));

        log.info("加载Prompt库缓存成功！, topicCache size:{}, typeCache size: {}", this.topicCache.size(), this.typeCache.size());
    }
}
