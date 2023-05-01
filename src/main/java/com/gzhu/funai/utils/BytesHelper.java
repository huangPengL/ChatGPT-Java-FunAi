package com.gzhu.funai.utils;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/2 23:05
 * @Description:
 */
public final class BytesHelper {

    private BytesHelper() {
    }

    public static int toInt(byte[] bytes) {
        int result = 0;
        for ( int i = 0; i < 4; i++ ) {
            result = ( result << 8 ) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }
}
