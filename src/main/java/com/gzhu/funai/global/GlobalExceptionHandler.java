package com.gzhu.funai.global;

import com.gzhu.funai.exception.*;
import com.gzhu.funai.utils.ResultCode;
import com.gzhu.funai.utils.ReturnResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/9 16:42
 */
@Slf4j              // logback记录日志
@ControllerAdvice   // 可对controller中被 @RequestMapping注解的方法加一些逻辑处理。最常用的就是异常处理
public class GlobalExceptionHandler {

    // 前端参数校验异常
    @ExceptionHandler(value= MethodArgumentNotValidException.class)
    @ResponseBody
    public ReturnResult handleRegisterLackException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}，异常类型：{}",e.getBindingResult().getAllErrors().get(0).getDefaultMessage(),e.getClass());
        return ReturnResult.error().code(ResultCode.BAD_PARAM.code).message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }


    // 数据库注册信息已存在异常
    @ExceptionHandler(value= {EmailException.class, PhoneException.class, UsernameException.class})
    @ResponseBody
    public ReturnResult handleRegisterRepeatException(Exception e){
        log.error("注册信息判重出现问题{}，异常类型：{}",e.getMessage(),e.getClass());
        return ReturnResult.error().code(ResultCode.USER_REGISTER_PARAMS_REPEAT.code).message(ResultCode.USER_REGISTER_PARAMS_REPEAT.msg+":"+e.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public ReturnResult exception(BaseException e) {
        log.error(e.getMessage());
        return ReturnResult.error().code(e.getCode()).message(e.getMsg());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ReturnResult exception(Exception e) {
        log.error(e.getMessage());
        return ReturnResult.error();
    }
}
