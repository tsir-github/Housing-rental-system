package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.web.admin.vo.review.RoomReviewQueryVo;
import com.atguigu.lease.web.admin.vo.review.RoomReviewSubmitVo;
import com.atguigu.lease.web.admin.vo.review.RoomReviewVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * @author system
 * @description 针对表【room_review(房源审核表)】的数据库操作Service
 * @createDate 2024-01-05 00:00:00
 */
public interface RoomReviewService extends IService<RoomReview> {

    /**
     * 分页查询房源审核列表
     */
    IPage<RoomReviewVo> pageRoomReviewByQuery(IPage<RoomReviewVo> page, RoomReviewQueryVo queryVo);

    /**
     * 提交房源审核
     */
    void submitRoomReview(RoomReviewSubmitVo submitVo);

    /**
     * 根据ID获取房源审核详情
     */
    RoomReviewVo getRoomReviewById(Long id);

    /**
     * 计算房源符合率评分
     */
    BigDecimal calculateComplianceScore(Long roomId);

    /**
     * 计算照片质量评分
     */
    BigDecimal calculatePhotoQualityScore(Long roomId);

    /**
     * 计算描述准确性评分
     */
    BigDecimal calculateDescriptionAccuracyScore(Long roomId);

    /**
     * 计算设施完整性评分
     */
    BigDecimal calculateFacilityCompletenessScore(Long roomId);

    /**
     * 计算位置准确性评分
     */
    BigDecimal calculateLocationAccuracyScore(Long roomId);

    /**
     * 自动审核房源（基于算法）
     */
    void autoReviewRoom(Long roomId, Long reviewerId);

    /**
     * 更新房间审核状态
     */
    void updateRoomReviewStatus(Long roomId);

    /**
     * 验证审核员权限
     */
    boolean validateReviewerPermission(Long reviewerId);
}