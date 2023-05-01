package com.gzhu.funai.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/17 0:54
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestChatRedisHelper {

    @Resource
    private ChatRedisHelper chatRedisHelper;

    @Test
    public void testChatCount(){
        String userId = "";
        chatRedisHelper.incrDailyChatCount(userId, 1);
        int dailyChatLimit = chatRedisHelper.getDailyChatCount(userId);
        System.out.println(dailyChatLimit);
    }

    @Test
    public void testPDFUploadCount(){
        String userId = "";
        chatRedisHelper.incrDailyFileUploadCount(userId, 1);
        int dailyChatLimit = chatRedisHelper.getDailyFileUploadCount(userId);
        System.out.println(dailyChatLimit);
    }
}
