package com.gzhu.funai.service;

import com.gzhu.funai.entity.PromptEntity;
import com.gzhu.funai.enums.PromptType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/11 16:39
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPromptService {
    @Resource
    private PromptService promptService;

    @Test
    public void testInsert(){
        PromptEntity promptEntity = PromptEntity.builder()
                .content("你是一个翻译专家, 你需要帮我把以下句子翻译成英文，无需输出多余内容，把翻译内容给我。内容如下：\\%s")
                .type(PromptType.CHATGPT.typeNo)
                .description("多语言 -> 英语")
                .topic("翻译")
                .build();

        promptService.save(promptEntity);
    }

    @Test
    public void testList(){
        promptService.list(null).forEach(System.out::println);
    }
}
