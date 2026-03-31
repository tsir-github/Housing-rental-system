package com.atguigu.lease.web.admin.controller.landlord;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.admin.service.LandlordPermissionService;
import com.atguigu.lease.web.admin.vo.landlord.LandlordPermissionVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 房东权限管理控制器
 * 后台管理员用于管理房东权限的接口
 */
@Tag(name = "房东权限管理")
@RestController
@RequestMapping("/admin/landlord")
@Slf4j
public class LandlordPermissionController {

    @Autowired
    private LandlordPermissionService landlordPermissionService;

    @Operation(summary = "获取房东列表")
    @GetMapping("/list")
    public Result<IPage<LandlordPermissionVo>> getLandlordList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String nickname) {
        
        log.info("获取房东列表: current={}, size={}, phone={}, nickname={}", current, size, phone, nickname);
        
        IPage<LandlordPermissionVo> result = landlordPermissionService.getLandlordList(current, size, phone, nickname);
        return Result.ok(result);
    }

    @Operation(summary = "查看房东权限详情")
    @GetMapping("/{landlordId}/permissions")
    public Result<List<String>> getLandlordPermissions(@PathVariable Long landlordId) {
        log.info("查看房东权限: landlordId={}", landlordId);
        
        List<String> permissions = landlordPermissionService.getLandlordPermissions(landlordId);
        return Result.ok(permissions);
    }

    @Operation(summary = "获取可分配权限列表")
    @GetMapping("/available-permissions")
    public Result<List<String>> getAvailablePermissions() {
        log.info("获取可分配权限列表");
        
        List<String> permissions = landlordPermissionService.getAvailablePermissions();
        return Result.ok(permissions);
    }

    @Operation(summary = "为房东分配权限")
    @PostMapping("/assign-permissions")
    public Result<Void> assignPermissions(@RequestBody Map<String, Object> request) {
        Long landlordId = Long.valueOf(request.get("landlordId").toString());
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) request.get("permissions");
        String reason = (String) request.get("reason");
        
        log.info("分配房东权限: landlordId={}, permissions={}, reason={}", landlordId, permissions, reason);
        
        landlordPermissionService.assignPermissions(landlordId, permissions);
        return Result.ok();
    }

    @Operation(summary = "撤销房东权限")
    @DeleteMapping("/revoke-permissions")
    public Result<Void> revokePermissions(@RequestBody Map<String, Object> request) {
        Long landlordId = Long.valueOf(request.get("landlordId").toString());
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) request.get("permissions");
        String reason = (String) request.get("reason");
        
        log.info("撤销房东权限: landlordId={}, permissions={}, reason={}", landlordId, permissions, reason);
        
        landlordPermissionService.revokePermissions(landlordId, permissions);
        return Result.ok();
    }

    @Operation(summary = "重置房东权限")
    @PostMapping("/{landlordId}/reset-permissions")
    public Result<Void> resetLandlordPermissions(@PathVariable Long landlordId) {
        log.info("重置房东权限: landlordId={}", landlordId);
        
        landlordPermissionService.resetLandlordPermissions(landlordId);
        return Result.ok();
    }

    @Operation(summary = "禁用房东账户")
    @PostMapping("/{landlordId}/disable")
    public Result<Void> disableLandlord(@PathVariable Long landlordId, @RequestParam String reason) {
        log.info("禁用房东账户: landlordId={}, reason={}", landlordId, reason);
        
        landlordPermissionService.disableLandlord(landlordId, reason);
        return Result.ok();
    }

    @Operation(summary = "启用房东账户")
    @PostMapping("/{landlordId}/enable")
    public Result<Void> enableLandlord(@PathVariable Long landlordId) {
        log.info("启用房东账户: landlordId={}", landlordId);
        
        landlordPermissionService.enableLandlord(landlordId);
        return Result.ok();
    }
}