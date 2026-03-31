package com.atguigu.lease.web.app.vo.landlord;

import com.atguigu.lease.model.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 房东房源审核状态VO
 */
@Schema(description = "房东房源审核状态VO")
@Data
public class RoomReviewStatusVo {

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "租金")
    private BigDecimal rent;

    @Schema(description = "公寓ID")
    private Long apartmentId;

    @Schema(description = "公寓名称")
    private String apartmentName;

    @Schema(description = "公寓地址")
    private String apartmentAddress;

    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;

    @Schema(description = "审核状态描述")
    private String reviewStatusDesc;

    @Schema(description = "符合率评分")
    private BigDecimal complianceScore;

    @Schema(description = "综合评分")
    private Integer overallRating;

    @Schema(description = "最后审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastReviewedAt;

    @Schema(description = "是否发布")
    private Integer isRelease;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Schema(description = "改进建议")
    private String improvementSuggestion;

    @Schema(description = "审核记录ID")
    private Long reviewId;

    @Schema(description = "审核员姓名")
    private String reviewerName;

    @Schema(description = "审核意见")
    private String reviewComments;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewedAt;

    @Schema(description = "照片质量评分")
    private BigDecimal photoQualityScore;

    @Schema(description = "描述准确性评分")
    private BigDecimal descriptionAccuracyScore;

    @Schema(description = "设施完整性评分")
    private BigDecimal facilityCompletenessScore;

    @Schema(description = "位置准确性评分")
    private BigDecimal locationAccuracyScore;
}