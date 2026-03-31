package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.web.app.mapper.LandlordPermissionMapper;
import com.atguigu.lease.web.app.service.LandlordPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 房东权限服务实现类（移动端）
 */
@Slf4j
@Service
public class LandlordPermissionServiceImpl implements LandlordPermissionService {

    @Autowired
    private LandlordPermissionMapper landlordPermissionMapper;

    @Override
    @Cacheable(value = "landlord_permission", key = "#userId + ':' + #permissionCode")
    public boolean hasPermission(Long userId, String permissionCode) {
        try {
            return landlordPermissionMapper.hasPermission(userId, permissionCode);
        } catch (Exception e) {
            log.error("检查房东权限失败，用户ID: {}, 权限代码: {}", userId, permissionCode, e);
            return false;
        }
    }

    @Override
    @Cacheable(value = "landlord_permissions", key = "#userId")
    public List<String> getUserPermissions(Long userId) {
        try {
            return landlordPermissionMapper.selectPermissionsByUserId(userId);
        } catch (Exception e) {
            log.error("获取房东权限列表失败，用户ID: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }

        return Arrays.stream(permissionCodes)
                .anyMatch(permission -> hasPermission(userId, permission));
    }

    @Override
    public boolean hasAllPermissions(Long userId, String... permissionCodes) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }

        return Arrays.stream(permissionCodes)
                .allMatch(permission -> hasPermission(userId, permission));
    }
}