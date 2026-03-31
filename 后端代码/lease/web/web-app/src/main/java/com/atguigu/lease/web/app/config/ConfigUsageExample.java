package com.atguigu.lease.web.app.config;

import com.atguigu.lease.web.app.config.common.CommonBusinessConfig;
import com.atguigu.lease.web.app.config.landlord.LandlordBusinessConfig;
import com.atguigu.lease.web.app.config.tenant.TenantBusinessConfig;
import org.springframework.stereotype.Component;

/**
 * 配置使用示例
 * 展示如何在代码中使用新的配置常量
 */
@Component
public class ConfigUsageExample {

    /**
     * 示例：在Controller中使用房东业务规则
     */
    public void landlordControllerExample() {
        // 检查房东公寓数量限制
        int maxApartments = LandlordBusinessConfig.BusinessRules.MAX_APARTMENTS_PER_LANDLORD;
        
        // 检查房东权限
        String createPermission = LandlordBusinessConfig.Permissions.APARTMENT_CREATE;
        
        // 检查房东状态
        int activeStatus = LandlordBusinessConfig.Status.ACTIVE;
    }

    /**
     * 示例：在Service中使用租客业务规则
     */
    public void tenantServiceExample() {
        // 检查租客申请数量限制
        int maxApplications = TenantBusinessConfig.BusinessRules.MAX_CONCURRENT_APPLICATIONS;
        
        // 检查租客信用等级
        int excellentCredit = TenantBusinessConfig.CreditLevel.EXCELLENT;
        
        // 检查租客权限
        String viewPermission = TenantBusinessConfig.Permissions.APARTMENT_VIEW;
    }

    /**
     * 示例：在通用Service中使用共用配置
     */
    public void commonServiceExample() {
        // 检查用户类型
        int landlordType = CommonBusinessConfig.UserType.LANDLORD;
        int tenantType = CommonBusinessConfig.UserType.TENANT;
        
        // 检查审核状态
        int pendingStatus = CommonBusinessConfig.ReviewStatus.PENDING;
        int approvedStatus = CommonBusinessConfig.ReviewStatus.APPROVED;
        
        // 使用系统配置
        int defaultPageSize = CommonBusinessConfig.SystemConfig.DEFAULT_PAGE_SIZE;
        long jwtExpiration = CommonBusinessConfig.SystemConfig.JWT_EXPIRATION_TIME;
    }

    /**
     * 示例：权限检查方法
     */
    public boolean checkLandlordPermission(String permission) {
        // 使用房东权限常量进行检查
        return LandlordBusinessConfig.Permissions.APARTMENT_CREATE.equals(permission) ||
               LandlordBusinessConfig.Permissions.ROOM_CREATE.equals(permission) ||
               LandlordBusinessConfig.Permissions.APPLICATION_PROCESS.equals(permission);
    }

    /**
     * 示例：业务规则验证
     */
    public boolean validateBusinessRules(int userType, int currentCount) {
        if (userType == CommonBusinessConfig.UserType.LANDLORD) {
            return currentCount < LandlordBusinessConfig.BusinessRules.MAX_APARTMENTS_PER_LANDLORD;
        } else if (userType == CommonBusinessConfig.UserType.TENANT) {
            return currentCount < TenantBusinessConfig.BusinessRules.MAX_CONCURRENT_APPLICATIONS;
        }
        return false;
    }
}