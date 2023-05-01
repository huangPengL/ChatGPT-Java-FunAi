package com.gzhu.funai.interceptor;

import com.google.gson.Gson;
import com.gzhu.funai.entity.UserApiKeyEntity;
import com.gzhu.funai.entity.UserEntity;
import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.enums.UserLevel;
import com.gzhu.funai.redis.ChatRedisHelper;
import com.gzhu.funai.service.UserApiKeyService;
import com.gzhu.funai.service.UserService;
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

/**
 * @Author: huangpenglong
 * @Date: 2023/4/17 9:04
 */

@Slf4j
public class UserChatLimitInterceptor implements HandlerInterceptor {

    private ChatRedisHelper chatRedisHelper;
    private UserApiKeyService userApiKeyService;
    private UserService userService;
    public UserChatLimitInterceptor(ChatRedisHelper chatRedisHelper, UserApiKeyService userApiKeyService, UserService userService){
        this.chatRedisHelper = chatRedisHelper;
        this.userApiKeyService = userApiKeyService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");

        try {
            String userId = JwtUtil.getUserId(token);
            UserEntity userEntity = userService.getById(userId);
            UserLevel uLevel = UserLevel.get(userEntity.getLevel());
            UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(userId, ApiType.OPENAI);

            // 若用户没上传API-Key，则做限制
            if(userApiKeyEntity == null || StringUtils.isEmpty(userApiKeyEntity.getApikey())){
                int dailyChatCount = this.chatRedisHelper.getDailyChatCount(userId);
                if (dailyChatCount >= uLevel.dailyChatLimit) {
                    log.info("已限制用户id为{}的聊天功能，该用户聊天次数为{}次", userId, dailyChatCount);
                    String extraMsg = "";
                    if(UserLevel.VISITOR.equals(uLevel)){
                        extraMsg = "请注册账号，获取更多使用额度~";
                    }
                    else if(UserLevel.NORMAL.equals(uLevel)){
                        extraMsg = "请联系管理员升级账号，获取更多使用额度~";
                    }
                    out(ReturnResult.error()
                            .code(ResultCode.USER_CHAT_LIMITED.code)
                            .message(ResultCode.USER_CHAT_LIMITED.msg + "已使用" + dailyChatCount + "次。" + extraMsg), response);
                    return false;
                }
            }
        } catch (Exception e) {
            out(ReturnResult.error().code(ResultCode.USER_NOT_LOGIN.code).message(ResultCode.USER_NOT_LOGIN.msg), response);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String token = request.getHeader("token");
        String userId = JwtUtil.getUserId(token);

        // 用户没有上传APIKey，那就记录使用次数
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(userId, ApiType.OPENAI);
        if (userApiKeyEntity == null || StringUtils.isEmpty(userApiKeyEntity.getApikey())){
            int count = chatRedisHelper.incrDailyChatCount(userId, 1);
            log.info("用户id为{}的当日聊天次数为{}", userId, count);
        }
    }

    /**
     * 失败时返回数据
     */
    private void out(ReturnResult result, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(result));
    }
}
