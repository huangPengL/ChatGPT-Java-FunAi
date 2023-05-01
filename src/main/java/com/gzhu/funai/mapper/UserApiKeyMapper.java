package com.gzhu.funai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzhu.funai.entity.UserApiKeyEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: huangpenglong
 * @Date: 2023/4/20 19:46
 */
public interface UserApiKeyMapper extends BaseMapper<UserApiKeyEntity>{

    void insertOrUpdate(@Param("userId") String userId, @Param("type") Integer type, @Param("apikey") String apikey);
}
