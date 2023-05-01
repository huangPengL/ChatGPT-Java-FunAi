package com.gzhu.funai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gzhu.funai.entity.AdminApiKeyEntity;
import com.gzhu.funai.enums.ApiType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/10 23:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestAdminApiKeyService {

    @Resource
    private AdminApiKeyService adminApiKeyService;

    @Resource
    private TaskExecutor queueThreadPool;

    @Test
    public void testGetBestByType(){

        for(int i=0;i<10;i++) {
            String s = adminApiKeyService.getBestByType(ApiType.OPENAI);
            System.out.println(s);
        }
    }

    @Test
    public void testRoundRobinGetByType(){

        for(int i=0;i<10;i++) {
            String s = adminApiKeyService.roundRobinGetByType(ApiType.OPENAI);
            System.out.println(s);
        }
    }

    @Test
    public void testList(){
        List<AdminApiKeyEntity> list = adminApiKeyService.list(null);
        for(AdminApiKeyEntity item: list){
            System.out.println(item);
        }
    }

    @Test
    public void testAdd(){
        AdminApiKeyEntity build = AdminApiKeyEntity.builder().name("123").build();
        adminApiKeyService.save(build);
    }

    @Test
    public void testDelete(){
        adminApiKeyService.remove(new QueryWrapper<AdminApiKeyEntity>().eq("name", "123"));
    }

    @Test
    public void load(){
        adminApiKeyService.load();

        String bestByType = adminApiKeyService.getBestByType(ApiType.OPENAI);
        System.out.println(bestByType);
    }
}
