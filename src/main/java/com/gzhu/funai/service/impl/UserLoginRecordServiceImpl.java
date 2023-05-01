package com.gzhu.funai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzhu.funai.entity.UserLoginRecord;
import com.gzhu.funai.mapper.UserLoginRecordMapper;
import com.gzhu.funai.service.UserLoginRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/26 17:10
 */

@Service
@Slf4j
public class UserLoginRecordServiceImpl
        extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord>
        implements UserLoginRecordService {


}
