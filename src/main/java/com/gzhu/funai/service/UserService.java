package com.gzhu.funai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzhu.funai.dto.UserListRequest;
import com.gzhu.funai.dto.UserRegisterRequest;
import com.gzhu.funai.dto.UserResetPasswordRequest;
import com.gzhu.funai.entity.UserEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gzhu.funai.enums.LoginType;
import com.gzhu.funai.session.LoginSession;

import java.util.List;
import java.util.Map;

/**
 *@Author :wuxiaodong
 *@Date: 2023/3/16 16:48
 *@Description:
 */
public interface UserService extends IService<UserEntity>{
    /**
     * 注册
     * @param userRegisterVo
     * @return
     */
    UserEntity register(UserRegisterRequest userRegisterVo);

    /**
     * 登录
     * @param loginSession
     * @return
     */
    Map<String,Object> login(LoginSession loginSession);

    /**
     * 重新设置密码
     * @param request
     * @return
     */
    boolean resetPwd(UserResetPasswordRequest request);

    IPage<UserEntity> getUserListByCondition(UserListRequest userListRequset, Long limit , Long page);

    boolean lock(String userId, Byte status);

    /**
     * 修改用户等级
     * 修改后用户当日的聊天 和 文件上传次数限制清空
     * @param userId
     * @param level
     * @return
     */
    boolean changeLevel(String userId, int level);
}
