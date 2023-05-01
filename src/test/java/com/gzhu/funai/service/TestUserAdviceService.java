package com.gzhu.funai.service;

import com.gzhu.funai.entity.UserAdvicesEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUserAdviceService {
    @Resource
    private UserAdvicesService userAdvicesService;

    @Test
    public void testUserAddAdvice(){
        UserAdvicesEntity userAdvicesEntity = UserAdvicesEntity.builder()
                .userId("8782ff00_0187_82ff0000_2c9e94c4_0000")
                .advice("这里的界面不太好")
                .build();
        userAdvicesService.save(userAdvicesEntity);
    }
}
