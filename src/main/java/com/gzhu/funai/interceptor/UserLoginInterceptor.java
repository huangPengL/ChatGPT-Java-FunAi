package com.gzhu.funai.interceptor;

import com.gzhu.funai.utils.JwtUtil;
import com.gzhu.funai.utils.ResultCode;
import com.gzhu.funai.utils.ReturnResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.google.gson.Gson;

/**
 * @Author :wuxiaodong
 * @Date: 2023/3/24 14:38
 * @Description: 登录状态验证
 */
@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            out(ReturnResult.error().code(ResultCode.USER_NOT_LOGIN.code).message(ResultCode.USER_NOT_LOGIN.msg),response);
            return false;
        }

        String userId;
        String userName;
        boolean tokenExpired;

        try {
            // 验证过程只要token是非法的，会自动抛异常
            userId = JwtUtil.getUserId(token);
            userName = JwtUtil.getUserName(token);
            tokenExpired = JwtUtil.isTokenExpired(token);
            if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(userId) && !tokenExpired){
                return true;
            }
            out(ReturnResult.error().code(ResultCode.USER_NOT_LOGIN.code).message(ResultCode.USER_NOT_LOGIN.msg),response);
            return false;
        } catch (Exception e) {
            out(ReturnResult.error().code(ResultCode.USER_NOT_LOGIN.code).message(ResultCode.USER_NOT_LOGIN.msg), response);
            return false;
        }
    }

    /**
     * api接口鉴权失败返回数据
     */
    private void out(ReturnResult result,HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        // 在请求处理完成后执行该方法
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 在视图渲染完成后执行该方法
    }
}
