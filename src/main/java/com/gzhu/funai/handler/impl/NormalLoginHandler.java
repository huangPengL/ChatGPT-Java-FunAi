package com.gzhu.funai.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gzhu.funai.entity.UserEntity;
import com.gzhu.funai.entity.UserLoginRecord;
import com.gzhu.funai.enums.UserLevel;
import com.gzhu.funai.handler.LoginHandler;
import com.gzhu.funai.service.UserLoginRecordService;
import com.gzhu.funai.service.UserService;
import com.gzhu.funai.session.LoginSession;
import com.gzhu.funai.utils.JwtUtil;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/30 21:09
 */

@Service(value = "NormalLoginHandler")
public class NormalLoginHandler implements LoginHandler {
    private static final String MAP_KEY_USERNAME = "username";
    private static final String MAP_KEY_MOBILE = "mobile";

    @Resource
    private UserService userService;
    @Resource
    private UserLoginRecordService userLoginRecordService;
    @Resource
    private TaskExecutor queueThreadPool;

    @Override
    public Map<String, Object> login(LoginSession loginSession) {
        Map<String,Object> map = new HashMap<>(5);

        // 去数据库查询 SELECT * FROM user WHERE level != ? and (username = ? OR mobile = ?)
        UserEntity userEntity = this.userService.getOne(new QueryWrapper<UserEntity>()
                .ne("level", UserLevel.VISITOR.levelNo)
                .and(wrapper -> {
                    wrapper.eq(MAP_KEY_MOBILE, loginSession.getLoginAcct()).or()
                            .eq(MAP_KEY_USERNAME, loginSession.getLoginAcct());
                    return wrapper;
                }));

        // 无此账户，登录失败
        if (userEntity == null){
            return null;
        }

        // 检验账户锁定情况
        if(userEntity.getStatus() == 1){
            return map;
        }

        // 进行密码匹配失败
        if (!new BCryptPasswordEncoder().matches(loginSession.getPassword(), userEntity.getPassword())) {
            return null;
        }

        //登录成功，返回数据
        if (!StringUtils.isEmpty(userEntity.getNickname())) {
            map.put(MAP_KEY_USERNAME,userEntity.getNickname());
        }else if (!StringUtils.isEmpty(userEntity.getUsername())){
            map.put(MAP_KEY_USERNAME,userEntity.getUsername());
        } else {
            map.put(MAP_KEY_USERNAME,userEntity.getMobile());
        }
        map.put("userId", userEntity.getId());
        map.put("userLevel", String.valueOf(userEntity.getLevel()));
        map.put("token", JwtUtil.createToken(userEntity.getId(), (String)(map.get(MAP_KEY_USERNAME)), userEntity.getLevel()));

        // 登录入库
        queueThreadPool.execute(() ->
                userLoginRecordService.save(UserLoginRecord.builder()
                        .userId(userEntity.getId())
                        .loginType(loginSession.getLoginType().typeNo)
                        .loginIp(loginSession.getIp())
                        .build())
        );
        return map;
    }
}
