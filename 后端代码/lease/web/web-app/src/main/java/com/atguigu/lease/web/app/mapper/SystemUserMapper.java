package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.SystemUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户Mapper接口
 */
@Mapper
public interface SystemUserMapper extends BaseMapper<SystemUser> {
}