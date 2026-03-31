package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.enums.DirectMoveInStatus;
import com.atguigu.lease.web.app.service.DirectMoveInApplicationService;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationDetailVO;
import com.atguigu.lease.web.app.vo.landlord.LandlordApplicationStatisticsVo;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 房东直接入住申请管理控制器
 * 职责：房东处理自己房源的直接入住申请
 * 用户：房东（user_info表，user_type=房东）
 */
@Slf4j
@Tag(name = "房东直接入住申请管理")
@RestController
@RequestMapping("/app/landlord/applications")
public class LandlordApplicationController {

    @Autowired
    private DirectMoveInApplicationService directMoveInApplicationService;

    @Operation(summary = "房东查看待处理申请列表")
    @GetMapping("/pending")
    public Result<List<DirectMoveInApplicationDetailVO>> getPendingApplications(
            @RequestParam Long landlordId) {
        
        List<DirectMoveInApplicationDetailVO> applications = 
            directMoveInApplicationService.getPendingApplicationsByLandlord(landlordId);
        return Result.ok(applications);
    }

    @Operation(summary = "房东查看申请历史")
    @GetMapping("/history")
    public Result<List<DirectMoveInApplicationDetailVO>> getApplicationHistory(
            @RequestParam Long landlordId) {
        
        // 获取房东的所有申请历史（包括待处理、已通过、已拒绝）
        List<DirectMoveInApplicationDetailVO> applications = 
            directMoveInApplicationService.getApplicationsByLandlord(landlordId);
        return Result.ok(applications);
    }

    @Operation(summary = "房东查看申请详情")
    @GetMapping("/detail/{applicationId}")
    public Result<DirectMoveInApplicationDetailVO> getApplicationDetail(@PathVariable Long applicationId) {
        DirectMoveInApplicationDetailVO detail = 
            directMoveInApplicationService.getApplicationDetail(applicationId);
        return Result.ok(detail);
    }

    @Operation(summary = "房东处理申请（审核通过/拒绝）")
    @PostMapping("/process/{id}")
    public Result<Void> processApplication(
            @PathVariable Long id,
            @RequestParam Long landlordId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {
        
        try {
            // processorType = 2 表示房东处理
            directMoveInApplicationService.processApplication(id, landlordId, 2, approved, rejectionReason);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail("处理申请失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东确认申请并生成租约")
    @PostMapping("/approve/{id}")
    public Result<Long> approveApplication(
            @PathVariable Long id,
            @RequestParam Long landlordId) {
        
        try {
            // 处理申请（房东确认）并自动生成租约
            directMoveInApplicationService.processApplication(id, landlordId, 2, true, null);
            
            // 获取生成的租约ID
            Long leaseId = directMoveInApplicationService.getLeaseIdByApplicationId(id);
            
            if (leaseId != null) {
                log.info("房东确认申请成功，已生成租约 - applicationId: {}, leaseId: {}", id, leaseId);
                return Result.ok(leaseId);
            } else {
                log.warn("房东确认申请成功，但未找到对应租约 - applicationId: {}", id);
                return Result.ok(id); // 返回申请ID作为备用
            }
        } catch (Exception e) {
            log.error("房东确认申请失败 - applicationId: {}, landlordId: {}", id, landlordId, e);
            return Result.fail("确认申请失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东拒绝申请")
    @PostMapping("/reject/{id}")
    public Result<Void> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long landlordId,
            @RequestParam String rejectionReason) {
        
        try {
            directMoveInApplicationService.processApplication(id, landlordId, 2, false, rejectionReason);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail("拒绝申请失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东检查房间可用性")
    @GetMapping("/room/{roomId}/availability")
    public Result<Boolean> checkRoomAvailability(@PathVariable Long roomId) {
        try {
            boolean available = directMoveInApplicationService.validateRoomAvailability(roomId);
            return Result.ok(available);
        } catch (Exception e) {
            return Result.fail("检查房间可用性失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东获取申请对应的租约ID")
    @GetMapping("/{applicationId}/lease")
    public Result<Long> getApplicationLeaseId(@PathVariable Long applicationId) {
        try {
            Long leaseId = directMoveInApplicationService.getLeaseIdByApplicationId(applicationId);
            if (leaseId == null) {
                return Result.fail("该申请尚未生成租约");
            }
            return Result.ok(leaseId);
        } catch (Exception e) {
            return Result.fail("获取租约信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东获取申请统计")
    @GetMapping("/statistics")
    public Result<LandlordApplicationStatisticsVo> getApplicationStatistics(@RequestParam Long landlordId) {
        try {
            LandlordApplicationStatisticsVo statistics = directMoveInApplicationService.getApplicationStatisticsByLandlord(landlordId);
            return Result.ok(statistics);
        } catch (Exception e) {
            return Result.fail("获取统计数据失败：" + e.getMessage());
        }
    }
}