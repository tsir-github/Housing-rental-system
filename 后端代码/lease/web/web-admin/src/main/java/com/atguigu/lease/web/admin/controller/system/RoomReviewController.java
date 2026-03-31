package com.atguigu.lease.web.admin.controller.system;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.web.admin.service.PropertyReviewService;
import com.atguigu.lease.web.admin.service.RoomReviewService;
import com.atguigu.lease.web.admin.vo.review.RoomReviewQueryVo;
import com.atguigu.lease.web.admin.vo.review.RoomReviewSubmitVo;
import com.atguigu.lease.web.admin.vo.review.RoomReviewVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 房源审核管理控制器
 */
@Tag(name = "房源审核管理")
@RestController
@RequestMapping("/admin/room/review")
@Slf4j
public class RoomReviewController {

    @Autowired
    private RoomReviewService roomReviewService;

    @Autowired
    private PropertyReviewService propertyReviewService;

    @Operation(summary = "分页查询待审核房源列表")
    @GetMapping("/pending")
    public Result<IPage<RoomReviewVo>> getPendingReviews(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            RoomReviewQueryVo queryVo) {
        
        // 设置查询条件为待审核状态
        queryVo.setStatus(ReviewStatus.PENDING);
        
        IPage<RoomReviewVo> page = new Page<>(current, size);
        IPage<RoomReviewVo> result = roomReviewService.pageRoomReviewByQuery(page, queryVo);
        
        return Result.ok(result);
    }

    @Operation(summary = "分页查询房源审核历史")
    @GetMapping("/history")
    public Result<IPage<RoomReviewVo>> getReviewHistory(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            RoomReviewQueryVo queryVo) {
        
        IPage<RoomReviewVo> page = new Page<>(current, size);
        IPage<RoomReviewVo> result = roomReviewService.pageRoomReviewByQuery(page, queryVo);
        
        return Result.ok(result);
    }

    @Operation(summary = "根据ID获取房源审核详情")
    @GetMapping("/{id}")
    public Result<RoomReviewVo> getReviewById(@PathVariable Long id) {
        RoomReviewVo reviewVo = roomReviewService.getRoomReviewById(id);
        if (reviewVo == null) {
            return Result.fail("审核记录不存在");
        }
        return Result.ok(reviewVo);
    }

    @Operation(summary = "手动审核提交房源审核")
    @PostMapping("/submit")
    public Result<Void> submitReview(@Valid @RequestBody RoomReviewSubmitVo submitVo) {
        try {
            roomReviewService.submitRoomReview(submitVo);
            return Result.ok();
        } catch (Exception e) {
            log.error("提交房源审核失败", e);
            return Result.fail("提交审核失败：" + e.getMessage());
        }
    }

    @Operation(summary = "自动审核房源")
    @PostMapping("/auto/{roomId}")
    public Result<Void> autoReviewRoom(
            @PathVariable Long roomId,
            @RequestParam Long reviewerId) {
        try {
            roomReviewService.autoReviewRoom(roomId, reviewerId);
            return Result.ok();
        } catch (Exception e) {
            log.error("自动审核房源失败", e);
            return Result.fail("自动审核失败：" + e.getMessage());
        }
    }

    @Operation(summary = "执行智能综合审核")
    @PostMapping("/comprehensive/{roomId}")
    public Result<PropertyReviewService.PropertyReviewResult> comprehensiveReview(
            @PathVariable Long roomId,
            @RequestParam Long reviewerId) {
        try {
            PropertyReviewService.PropertyReviewResult result = 
                propertyReviewService.executeComprehensiveReview(roomId, reviewerId);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("智能综合审核失败", e);
            return Result.fail("智能审核失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房源综合评分")
    @GetMapping("/score/{roomId}")
    public Result<Object> getRoomScore(@PathVariable Long roomId) {
        try {
            // 计算各项评分
            BigDecimal complianceScore = roomReviewService.calculateComplianceScore(roomId);
            BigDecimal photoQualityScore = roomReviewService.calculatePhotoQualityScore(roomId);
            BigDecimal descriptionAccuracyScore = roomReviewService.calculateDescriptionAccuracyScore(roomId);
            BigDecimal facilityCompletenessScore = roomReviewService.calculateFacilityCompletenessScore(roomId);
            BigDecimal locationAccuracyScore = roomReviewService.calculateLocationAccuracyScore(roomId);
            BigDecimal overallScore = propertyReviewService.calculateOverallScore(roomId);

            // 构建返回结果
            java.util.Map<String, BigDecimal> scoreResult = new java.util.HashMap<>();
            scoreResult.put("complianceScore", complianceScore);
            scoreResult.put("photoQualityScore", photoQualityScore);
            scoreResult.put("descriptionAccuracyScore", descriptionAccuracyScore);
            scoreResult.put("facilityCompletenessScore", facilityCompletenessScore);
            scoreResult.put("locationAccuracyScore", locationAccuracyScore);
            scoreResult.put("overallScore", overallScore);

            return Result.ok(scoreResult);
        } catch (Exception e) {
            log.error("获取房源评分失败", e);
            return Result.fail("获取评分失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查看房源审核状态")
    @GetMapping("/landlord/{landlordId}")
    public Result<IPage<RoomReviewVo>> getLandlordReviews(
            @PathVariable Long landlordId,
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        
        RoomReviewQueryVo queryVo = new RoomReviewQueryVo();
        queryVo.setLandlordId(landlordId);
        
        IPage<RoomReviewVo> page = new Page<>(current, size);
        IPage<RoomReviewVo> result = roomReviewService.pageRoomReviewByQuery(page, queryVo);
        
        return Result.ok(result);
    }

    @Operation(summary = "根据房间ID查询审核状态")
    @GetMapping("/room/{roomId}/status")
    public Result<RoomReviewVo> getRoomReviewStatus(@PathVariable Long roomId) {
        try {
            // 查询最新的审核记录
            LambdaQueryWrapper<RoomReview> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoomReview::getRoomId, roomId)
                       .orderByDesc(RoomReview::getCreateTime)
                       .last("LIMIT 1");
            
            RoomReview latestReview = roomReviewService.getOne(queryWrapper);
            if (latestReview == null) {
                return Result.fail("该房源暂无审核记录");
            }
            
            RoomReviewVo reviewVo = roomReviewService.getRoomReviewById(latestReview.getId());
            return Result.ok(reviewVo);
        } catch (Exception e) {
            log.error("查询房源审核状态失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "批量审核房源")
    @PostMapping("/batch")
    public Result<Void> batchReview(@RequestBody List<RoomReviewSubmitVo> submitVoList) {
        try {
            for (RoomReviewSubmitVo submitVo : submitVoList) {
                roomReviewService.submitRoomReview(submitVo);
            }
            return Result.ok();
        } catch (Exception e) {
            log.error("批量审核房源失败", e);
            return Result.fail("批量审核失败：" + e.getMessage());
        }
    }
}