package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.ReviewStatus;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "房源审核表")
@TableName(value = "room_review")
@Data
public class RoomReview extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "房间ID")
    @TableField(value = "room_id")
    private Long roomId;

    @Schema(description = "公寓ID")
    @TableField(value = "apartment_id")
    private Long apartmentId;

    @Schema(description = "房东ID（如果是房东发布的房源）")
    @TableField(value = "landlord_id")
    private Long landlordId;

    @Schema(description = "审核员ID")
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    @Schema(description = "符合率评分（0-10分）")
    @TableField(value = "compliance_score")
    private BigDecimal complianceScore;

    @Schema(description = "照片质量评分")
    @TableField(value = "photo_quality_score")
    private BigDecimal photoQualityScore;

    @Schema(description = "描述准确性评分")
    @TableField(value = "description_accuracy_score")
    private BigDecimal descriptionAccuracyScore;

    @Schema(description = "设施完整性评分")
    @TableField(value = "facility_completeness_score")
    private BigDecimal facilityCompletenessScore;

    @Schema(description = "位置准确性评分")
    @TableField(value = "location_accuracy_score")
    private BigDecimal locationAccuracyScore;

    @Schema(description = "审核状态")
    @TableField(value = "status")
    private ReviewStatus status;

    @Schema(description = "综合评级：1-优秀，2-良好，3-一般，4-较差")
    @TableField(value = "overall_rating")
    private Integer overallRating;

    @Schema(description = "审核意见")
    @TableField(value = "review_comments")
    private String reviewComments;

    @Schema(description = "拒绝原因（JSON格式存储多个原因）")
    @TableField(value = "rejection_reasons")
    private String rejectionReasons;

    @Schema(description = "审核完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "reviewed_at")
    private Date reviewedAt;
}