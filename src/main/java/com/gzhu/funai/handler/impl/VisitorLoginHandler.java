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
import com.gzhu.funai.utils.SequentialUuidHexGenerator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/30 21:13
 */

@Service(value = "VisitorLoginHandler")
public class VisitorLoginHandler implements LoginHandler {
    private static final String MAP_KEY_USERNAME = "username";

    @Resource
    private UserService userService;
    @Resource
    private UserLoginRecordService userLoginRecordService;
    @Resource
    private TaskExecutor queueThreadPool;

    @Override
    public Map<String, Object> login(LoginSession loginSession) {
        Map<String,Object> map = new HashMap<>(5);

        // 去数据库查询，以IP为username的游客账号是否存在
        UserEntity userEntity = this.userService.getOne(new QueryWrapper<UserEntity>()
                .eq("level", UserLevel.VISITOR.levelNo)
                .eq(MAP_KEY_USERNAME, loginSession.getIp()));

        // 无此账户，则创建一个
        if (userEntity == null){
            userEntity = UserEntity.builder()
                    .id(SequentialUuidHexGenerator.generate())
                    .username(loginSession.getIp())
                    .status((byte) 0)
                    .level(UserLevel.VISITOR.levelNo)
                    .build();
   
            userService.save(userEntity);
        }

        // 检验账户锁定情况
        if(userEntity.getStatus() == 1){
            return map;
        }

        //登录成功，返回数据
        map.put(MAP_KEY_USERNAME,userEntity.getUsername());
        map.put("userId", userEntity.getId());
        map.put("userLevel", String.valueOf(userEntity.getLevel()));
        map.put("token", JwtUtil.createToken(userEntity.getId(), (String)(map.get(MAP_KEY_USERNAME)), userEntity.getLevel()));

        // 登录入库
        UserEntity finalUserEntity = userEntity;
        queueThreadPool.execute(() ->
                userLoginRecordService.save(UserLoginRecord.builder()
                        .userId(finalUserEntity.getId())
                        .loginType(loginSession.getLoginType().typeNo)
                        .loginIp(loginSession.getIp())
                        .build())
        );
        return map;
    }
}
