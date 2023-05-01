package com.gzhu.funai.interceptor;

import com.google.gson.Gson;

import com.gzhu.funai.utils.ReturnResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author :wuxiaodong
 * @Date: 2023/4/8 13:55
 * @Description:
 */
@Slf4j
public class AccessLimitInterceptor  implements HandlerInterceptor {
    private final RedisTemplate<String, Object> redisTemplate;

    public AccessLimitInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 多长时间内
     */
    private Long minute = 5L;

    /**
     * 访问次数
     */
    private Long times = 5L;

    /**
     * 禁用时长--单位/分钟
     */

    private Long lockTime = 10L;

    /**
     * 锁住时的key前缀
     */
    public static final String LOCK_PREFIX = "LOCK";

    /**
     * 统计次数时的key前缀
     */
    public static final String COUNT_PREFIX = "COUNT";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        // 这里忽略代理软件方式访问，默认直接访问，也就是获取得到的就是访问者真实ip地址
        String ip = request.getRemoteAddr();
        String lockKey = LOCK_PREFIX + ip + uri;
        Object isLock = redisTemplate.opsForValue().get(lockKey);
        if(Objects.isNull(isLock)){
            // 还未被禁用
            String countKey = COUNT_PREFIX + ip + uri;
            Object count = redisTemplate.opsForValue().get(countKey);
            if(Objects.isNull(count)){
                // 首次访问
                log.info("首次访问,uri{},ip{}",uri,ip);
                redisTemplate.opsForValue().set(countKey,1,minute, TimeUnit.MINUTES);
            }else{
                // 此用户前一点时间就访问过该接口
                if((Integer)count < times){
                    // 放行，访问次数 + 1
                    redisTemplate.opsForValue().increment(countKey);
                }else{
                    log.info("{}禁用访问{}",ip, uri);
                    // 禁用
                    redisTemplate.opsForValue().set(lockKey, 1,lockTime, TimeUnit.MINUTES);
                    // 删除统计
                    redisTemplate.delete(countKey);
                    out(ReturnResult.error().message("5分钟内超过接口访问次数限制"),response);
                }
            }
        }else{
//            // 此用户访问此接口已被禁用
            out(ReturnResult.error().message("5分钟内超过接口访问次数限制"),response);
            return false;
        }
        return true;
    }

    /**
     * api接口鉴权失败返回数据
     */
    private void out(ReturnResult result, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(result));
    }
}
