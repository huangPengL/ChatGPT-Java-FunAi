package com.gzhu.funai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzhu.funai.entity.UserAdvicesEntity;
import com.gzhu.funai.mapper.UserAdvicesMapper;
import com.gzhu.funai.service.UserAdvicesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserAdvicesServiceImpl extends ServiceImpl<UserAdvicesMapper, UserAdvicesEntity>
        implements UserAdvicesService {
}
