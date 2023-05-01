package com.gzhu.funai.config;

import com.gzhu.funai.interceptor.*;
import com.gzhu.funai.redis.ChatRedisHelper;
import com.gzhu.funai.service.UserApiKeyService;
import com.gzhu.funai.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author :wuxiaodong
 * @Date: 2023/3/24 14:40
 * @Description: springmvc配置，添加拦截器
 */
@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ChatRedisHelper chatRedisHelper;
    @Resource
    private UserApiKeyService userApiKeyService;
    @Resource
    private UserService userService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // token携带认证。拦截所有请求 除了 登录、注册、发送验证码、重置密码的请求
        registry.addInterceptor(new UserLoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register","/user/sendCode", "/user/resetPwd");

        // 接口防刷
        registry.addInterceptor(new AccessLimitInterceptor(this.redisTemplate))
                .addPathPatterns("/user/sendCode");

        // 聊天功能次数限制
        registry.addInterceptor(new UserChatLimitInterceptor(this.chatRedisHelper, this.userApiKeyService, this.userService))
                .addPathPatterns(
                        "/translation/translate", "/chat/game/startGameSession",
                        "/chat/session", "/chat/oneShot",
                        "/chat/streamSessionChat", "/chat/streamOneShotChat",
                        "/file/chatWithFile", "/file/streamChatWithFile");

        // 文件上传功能次数限制
        registry.addInterceptor(new UserFileUploadLimitInterceptor(this.chatRedisHelper, this.userApiKeyService, this.userService))
                .addPathPatterns(
                        "/file/chatPdfUpload"
                        );

        // 管理员权限接口禁用
        registry.addInterceptor(new AdminOperateInterceptor())
                .addPathPatterns(
                        "/**/admin/**"
                );
    }
}
