package com.gzhu.funai.api.mengwang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gzhu.funai.utils.HttpClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

/**
 * @Author :wuxiaodong
 * @Date: 2023/2/24 17:02
 * @Description:短信发送工具类
 */
@Component
@Data
@Slf4j
public class SmsComponent {
    @Value("${sms.apikey}")
    private String apiKey;
    @Value("${sms.url}")
    private String url;

    public boolean send(String phone,String code) throws Exception {
        String content = "您的验证码是：%s,两分钟内有效！(FunAI智能应用平台)";
        content = URLEncoder.encode(String.format(content,code), "GBK");
        String body ="apikey="+apiKey+"&mobile="+phone+"&content="+content;
        String result = HttpClientUtil.post(url,body,"application/x-www-form-urlencoded","GBK", 10000, 10000);
        JSONObject jsonObject = JSON.parseObject(result);
        return (Integer)(jsonObject.get("result")) == 0;
    }
}
