package com.atguigu.lease.web.app.vo.landlord;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 房东房源审核详情VO
 */
@Schema(description = "房东房源审核详情VO")
@Data
public class RoomReviewDetailVo {

    @Schema(description = "房间信息")
    private RoomInfo roomInfo;

    @Schema(description = "公寓信息")
    private ApartmentInfo apartmentInfo;

    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;

    @Schema(description = "审核状态描述")
    private String reviewStatusDesc;

    @Schema(description = "符合率评分")
    private BigDecimal complianceScore;

    @Schema(description = "最后审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastReviewedAt;

    @Schema(description = "最新审核记录")
    private RoomReview latestReview;

    @Schema(description = "综合评分")
    private Integer overallRating;

    @Schema(description = "审核意见")
    private String reviewComments;

    @Schema(description = "照片质量评分")
    private BigDecimal photoQualityScore;

    @Schema(description = "描述准确性评分")
    private BigDecimal descriptionAccuracyScore;

    @Schema(description = "设施完整性评分")
    private BigDecimal facilityCompletenessScore;

    @Schema(description = "位置准确性评分")
    private BigDecimal locationAccuracyScore;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewedAt;

    @Schema(description = "审核员姓名")
    private String reviewerName;

    @Schema(description = "改进建议")
    private String improvementSuggestion;

    @Schema(description = "审核历史记录")
    private List<RoomReview> reviewHistory;
}