package com.gzhu.funai.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @Author: huangpenglong
 * @Date: 2023/5/3 14:47
 */

@Component
public class AdminApiKeyRedisHelper {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 判断当前的openai免费key是否受限
     * @param apiKeyId
     * @return false表示不受限，true表示受限
     */
    public boolean judgeOpenAiFreeKeyLimit(int apiKeyId){
        String key = String.format(RedisKeys.ADMIN_OPENAI_FREE_KEY_LIMIT, apiKeyId);
        Object o = redisTemplate.opsForValue().get(key);

        // 若当前key不存在 或 次数小于3，那么说明当前key暂时不受限
        return !(StringUtils.isEmpty(o) || Integer.parseInt(String.valueOf(o)) < 3);
    }

    /**
     * 给当前的openai免费key记录受限信息
     * @param apiKeyId
     * @param count
     */
    public int incrOpenAiFreeKeyLimit(int apiKeyId, int count){
        String key = String.format(RedisKeys.ADMIN_OPENAI_FREE_KEY_LIMIT, apiKeyId);
        Object o = redisTemplate.opsForValue().get(key);

        // 若当前key不存在，则新建一个key。设置1分钟作为key的过期时间
        if(StringUtils.isEmpty(o)){
            Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusMinutes(1));
            redisTemplate.opsForValue().set(key, count, duration);
            return count;
        }
        // 若存在则新增
        return Integer.parseInt(String.valueOf(redisTemplate.opsForValue().increment(key, count)));
    }
}
