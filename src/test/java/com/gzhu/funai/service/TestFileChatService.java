package com.gzhu.funai.service;

import com.gzhu.funai.api.openai.constant.OpenAIConst;
import com.gzhu.funai.api.openai.req.ChatGPTReq;
import com.gzhu.funai.api.openai.resp.ChatGPTResp;
import com.gzhu.funai.enums.ApiType;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author zxw
 * @Desriiption:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestFileChatService {

    @Autowired
    private FileChatService fileChatService;
    @Resource
    private AdminApiKeyService adminApiKeyService;
    @Resource
    private PromptService promptService;

    @Test
    public void uploadFile(){
        adminApiKeyService.load();
        promptService.load();
        //生成File文件
        File file = new File("D:\\！！笔记\\000学习\\6-专业前沿学习\\003-人工智能\\09-NLP相关论文\\14-chatgpt\\ChatGPT-SpringBoot\\src\\test\\java\\com\\gzhu\\funai\\service\\ICTAI2022_9_28___终稿.pdf");

        //File文件转MultipartFile
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input));

            ChatGPTReq chatGPTReq = ChatGPTReq.builder().model(OpenAIConst.MODEL_NAME_CHATGPT_3_5).build();
            String resultR = fileChatService.uploadFile(multipartFile, "", adminApiKeyService.roundRobinGetByType(ApiType.OPENAI), chatGPTReq, false);

            System.out.println("文件处理成功");

            ChatGPTResp resp = fileChatService.chatWithFile("", 85, "文章的创新点是什么？", adminApiKeyService.roundRobinGetByType(ApiType.OPENAI), chatGPTReq, false);

            if(resp != null){
                System.out.println("结果0：" + resp.getMessage());
            }

            ChatGPTResp resp1 = fileChatService.chatWithFile("", 85, "发表这篇文章的机构是谁？", adminApiKeyService.roundRobinGetByType(ApiType.OPENAI), chatGPTReq, false);
            if(resp1 != null){
                System.out.println("结果1：" + resp1.getMessage());
            }

            boolean result = fileChatService.dropCollection("", 85, false);
            if(result){
                System.out.println("关闭连接成功");
            }else{
                System.out.println("关闭连接失败");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
