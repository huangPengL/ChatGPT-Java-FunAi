package com.gzhu.funai.utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/26 18:28
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    ByteArrayOutputStream output;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener listener) {

            }

            @Override
            public void write(int b) throws IOException {
                output.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                output.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                output.write(b,off,len);
            }
        };
    }

    public byte[] getDataStream() {
        return output.toByteArray();
    }

}

