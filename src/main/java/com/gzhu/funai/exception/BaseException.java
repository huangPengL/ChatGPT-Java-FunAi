package com.gzhu.funai.exception;

import com.gzhu.funai.utils.ReturnResult;
import lombok.Data;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/28 0:39
 */

@Data
public class BaseException extends RuntimeException {

    private final String msg;
    private final int code;

    public BaseException(String msg) {
        super(msg);
        this.code = ReturnResult.error().getCode();
        this.msg = msg;
    }

    public BaseException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BaseException() {
        super(ReturnResult.error().getMessage());
        this.code = ReturnResult.error().getCode();
        this.msg = ReturnResult.error().getMessage();
    }
}
