package com.gzhu.funai.redis;

import com.google.common.collect.ImmutableList;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/17 0:47
 */

@Component
public class ChatRedisHelper {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增用户当日的聊天次数count次
     * @param userId
     * @param count
     * @return
     */
    public int incrDailyChatCount(String userId, int count){
        String key = String.format(RedisKeys.USER_CHAT_DAILY_LIMIT, userId);
        Object o = redisTemplate.opsForValue().get(key);

        // 若当前key不存在，则新建一个key。计算当前时间到0点的时间差，作为key的过期时间
        if(StringUtils.isEmpty(o)){
            Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.now().with(LocalTime.MAX));
            redisTemplate.opsForValue().set(key, count, duration);
            return count;
        }
        // 若存在则新增
        return Integer.parseInt(String.valueOf(redisTemplate.opsForValue().increment(key, count)));
    }

    /**
     * 查看用户当日的聊天次数
     * @param userId
     * @return
     */
    public int getDailyChatCount(String userId){
        String key = String.format(RedisKeys.USER_CHAT_DAILY_LIMIT, userId);
        // 没有这个userId则返回0
        Object o = redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(o)){
            return 0;
        }
        return Integer.parseInt(String.valueOf(o));
    }

    /**
     * 新增用户上传次数count次
     * @param userId
     * @param count
     * @return
     */
    public int incrDailyFileUploadCount(String userId, int count){
        String key = String.format(RedisKeys.USER_FILE_UPLOAD_DAILY_LIMIT, userId);
        Object o = redisTemplate.opsForValue().get(key);

        // 若当前key不存在，则新建一个key。计算当前时间到0点的时间差，作为key的过期时间
        if(StringUtils.isEmpty(o)){
            Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.now().with(LocalTime.MAX));
            redisTemplate.opsForValue().set(key, count, duration);
            return count;
        }
        // 若存在则新增
        return Integer.parseInt(String.valueOf(redisTemplate.opsForValue().increment(key, count)));
    }

    /**
     * 查看用户当日的聊天次数
     * @param userId
     * @return
     */
    public int getDailyFileUploadCount(String userId){
        String key = String.format(RedisKeys.USER_FILE_UPLOAD_DAILY_LIMIT, userId);
        // 没有这个userId则返回0
        Object o = redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(o)){
            return 0;
        }
        return Integer.parseInt(String.valueOf(o));
    }

    /**
     * 清空限制
     * @param userId
     */
    public void truncateLimit(String userId){
        String dailyChatLimitKey = String.format(RedisKeys.USER_CHAT_DAILY_LIMIT, userId);
        String dailyFileUploadLimitKey = String.format(RedisKeys.USER_FILE_UPLOAD_DAILY_LIMIT, userId);

        redisTemplate.delete(ImmutableList.of(dailyChatLimitKey, dailyFileUploadLimitKey));
    }
}
