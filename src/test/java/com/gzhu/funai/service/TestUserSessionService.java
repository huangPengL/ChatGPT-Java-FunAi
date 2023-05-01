package com.gzhu.funai.service;

import com.gzhu.funai.entity.UserSessionEntity;
import com.gzhu.funai.enums.SessionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/17 15:36
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUserSessionService {
    @Autowired
    private UserSessionService userSessionService;

    @Test
    public void testInsert(){
        UserSessionEntity session = new UserSessionEntity("", "test_chattype", SessionType.NORMAL_CHAT.type);
        userSessionService.save(session);
    }
    @Test
    public void testRename(){
        UserSessionEntity session = new UserSessionEntity(97, "测试修改会话名称");
        userSessionService.updateById(session);
    }

    @Test
    public void testGetSessionList(){
        List<UserSessionEntity> sessionList = userSessionService.getSessionList("", SessionType.NORMAL_CHAT);
        sessionList.stream().forEach(e -> System.out.println(e.getSessionId()));
    }
}
