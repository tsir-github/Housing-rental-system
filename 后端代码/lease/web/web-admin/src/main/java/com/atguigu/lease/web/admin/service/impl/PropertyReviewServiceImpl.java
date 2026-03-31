package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.web.admin.service.PropertyReviewService;
import com.atguigu.lease.web.admin.service.RoomReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 房源审核核心服务实现类
 * 实现智能审核算法和通知功能
 */
@Slf4j
@Service
public class PropertyReviewServiceImpl implements PropertyReviewService {

    @Autowired
    private RoomReviewService roomReviewService;

    // 评分权重配置
    private static final BigDecimal COMPLIANCE_WEIGHT = BigDecimal.valueOf(0.40);      // 符合率权重40%
    private static final BigDecimal PHOTO_WEIGHT = BigDecimal.valueOf(0.15);           // 照片质量权重15%
    private static final BigDecimal DESCRIPTION_WEIGHT = BigDecimal.valueOf(0.15);     // 描述准确性权重15%
    private static final BigDecimal FACILITY_WEIGHT = BigDecimal.valueOf(0.15);       // 设施完整性权重15%
    private static final BigDecimal LOCATION_WEIGHT = BigDecimal.valueOf(0.15);       // 位置准确性权重15%

    // 评级阈值配置
    private static final BigDecimal EXCELLENT_THRESHOLD = BigDecimal.valueOf(8.5);     // 优秀阈值
    private static final BigDecimal GOOD_THRESHOLD = BigDecimal.valueOf(7.0);          // 良好阈值
    private static final BigDecimal AVERAGE_THRESHOLD = BigDecimal.valueOf(5.0);       // 一般阈值
    private static final BigDecimal PASS_THRESHOLD = BigDecimal.valueOf(4.0);          // 通过阈值

    @Override
    public PropertyReviewResult executeComprehensiveReview(Long roomId, Long reviewerId) {
        log.info("开始执行房源{}的综合审核，审核员ID：{}", roomId, reviewerId);

        // 计算各项评分
        BigDecimal complianceScore = roomReviewService.calculateComplianceScore(roomId);
        BigDecimal photoQualityScore = roomReviewService.calculatePhotoQualityScore(roomId);
        BigDecimal descriptionAccuracyScore = roomReviewService.calculateDescriptionAccuracyScore(roomId);
        BigDecimal facilityCompletenessScore = roomReviewService.calculateFacilityCompletenessScore(roomId);
        BigDecimal locationAccuracyScore = roomReviewService.calculateLocationAccuracyScore(roomId);

        log.info("房源{}评分详情 - 符合率:{}, 照片质量:{}, 描述准确性:{}, 设施完整性:{}, 位置准确性:{}",
                roomId, complianceScore, photoQualityScore, descriptionAccuracyScore,
                facilityCompletenessScore, locationAccuracyScore);

        // 计算综合评分
        BigDecimal overallScore = calculateWeightedScore(
                complianceScore, photoQualityScore, descriptionAccuracyScore,
                facilityCompletenessScore, locationAccuracyScore);

        // 确定审核状态和评级
        ReviewStatus status = determineReviewStatus(overallScore);
        Integer overallRating = determineOverallRating(overallScore);
        String comments = generateReviewComments(overallScore, status);

        log.info("房源{}审核完成 - 综合评分:{}, 状态:{}, 评级:{}", roomId, overallScore, status, overallRating);

        // 发送通知
        sendReviewNotification(roomId, status, comments);

        return new PropertyReviewResult(
                status, overallRating, comments,
                complianceScore, photoQualityScore, descriptionAccuracyScore,
                facilityCompletenessScore, locationAccuracyScore
        );
    }

    @Override
    public BigDecimal calculateOverallScore(Long roomId) {
        BigDecimal complianceScore = roomReviewService.calculateComplianceScore(roomId);
        BigDecimal photoQualityScore = roomReviewService.calculatePhotoQualityScore(roomId);
        BigDecimal descriptionAccuracyScore = roomReviewService.calculateDescriptionAccuracyScore(roomId);
        BigDecimal facilityCompletenessScore = roomReviewService.calculateFacilityCompletenessScore(roomId);
        BigDecimal locationAccuracyScore = roomReviewService.calculateLocationAccuracyScore(roomId);

        return calculateWeightedScore(
                complianceScore, photoQualityScore, descriptionAccuracyScore,
                facilityCompletenessScore, locationAccuracyScore);
    }

    @Override
    public ReviewStatus determineReviewStatus(BigDecimal overallScore) {
        if (overallScore.compareTo(PASS_THRESHOLD) >= 0) {
            return ReviewStatus.APPROVED;
        } else {
            return ReviewStatus.REJECTED;
        }
    }

    @Override
    public void sendReviewNotification(Long roomId, ReviewStatus status, String comments) {
        // TODO: 实现通知发送逻辑
        // 可以发送短信、邮件、站内消息等
        log.info("发送审核通知 - 房源ID:{}, 状态:{}, 意见:{}", roomId, status, comments);
        
        // 这里可以集成消息队列、短信服务等
        // 例如：
        // messageService.sendNotification(roomId, status, comments);
        // smsService.sendReviewResult(roomId, status);
    }

    /**
     * 计算加权综合评分
     */
    private BigDecimal calculateWeightedScore(BigDecimal complianceScore, BigDecimal photoQualityScore,
                                            BigDecimal descriptionAccuracyScore, BigDecimal facilityCompletenessScore,
                                            BigDecimal locationAccuracyScore) {
        BigDecimal weightedScore = complianceScore.multiply(COMPLIANCE_WEIGHT)
                .add(photoQualityScore.multiply(PHOTO_WEIGHT))
                .add(descriptionAccuracyScore.multiply(DESCRIPTION_WEIGHT))
                .add(facilityCompletenessScore.multiply(FACILITY_WEIGHT))
                .add(locationAccuracyScore.multiply(LOCATION_WEIGHT));

        return weightedScore.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 确定综合评级
     */
    private Integer determineOverallRating(BigDecimal overallScore) {
        if (overallScore.compareTo(EXCELLENT_THRESHOLD) >= 0) {
            return 1; // 优秀
        } else if (overallScore.compareTo(GOOD_THRESHOLD) >= 0) {
            return 2; // 良好
        } else if (overallScore.compareTo(AVERAGE_THRESHOLD) >= 0) {
            return 3; // 一般
        } else {
            return 4; // 较差
        }
    }

    /**
     * 生成审核意见
     */
    private String generateReviewComments(BigDecimal overallScore, ReviewStatus status) {
        StringBuilder comments = new StringBuilder();
        comments.append("系统智能审核结果：");

        if (status == ReviewStatus.APPROVED) {
            if (overallScore.compareTo(EXCELLENT_THRESHOLD) >= 0) {
                comments.append("房源质量优秀，各项指标均达到高标准，推荐优先展示。");
            } else if (overallScore.compareTo(GOOD_THRESHOLD) >= 0) {
                comments.append("房源质量良好，符合发布标准，建议正常展示。");
            } else if (overallScore.compareTo(AVERAGE_THRESHOLD) >= 0) {
                comments.append("房源质量一般，达到基本发布要求，建议优化后提升展示效果。");
            } else {
                comments.append("房源质量刚好达标，建议完善相关信息以提升用户体验。");
            }
        } else {
            comments.append("房源质量不达标，存在以下问题需要改进：");
            
            // 根据各项评分给出具体建议
            BigDecimal complianceScore = roomReviewService.calculateComplianceScore(overallScore.longValue());
            if (complianceScore.compareTo(BigDecimal.valueOf(6.0)) < 0) {
                comments.append("基础信息不完整；");
            }
            
            BigDecimal photoScore = roomReviewService.calculatePhotoQualityScore(overallScore.longValue());
            if (photoScore.compareTo(BigDecimal.valueOf(6.0)) < 0) {
                comments.append("照片质量或数量不足；");
            }
            
            comments.append("请完善后重新提交审核。");
        }

        comments.append("（综合评分：").append(overallScore).append("分）");
        return comments.toString();
    }
}