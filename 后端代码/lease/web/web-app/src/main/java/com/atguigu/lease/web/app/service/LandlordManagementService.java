package com.atguigu.lease.web.app.service;

import com.atguigu.lease.common.login.LoginUser;

/**
 * 房东管理服务接口
 */
public interface LandlordManagementService {

    /**
     * 验证房东是否有权限访问指定功能
     * @param landlordId 房东ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long landlordId, String permission);

    /**
     * 验证房东是否有权限操作指定资源
     * @param landlordId 房东ID
     * @param resourceType 资源类型（apartment, room, facility等）
     * @param resourceId 资源ID
     * @return 是否有权限
     */
    boolean hasResourcePermission(Long landlordId, String resourceType, Long resourceId);

    /**
     * 获取房东的权限列表
     * @param landlordId 房东ID
     * @return 权限列表
     */
    java.util.List<String> getLandlordPermissions(Long landlordId);

    /**
     * 验证当前登录用户是否为房东
     * @param loginUser 登录用户信息
     * @return 是否为房东
     */
    boolean isLandlord(LoginUser loginUser);

    /**
     * 获取房东统计信息
     * @param landlordId 房东ID
     * @return 统计信息
     */
    java.util.Map<String, Object> getLandlordStatistics(Long landlordId);
}