package com.gzhu.funai.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/26 18:11
 */

@Slf4j
public class RequestWrapper  extends HttpServletRequestWrapper {

    private byte[] requestBody=null;

    @SneakyThrows
    public RequestWrapper(HttpServletRequest request) {
        super(request);

        // 包含文件流，不打印body信息
        String body = "";
        if(request.getContentType() != null && !request.getContentType().startsWith("multipart/form-data")){
            body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        }

        String url = request.getRequestURI();
        log.info("ip{}, 访问路径{} 方法入参: {}",request.getRemoteAddr(), url, body);

        requestBody=body.getBytes(Charset.defaultCharset());

    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream inputStream=new ByteArrayInputStream(requestBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public byte[] getRequestBody(){
        return this.requestBody;
    }
}
