package com.gzhu.funai.api.baidu.util;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.baidu.aip.util.Util;
import com.gzhu.funai.api.baidu.constant.BaiDuConst;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/9 22:05
 * @Description:
 */
public class VoiceTool {

    // 初始化一个AipSpeech
    public static  final AipSpeech client = new AipSpeech(BaiDuConst.APP_ID, BaiDuConst.API_KEY, BaiDuConst.SECRET_KEY);
    /**
     * 语音识别
     * @return
     */
    public static String SpeechRecognition(byte[] data) throws JSONException {

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 调用接口
        JSONObject asrRes2 = client.asr(data, "wav", 16000, null);
        return asrRes2.toString(2);
    }

    /**
     * 语音合成
     * @param text 文字内容
     * @param path 合成语音生成路径,pcm格式
     * @return
     */
    public static void SpeechSynthesis(String text, String path) {
        /*
        最长的长度
         */
        int maxLength = 1024;
        if (text.getBytes().length >= maxLength) {
            return ;
        }
        // 初始化一个AipSpeech
        AipSpeech client = new AipSpeech(BaiDuConst.APP_ID, BaiDuConst.API_KEY, BaiDuConst.SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 调用接口
        TtsResponse res = client.synthesis(text, "zh", 1, null);
        byte[] data = res.getData();
        if (data != null) {
            try {
                Util.writeBytesToFileSystem(data, path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
