package com.atguigu.lease.web.admin.mapper;

import com.atguigu.lease.model.entity.LandlordPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 房东权限Mapper接口
 */
@Mapper
public interface LandlordPermissionMapper extends BaseMapper<LandlordPermission> {

    /**
     * 根据用户ID查询权限代码列表
     */
    @Select("SELECT permission_code FROM landlord_permission WHERE user_id = #{userId} AND is_deleted = 0")
    List<String> selectPermissionsByUserId(Long userId);

    /**
     * 查询所有有房东权限的用户ID（去重）
     */
    @Select("SELECT DISTINCT user_id FROM landlord_permission WHERE is_deleted = 0")
    List<Long> selectDistinctUserIds();
}