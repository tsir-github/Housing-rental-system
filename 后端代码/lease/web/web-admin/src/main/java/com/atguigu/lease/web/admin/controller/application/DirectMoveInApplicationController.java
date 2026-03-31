package com.atguigu.lease.web.admin.controller.application;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.model.enums.DirectMoveInStatus;
import com.atguigu.lease.web.admin.service.DirectMoveInApplicationService;
import com.atguigu.lease.web.admin.vo.application.DirectMoveInApplicationDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 官方直接入住申请管理控制器
 * 职责：官方人员管理所有直接入住申请，支持分类查询
 * 用户：官方人员（system_user表）
 */
@Tag(name = "官方直接入住申请管理")
@RestController
@RequestMapping("/admin/application/direct-move-in")
@Slf4j
public class DirectMoveInApplicationController {

    @Autowired
    private DirectMoveInApplicationService directMoveInApplicationService;

    @Operation(summary = "官方分页查询待处理申请列表（支持分类筛选）")
    @GetMapping("/pending")
    public Result<IPage<DirectMoveInApplicationDetailVO>> getPendingApplications(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(required = false) Long processorId,
            @RequestParam(required = false) Integer processorType,
            @RequestParam(required = false) Integer publisherType) {
        
        log.info("查询待处理申请列表 - current: {}, size: {}, processorId: {}, processorType: {}, publisherType: {}", 
                current, size, processorId, processorType, publisherType);
        
        Page<DirectMoveInApplicationDetailVO> page = new Page<>(current, size);
        
        IPage<DirectMoveInApplicationDetailVO> result = directMoveInApplicationService
                .getPendingApplicationsWithDetails(page, processorId, processorType, publisherType);
        
        log.info("查询结果 - 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());
        
        return Result.ok(result);
    }

    @Operation(summary = "官方分页查询申请历史（支持分类筛选）")
    @GetMapping("/history")
    public Result<IPage<DirectMoveInApplicationDetailVO>> getApplicationHistory(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) DirectMoveInStatus status,
            @RequestParam(required = false) Long processorId,
            @RequestParam(required = false) Integer processorType,
            @RequestParam(required = false) Integer publisherType) {
        
        log.info("查询申请历史 - current: {}, size: {}, userId: {}, status: {}, processorId: {}, processorType: {}, publisherType: {}", 
                current, size, userId, status, processorId, processorType, publisherType);
        
        Page<DirectMoveInApplicationDetailVO> page = new Page<>(current, size);
        
        Integer statusCode = status != null ? status.getCode() : null;
        
        IPage<DirectMoveInApplicationDetailVO> result = directMoveInApplicationService
                .getApplicationHistoryWithDetails(page, userId, statusCode, processorId, processorType, publisherType);
        
        log.info("查询结果 - 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());
        
        return Result.ok(result);
    }

    @Operation(summary = "官方查看申请详情")
    @GetMapping("/{id}")
    public Result<DirectMoveInApplicationDetailVO> getApplicationById(@PathVariable Long id) {
        DirectMoveInApplicationDetailVO application = directMoveInApplicationService.getApplicationDetailById(id);
        if (application == null) {
            return Result.fail("申请记录不存在");
        }
        return Result.ok(application);
    }

    @Operation(summary = "官方处理直接入住申请（审核通过时自动生成租约）")
    @PostMapping("/process/{id}")
    public Result<Object> processApplication(
            @PathVariable Long id,
            @RequestParam Long processorId,
            @RequestParam Integer processorType,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {
        
        try {
            directMoveInApplicationService.processApplication(id, processorId, processorType, approved, rejectionReason);
            
            if (approved) {
                // 申请通过时，Service已自动生成租约，这里获取租约ID返回
                // TODO: 需要在Service中添加方法获取生成的租约ID
                return Result.ok("申请已通过，租约已自动生成");
            } else {
                return Result.ok("申请已拒绝");
            }
        } catch (Exception e) {
            log.error("处理直接入住申请失败", e);
            return Result.fail("处理申请失败：" + e.getMessage());
        }
    }

    // 删除冗余的approve接口，因为process接口已经包含了所有功能

    @Operation(summary = "官方拒绝申请")
    @PostMapping("/reject/{id}")
    public Result<Void> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long processorId,
            @RequestParam Integer processorType,
            @RequestParam String rejectionReason) {
        
        try {
            directMoveInApplicationService.processApplication(id, processorId, processorType, false, rejectionReason);
            return Result.ok();
        } catch (Exception e) {
            log.error("拒绝申请失败", e);
            return Result.fail("拒绝申请失败：" + e.getMessage());
        }
    }

    @Operation(summary = "官方检查房间可用性")
    @GetMapping("/room/{roomId}/availability")
    public Result<Boolean> checkRoomAvailability(@PathVariable Long roomId) {
        try {
            boolean available = directMoveInApplicationService.isRoomAvailable(roomId);
            return Result.ok(available);
        } catch (Exception e) {
            log.error("检查房间可用性失败", e);
            return Result.fail("检查失败：" + e.getMessage());
        }
    }
}