package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.LandlordPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房东权限Mapper（移动端）
 */
public interface LandlordPermissionMapper extends BaseMapper<LandlordPermission> {

    /**
     * 根据用户ID查询权限列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(@Param("userId") Long userId, @Param("permissionCode") String permissionCode);

}