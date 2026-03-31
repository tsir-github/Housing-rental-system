package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.ReviewStatus;

import java.math.BigDecimal;

/**
 * 房源审核核心服务
 * 实现房源审核评分算法和审核状态管理
 */
public interface PropertyReviewService {

    /**
     * 执行房源综合审核
     * @param roomId 房间ID
     * @param reviewerId 审核员ID
     * @return 审核结果
     */
    PropertyReviewResult executeComprehensiveReview(Long roomId, Long reviewerId);

    /**
     * 计算房源综合评分
     * @param roomId 房间ID
     * @return 综合评分
     */
    BigDecimal calculateOverallScore(Long roomId);

    /**
     * 根据评分确定审核状态
     * @param overallScore 综合评分
     * @return 审核状态
     */
    ReviewStatus determineReviewStatus(BigDecimal overallScore);

    /**
     * 发送审核通知
     * @param roomId 房间ID
     * @param status 审核状态
     * @param comments 审核意见
     */
    void sendReviewNotification(Long roomId, ReviewStatus status, String comments);

    /**
     * 房源审核结果内部类
     */
    class PropertyReviewResult {
        private ReviewStatus status;
        private Integer overallRating;
        private String comments;
        private BigDecimal complianceScore;
        private BigDecimal photoQualityScore;
        private BigDecimal descriptionAccuracyScore;
        private BigDecimal facilityCompletenessScore;
        private BigDecimal locationAccuracyScore;

        // 构造函数
        public PropertyReviewResult(ReviewStatus status, Integer overallRating, String comments,
                                  BigDecimal complianceScore, BigDecimal photoQualityScore,
                                  BigDecimal descriptionAccuracyScore, BigDecimal facilityCompletenessScore,
                                  BigDecimal locationAccuracyScore) {
            this.status = status;
            this.overallRating = overallRating;
            this.comments = comments;
            this.complianceScore = complianceScore;
            this.photoQualityScore = photoQualityScore;
            this.descriptionAccuracyScore = descriptionAccuracyScore;
            this.facilityCompletenessScore = facilityCompletenessScore;
            this.locationAccuracyScore = locationAccuracyScore;
        }

        // Getters
        public ReviewStatus getStatus() { return status; }
        public Integer getOverallRating() { return overallRating; }
        public String getComments() { return comments; }
        public BigDecimal getComplianceScore() { return complianceScore; }
        public BigDecimal getPhotoQualityScore() { return photoQualityScore; }
        public BigDecimal getDescriptionAccuracyScore() { return descriptionAccuracyScore; }
        public BigDecimal getFacilityCompletenessScore() { return facilityCompletenessScore; }
        public BigDecimal getLocationAccuracyScore() { return locationAccuracyScore; }
    }
}