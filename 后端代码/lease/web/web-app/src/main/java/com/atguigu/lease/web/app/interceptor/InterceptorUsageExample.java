package com.atguigu.lease.web.app.interceptor;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.web.app.config.common.CommonBusinessConfig;
import com.atguigu.lease.web.app.config.landlord.LandlordBusinessConfig;
import com.atguigu.lease.web.app.interceptor.landlord.RequireLandlordPermission;
import com.atguigu.lease.web.app.interceptor.tenant.RequireTenantPermission;
import org.springframework.web.bind.annotation.*;

/**
 * 拦截器使用示例
 * 展示如何在Controller中使用权限注解和获取用户信息
 */
@RestController
public class InterceptorUsageExample {

    /**
     * 示例1：房东权限注解使用
     */
    @PostMapping("/app/landlord/apartment")
    @RequireLandlordPermission(
        value = LandlordBusinessConfig.Permissions.APARTMENT_CREATE, 
        description = "创建公寓权限"
    )
    public Result createApartment() {
        // 获取当前登录的房东用户
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        
        // 验证用户类型（拦截器已经验证过，这里只是示例）
        if (!loginUser.getUserType().equals(CommonBusinessConfig.UserType.LANDLORD)) {
            return Result.fail("非房东用户");
        }
        
        // 房东创建公寓逻辑
        return Result.ok("公寓创建成功");
    }

    /**
     * 示例2：房东查看权限注解使用
     */
    @GetMapping("/app/landlord/rooms")
    @RequireLandlordPermission(
        value = LandlordBusinessConfig.Permissions.ROOM_VIEW, 
        description = "查看房源权限"
    )
    public Result getLandlordRooms() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        
        // 只能查看自己的房源
        Long landlordId = loginUser.getUserId();
        
        // 查询房东的房源列表逻辑
        return Result.ok("房源列表查询成功");
    }

    /**
     * 示例3：租客权限注解使用（预留）
     */
    @PostMapping("/app/tenant/application")
    @RequireTenantPermission(
        value = "application:create", 
        description = "创建申请权限"
    )
    public Result createApplication() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        
        // 验证用户类型
        if (!loginUser.getUserType().equals(CommonBusinessConfig.UserType.TENANT)) {
            return Result.fail("非租客用户");
        }
        
        // 租客创建申请逻辑
        return Result.ok("申请创建成功");
    }

    /**
     * 示例4：共用接口（只需要登录，不需要特定权限）
     */
    @GetMapping("/app/common/apartments")
    public Result getApartments() {
        // 这个接口只需要通过AuthenticationInterceptor验证
        // 不需要特定的房东或租客权限
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        
        if (loginUser.getUserType().equals(CommonBusinessConfig.UserType.LANDLORD)) {
            // 房东看到的公寓列表（包括自己发布的）
            return Result.ok("房东视角的公寓列表");
        } else if (loginUser.getUserType().equals(CommonBusinessConfig.UserType.TENANT)) {
            // 租客看到的公寓列表（只看已审核通过的）
            return Result.ok("租客视角的公寓列表");
        }
        
        return Result.ok("通用公寓列表");
    }

    /**
     * 示例5：在Service中获取用户信息
     */
    public void serviceExample() {
        // 在Service层也可以获取当前登录用户
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        
        if (loginUser != null) {
            Long userId = loginUser.getUserId();
            String username = loginUser.getUsername();
            Integer userType = loginUser.getUserType();
            
            // 根据用户类型执行不同的业务逻辑
            if (userType.equals(CommonBusinessConfig.UserType.LANDLORD)) {
                // 房东业务逻辑
                handleLandlordBusiness(userId);
            } else if (userType.equals(CommonBusinessConfig.UserType.TENANT)) {
                // 租客业务逻辑
                handleTenantBusiness(userId);
            }
        }
    }

    /**
     * 示例6：权限检查方法
     */
    public boolean checkUserPermission(String requiredPermission) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        
        if (loginUser == null) {
            return false;
        }
        
        // 根据用户类型检查权限
        if (loginUser.getUserType().equals(CommonBusinessConfig.UserType.LANDLORD)) {
            // 检查房东权限
            return checkLandlordPermission(loginUser.getUserId(), requiredPermission);
        } else if (loginUser.getUserType().equals(CommonBusinessConfig.UserType.TENANT)) {
            // 检查租客权限
            return checkTenantPermission(loginUser.getUserId(), requiredPermission);
        }
        
        return false;
    }

    private void handleLandlordBusiness(Long landlordId) {
        // 房东业务逻辑处理
    }

    private void handleTenantBusiness(Long tenantId) {
        // 租客业务逻辑处理
    }

    private boolean checkLandlordPermission(Long landlordId, String permission) {
        // 检查房东是否有特定权限
        // 可以查询landlord_permission表
        return true;
    }

    private boolean checkTenantPermission(Long tenantId, String permission) {
        // 检查租客是否有特定权限
        return true;
    }
}