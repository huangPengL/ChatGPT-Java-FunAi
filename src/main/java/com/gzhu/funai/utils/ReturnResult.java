package com.gzhu.funai.utils;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 15:44
 */

@Data
public class ReturnResult {
    @ApiModelProperty("响应码")
    private Integer code;

    @ApiModelProperty("返回信息")
    private String message;

    @ApiModelProperty("返回数据")
    private Map<String, Object> data = new HashMap<String, Object>();

    //无参构造方法私有(使得外部只能通过调用ok() 和 error()来获取对象)
    private ReturnResult() { }


    //成功 静态方法
    public static ReturnResult ok(){
        ReturnResult r = new ReturnResult();
        r.setCode(ResultCode.SUCCESS.code);
        r.setMessage(ResultCode.SUCCESS.msg);
        return r;
    }
    //失败 静态方法
    public static ReturnResult error(){
        ReturnResult r = new ReturnResult();
        r.setCode(ResultCode.ERROR.code);
        r.setMessage(ResultCode.ERROR.msg);
        return r;
    }

    public ReturnResult codeAndMessage(ResultCode status){
        this.setCode(status.code);
        this.setMessage(status.msg);
        return this;
    }

    // 链式编程
    public ReturnResult code(Integer code){
        this.setCode(code);
        return this;
    }

    public ReturnResult message(String message){
        this.setMessage(message);
        return this;
    }

    public ReturnResult data(String key,Object value){
        this.data.put(key,value);
        return this;
    }

    public ReturnResult data(Map<String,Object> map){
        this.setData(map);
        return this;
    }

}
