package com.gzhu.funai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzhu.funai.api.mengwang.SmsComponent;
import com.gzhu.funai.dto.*;
import com.gzhu.funai.entity.UserAdvicesEntity;
import com.gzhu.funai.entity.UserApiKeyEntity;
import com.gzhu.funai.entity.UserEntity;
import com.gzhu.funai.enums.ApiType;
import com.gzhu.funai.enums.LoginType;
import com.gzhu.funai.enums.SessionType;
import com.gzhu.funai.enums.UserLevel;
import com.gzhu.funai.redis.RedisKeys;
import com.gzhu.funai.service.*;
import com.gzhu.funai.session.LoginSession;
import com.gzhu.funai.utils.ResultCode;
import com.gzhu.funai.utils.ReturnResult;
import com.gzhu.funai.utils.VerificationCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author :wuxiaodong
 * @Date: 2023/3/16 14:58
 * @Description:
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SmsComponent smsComponent;

    @Resource
    private ChatService chatService;

    @Resource
    private UserSessionService userSessionService;

    @Resource
    private UserApiKeyService userApiKeyService;

    @Resource
    private UserAdvicesService userAdvicesService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/sendCode")
    public ReturnResult sendCode(@Valid @RequestBody SendCodeRequest request) throws Exception {
        // 接口防刷，验证码有效缓存时间内，不允许再向手机发送验证码
        if (!StringUtils.isEmpty(stringRedisTemplate.opsForValue().get(
                String.format(RedisKeys.USER_REGISTER_CODE, request.getPhone())))) {
            return ReturnResult.error().message("2分钟内禁止重复发送验证码");
        }

        // 随机产生4位数验证码
        String code = VerificationCodeGenerator.generateCode(4);
        stringRedisTemplate.opsForValue().set(
                String.format(RedisKeys.USER_REGISTER_CODE ,request.getPhone()) ,code,2, TimeUnit.MINUTES);

        // 发送验证码
        return smsComponent.send(request.getPhone(),code) ? ReturnResult.ok() : ReturnResult.error().message("发送验证码错误");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ReturnResult register(@Valid @RequestBody UserRegisterRequest req, HttpServletRequest request) {
        // 先校验验证码
        String redisCode = stringRedisTemplate.opsForValue().get(
                String.format(RedisKeys.USER_REGISTER_CODE, req.getPhone()));

        if (StringUtils.isEmpty(redisCode)) {
            return ReturnResult.error().message("验证码过期，请重新发送验证码");
        }

        if (!req.getCode().equals(redisCode)) {
            return ReturnResult.error().message("验证码错误");
        }

        // 删除验证码
        stringRedisTemplate.delete(String.format(RedisKeys.USER_REGISTER_CODE ,req.getPhone()));

        //验证码通过，真正注册
        UserEntity register = userService.register(req);

        // 给新用户创建第一个聊天会话
        userSessionService.save(register.getId(), register.getUsername() + "的聊天室", SessionType.NORMAL_CHAT);

        // 给新用户直接登录
        log.info("号码{}注册成功，已自动登录！", req.getPhone());
        return ReturnResult.ok().data(userService.login(
                LoginSession.builder()
                    .loginAcct(req.getUserName())
                    .password(req.getPassword())
                    .loginType(LoginType.NORMAL)
                    .ip(request.getRemoteAddr())
                    .build()
        ));
    }


    /**
    * 登录（账号登录 / 游客登录）
    * */
    @PostMapping("/login")
    public ReturnResult login(@RequestBody @Valid UserLoginRequest req, HttpServletRequest request) {

        Map<String, Object> map = userService.login(
                LoginSession.builder()
                        .loginAcct(req.getLoginAcct())
                        .password(req.getPassword())
                        .loginType(LoginType.get(req.getLoginType()))
                        .ip(request.getRemoteAddr())
                        .build());

        if (map == null) {
            return ReturnResult.error().message("用户名或密码错误");
        }
        else if (map.isEmpty()){
            return ReturnResult.error().message("账号已被锁定，请与管理员联系！！！");
        }
        //  登录成功，map包含用户名和token.要求前端将token串保留在cookie中，前端请求拦截器把cookie的token放在请求头的字段中
        return ReturnResult.ok().data(map);
    }

    /**
    * 退出登录。
    * 前端：清除浏览器中关于本站的所有cookie,返回登录页
    * */
    @GetMapping("/logout/{userId}")
    public ReturnResult logout(@PathVariable("userId") String userId) {
        // 清除用户缓存
        chatService.clearUserCache(userId);
        return ReturnResult.ok();
    }

    /**
    * 重置密码
    * */
    @PostMapping("/resetPwd")
    public ReturnResult resetPwd(@RequestBody @Valid UserResetPasswordRequest request) {
        boolean flag = userService.resetPwd(request);
        return flag ? ReturnResult.ok():ReturnResult.error().codeAndMessage(ResultCode.USER_NOT_EXIST);
    }

    /**
     * 根据userid和typeNo获得第三方的APIKey
     */
    @PostMapping("/apiKey/get")
    public ReturnResult getApiKey(@RequestBody @Valid UserApiKeyRequest req){
        UserApiKeyEntity userApiKeyEntity = userApiKeyService.getByUserIdAndType(
                req.getUserId(), ApiType.get(req.getApiTypeNo()));
        return ReturnResult.ok().data("api_key", userApiKeyEntity == null ? "" : userApiKeyEntity.getApikey());
    }

    /**
     * 根据唯一键 userId 和 type来决定插入还是更新数据
     */
    @PostMapping("/apiKey/insertOrUpdate")
    public ReturnResult insertOrUpdateApiKey(@RequestBody @Valid UserApiKeyRequest req){
        UserApiKeyEntity userApiKeyEntity = UserApiKeyEntity.builder()
                .apikey(req.getApiKey())
                .type(req.getApiTypeNo())
                .userId(req.getUserId())
                .build();
        userApiKeyService.insertOrUpdate(userApiKeyEntity);
        return ReturnResult.ok();
    }

    /**
     * 条件查询用户列表
     */
    @PostMapping("/admin/getUserListByCondition/{limit}/{page}")
    public ReturnResult getUserListByCondition(@RequestBody UserListRequest userListRequset, @PathVariable("limit") Long limit,@PathVariable("page") Long page) {
        IPage<UserEntity> iPage = userService.getUserListByCondition(userListRequset, limit, page);
        return ReturnResult.ok().data("records",iPage.getRecords()).data("total",iPage.getTotal());
    }

    /**
     * 用户锁定/解锁
     */
    @PostMapping("/admin/lockUser")
    public ReturnResult lockUser(@RequestBody UserLockRequest userLockRequest) {
        boolean flag = userService.lock(userLockRequest.getUserId(), userLockRequest.getStatus());
        return flag ? ReturnResult.ok() : ReturnResult.error();
    }

    /**
     * 修改用户等级
     */
    @PostMapping("/admin/changeLevel")
    public ReturnResult changeLevel(@RequestBody ChangeUserLevelRequest request) {
        // 不能修改游客的等级
        if(UserLevel.VISITOR.levelNo.equals(request.getOriginalLevel())){
            return ReturnResult.error().message("不能修改游客的等级!");
        }
        boolean flag = userService.changeLevel(request.getUserId(), request.getLevel());
        return flag ?  ReturnResult.ok() : ReturnResult.error();
    }

    /**
     * 添加用户建议
     * @Author :oujiajun
     * @Date: 2023/4/29 14:58
     */
    @PostMapping("/advice/addAdvice")
    public ReturnResult addAdvice(@RequestBody @Valid UserAdviceRequest userAdviceRequest) {
        if (StringUtils.isEmpty(userAdviceRequest.getUserAdvice())) {
            return ReturnResult.error().message("意见内容不能为空，请填写后再次提交");
        }
        UserAdvicesEntity userAdviceEntity = UserAdvicesEntity.builder()
                .userId(userAdviceRequest.getUserId())
                .username(userAdviceRequest.getUsername())
                .advice(userAdviceRequest.getUserAdvice())
                .build();
        userAdvicesService.save(userAdviceEntity);
        return ReturnResult.ok();
    }
}
