package com.atguigu.lease.web.app.service;

import java.util.List;

/**
 * 房东权限服务接口（移动端）
 */
public interface LandlordPermissionService {

    /**
     * 检查房东是否有指定权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 获取房东的所有权限
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 检查房东是否有任意一个权限
     */
    boolean hasAnyPermission(Long userId, String... permissionCodes);

    /**
     * 检查房东是否有所有权限
     */
    boolean hasAllPermissions(Long userId, String... permissionCodes);
}