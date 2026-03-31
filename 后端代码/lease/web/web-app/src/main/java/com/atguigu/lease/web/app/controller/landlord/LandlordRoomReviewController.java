package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.web.app.service.RoomReviewService;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewDetailVo;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewStatusVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房东房源审核管理控制器
 * 整合了房东查看房源审核状态、审核历史、统计信息等功能
 */
@Tag(name = "房东房源审核管理")
@RestController
@RequestMapping("/app/landlord/review")
@Slf4j
public class LandlordRoomReviewController {

    @Autowired
    private RoomReviewService roomReviewService;

    @Operation(summary = "房东查看自己房源的审核状态")
    @GetMapping("/status")
    public Result<IPage<RoomReviewStatusVo>> getMyReviewStatus(
            @RequestParam Long landlordId,
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(required = false) Integer reviewStatus) {
        
        try {
            log.info("房东查看审核状态：房东ID={}, 页码={}, 大小={}, 状态筛选={}", landlordId, current, size, reviewStatus);
            
            Page<RoomReviewStatusVo> page = new Page<>(current, size);
            IPage<RoomReviewStatusVo> result = roomReviewService.getLandlordRoomReviewStatus(page, landlordId, reviewStatus);
            
            log.info("房东审核状态查询成功：房东ID={}, 总数={}", landlordId, result.getTotal());
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询房东房源审核状态失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查看特定房源的审核历史")
    @GetMapping("/review-history/{roomId}")
    public Result<List<RoomReview>> getReviewHistory(@PathVariable Long roomId, @RequestParam Long landlordId) {
        try {
            log.info("房东查看审核历史：房源ID={}, 房东ID={}", roomId, landlordId);
            
            // 获取所有审核历史记录
            List<RoomReview> reviewHistory = roomReviewService.getAllReviewHistoryByRoomId(roomId, landlordId);
            
            log.info("房东审核历史查询成功：房源ID={}, 记录数={}", roomId, reviewHistory.size());
            return Result.ok(reviewHistory);
        } catch (Exception e) {
            log.error("查询房源审核历史失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查看审核统计信息")
    @GetMapping("/review-statistics")
    public Result<Map<String, Object>> getReviewStatistics(@RequestParam Long landlordId) {
        try {
            log.info("房东查看审核统计：房东ID={}", landlordId);
            
            // 使用新的Service方法获取统计信息
            Page<RoomReviewStatusVo> page = new Page<>(1, Integer.MAX_VALUE);
            IPage<RoomReviewStatusVo> allRooms = roomReviewService.getLandlordRoomReviewStatus(page, landlordId, null);
            
            // 统计审核状态
            long totalRooms = allRooms.getTotal();
            long pendingReview = allRooms.getRecords().stream()
                .filter(room -> room.getReviewStatus() != null && room.getReviewStatus().getCode() == 1)
                .count();
            long approved = allRooms.getRecords().stream()
                .filter(room -> room.getReviewStatus() != null && room.getReviewStatus().getCode() == 2)
                .count();
            long rejected = allRooms.getRecords().stream()
                .filter(room -> room.getReviewStatus() != null && room.getReviewStatus().getCode() == 3)
                .count();
            
            // 计算平均符合率评分
            double avgComplianceScore = allRooms.getRecords().stream()
                .filter(room -> room.getComplianceScore() != null)
                .mapToDouble(room -> room.getComplianceScore().doubleValue())
                .average()
                .orElse(0.0);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRooms", totalRooms);
            statistics.put("pendingReview", pendingReview);
            statistics.put("approved", approved);
            statistics.put("rejected", rejected);
            statistics.put("avgComplianceScore", Math.round(avgComplianceScore * 10.0) / 10.0);
            statistics.put("approvalRate", totalRooms > 0 ? 
                Math.round((approved * 100.0 / totalRooms) * 10.0) / 10.0 : 0.0);
            
            log.info("房东审核统计查询成功：房东ID={}, 总房源数={}", landlordId, totalRooms);
            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("查询房东审核统计失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查看审核详情")
    @GetMapping("/detail/{roomId}")
    public Result<RoomReviewDetailVo> getReviewDetail(
            @PathVariable Long roomId,
            @RequestParam Long landlordId) {
        
        try {
            log.info("房东查看审核详情：房源ID={}, 房东ID={}", roomId, landlordId);
            
            RoomReviewDetailVo result = roomReviewService.getRoomReviewDetail(roomId, landlordId);
            
            log.info("房东审核详情查询成功：房源ID={}", roomId);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询审核详情失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }
}