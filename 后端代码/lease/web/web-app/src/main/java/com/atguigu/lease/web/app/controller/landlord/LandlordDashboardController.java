package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.app.service.LandlordManagementService;
import com.atguigu.lease.web.app.vo.landlord.LandlordDashboardVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 房东仪表板控制器
 * 提供房东的仪表板数据、统计信息查看和权限状态查询功能
 * 注意：权限管理由后台管理员负责，房东只能查看不能修改
 */
@Tag(name = "房东仪表板")
@RestController
@RequestMapping("/app/landlord/dashboard")
@Slf4j
public class LandlordDashboardController {

    @Autowired
    private LandlordManagementService landlordManagementService;

    @Operation(summary = "获取房东仪表板数据")
    @GetMapping("/data")
    public Result<LandlordDashboardVo> getDashboardData() {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            // 验证房东身份
            if (!landlordManagementService.isLandlord(LoginUserHolder.getLoginUser())) {
                log.warn("非房东用户尝试访问仪表板: userId={}", landlordId);
                return Result.fail("无权访问房东功能");
            }
            
            // 获取统计信息
            Map<String, Object> statistics = landlordManagementService.getLandlordStatistics(landlordId);
            
            // 构建仪表板数据
            LandlordDashboardVo dashboardVo = new LandlordDashboardVo();
            dashboardVo.setLandlordId(landlordId);
            dashboardVo.setApartmentCount((Long) statistics.get("apartmentCount"));
            dashboardVo.setRoomCount((Long) statistics.get("roomCount"));
            dashboardVo.setPublishedRoomCount((Long) statistics.get("publishedRoomCount"));
            dashboardVo.setAvailableRoomCount((Long) statistics.get("availableRoomCount"));
            dashboardVo.setOccupiedRoomCount((Long) statistics.get("occupiedRoomCount"));
            dashboardVo.setPendingApplicationCount((Long) statistics.get("pendingApplicationCount"));
            dashboardVo.setPendingAppointmentCount((Long) statistics.get("pendingAppointmentCount"));
            dashboardVo.setTodayAppointmentCount((Long) statistics.get("todayAppointmentCount"));
            dashboardVo.setOccupancyRate((Double) statistics.get("occupancyRate"));
            dashboardVo.setMonthlyNewRooms((Long) statistics.get("monthlyNewRooms"));
            dashboardVo.setMonthlyDeals((Long) statistics.get("monthlyDeals"));
            dashboardVo.setMonthlyIncome((Double) statistics.get("monthlyIncome"));
            dashboardVo.setLastUpdateTime(LocalDateTime.now());
            
            // 设置默认的房东等级和信用评分
            dashboardVo.setLandlordLevel("普通房东");
            dashboardVo.setCreditScore(85);
            
            log.info("获取房东仪表板数据成功: landlordId={}", landlordId);
            
            return Result.ok(dashboardVo);
        } catch (Exception e) {
            log.error("获取房东仪表板数据失败", e);
            return Result.fail("获取仪表板数据失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东经营统计", description = "房东查看自己的经营数据统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            // 验证房东身份
            if (!landlordManagementService.isLandlord(LoginUserHolder.getLoginUser())) {
                log.warn("非房东用户尝试访问统计信息: userId={}", landlordId);
                return Result.fail("无权访问房东功能");
            }
            
            Map<String, Object> statistics = landlordManagementService.getLandlordStatistics(landlordId);
            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("获取房东统计信息失败", e);
            return Result.fail("获取统计信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东快速操作菜单")
    @GetMapping("/quick-actions")
    public Result<Map<String, Object>> getQuickActions() {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            Map<String, Object> quickActions = Map.of(
                "actions", java.util.List.of(
                    Map.of("key", "add-apartment", "title", "添加公寓", "icon", "apartment", "path", "/landlord/apartment/add"),
                    Map.of("key", "add-room", "title", "发布房源", "icon", "room", "path", "/landlord/room/add"),
                    Map.of("key", "manage-applications", "title", "处理申请", "icon", "application", "path", "/landlord/applications"),
                    Map.of("key", "manage-appointments", "title", "预约管理", "icon", "appointment", "path", "/landlord/appointments"),
                    Map.of("key", "view-statistics", "title", "数据统计", "icon", "statistics", "path", "/landlord/statistics")
                )
            );
            
            return Result.ok(quickActions);
        } catch (Exception e) {
            log.error("获取房东快速操作菜单失败", e);
            return Result.fail("获取快速操作菜单失败：" + e.getMessage());
        }
    }

    @Operation(summary = "查看房东权限状态", description = "房东查看自己被分配的权限列表（只读）")
    @GetMapping("/permissions")
    public Result<List<String>> getPermissions() {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            List<String> permissions = landlordManagementService.getLandlordPermissions(landlordId);
            return Result.ok(permissions);
        } catch (Exception e) {
            log.error("获取房东权限列表失败", e);
            return Result.fail("获取权限列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "检查功能权限", description = "前端用于检查是否显示某个功能按钮")
    @GetMapping("/check-permission")
    public Result<Boolean> checkPermission(@RequestParam String permission) {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            boolean hasPermission = landlordManagementService.hasPermission(landlordId, permission);
            return Result.ok(hasPermission);
        } catch (Exception e) {
            log.error("验证权限失败", e);
            return Result.fail("验证权限失败：" + e.getMessage());
        }
    }

    @Operation(summary = "检查资源操作权限", description = "验证是否有权限操作特定资源")
    @GetMapping("/check-resource-permission")
    public Result<Boolean> checkResourcePermission(@RequestParam String resourceType, 
                                                  @RequestParam Long resourceId) {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            boolean hasPermission = landlordManagementService.hasResourcePermission(
                landlordId, resourceType, resourceId);
            return Result.ok(hasPermission);
        } catch (Exception e) {
            log.error("验证资源权限失败", e);
            return Result.fail("验证资源权限失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东仪表板概览", description = "房东首页显示的综合信息概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            // 获取统计信息
            Map<String, Object> statistics = landlordManagementService.getLandlordStatistics(landlordId);
            
            // 获取权限信息（只读）
            List<String> permissions = landlordManagementService.getLandlordPermissions(landlordId);
            
            Map<String, Object> overview = Map.of(
                "statistics", statistics,
                "permissions", permissions,
                "landlordId", landlordId
            );
            
            return Result.ok(overview);
        } catch (Exception e) {
            log.error("获取房东信息概览失败", e);
            return Result.fail("获取信息概览失败：" + e.getMessage());
        }
    }
}