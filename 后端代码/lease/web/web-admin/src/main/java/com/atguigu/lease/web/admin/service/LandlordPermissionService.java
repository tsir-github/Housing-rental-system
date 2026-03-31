package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.web.admin.vo.landlord.LandlordPermissionVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 后台管理房东权限服务接口
 */
public interface LandlordPermissionService {

    /**
     * 获取房东列表（分页）
     */
    IPage<LandlordPermissionVo> getLandlordList(Long current, Long size, String phone, String nickname);

    /**
     * 获取房东权限列表
     */
    List<String> getLandlordPermissions(Long landlordId);

    /**
     * 为房东分配权限
     */
    void assignPermissions(Long landlordId, List<String> permissions);

    /**
     * 撤销房东权限
     */
    void revokePermissions(Long landlordId, List<String> permissions);

    /**
     * 获取所有可分配的权限列表
     */
    List<String> getAvailablePermissions();

    /**
     * 重置房东权限（清空所有权限）
     */
    void resetLandlordPermissions(Long landlordId);

    /**
     * 禁用房东账户
     */
    void disableLandlord(Long landlordId, String reason);

    /**
     * 启用房东账户
     */
    void enableLandlord(Long landlordId);
}