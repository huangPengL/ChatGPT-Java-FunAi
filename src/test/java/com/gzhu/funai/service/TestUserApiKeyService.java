package com.gzhu.funai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gzhu.funai.entity.UserApiKeyEntity;
import com.gzhu.funai.enums.ApiType;
import org.junit.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import springfox.documentation.service.ApiKey;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/20 19:48
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUserApiKeyService  {
    @Autowired
    private UserApiKeyService userApiKeyService;


    @Test
    public void testSave(){

        UserApiKeyEntity userApiKeyEntity = UserApiKeyEntity.builder()
                .userId("")
                .apikey("AAABBB")
                .type(ApiType.OPENAI.typeNo)
                .build();
        userApiKeyService.insertOrUpdate(userApiKeyEntity);
    }

    @Test
    public void testGetOne(){
        UserApiKeyEntity one = userApiKeyService.getOne(new QueryWrapper<UserApiKeyEntity>()
                .eq("user_id", "")
                .eq("type", ApiType.OPENAI.typeNo)
        );

        System.out.println(one);
    }
}
