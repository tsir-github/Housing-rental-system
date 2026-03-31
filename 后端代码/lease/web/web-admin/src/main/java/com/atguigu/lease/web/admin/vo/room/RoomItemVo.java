package com.atguigu.lease.web.admin.vo.room;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;


@Data
@Schema(description = "房间信息")
public class RoomItemVo extends RoomInfo {

    @Schema(description = "租约结束日期")
    private Date leaseEndDate;

    @Schema(description = "当前入住状态")
    private Boolean isCheckIn;

    @Schema(description = "所属公寓信息")
    private ApartmentInfo apartmentInfo;

    @Schema(description = "审核信息(仅房东发布的房间有)")
    private RoomReviewInfo reviewInfo;

    @Schema(description = "房源审核信息")
    @Data
    public static class RoomReviewInfo {
        @Schema(description = "审核记录ID")
        private Long reviewId;

        @Schema(description = "审核员ID")
        private Long reviewerId;

        @Schema(description = "审核员姓名")
        private String reviewerName;

        @Schema(description = "审核状态：1-待审核，2-审核通过，3-审核拒绝")
        private com.atguigu.lease.model.enums.ReviewStatus reviewStatus;

        @Schema(description = "符合率评分（0-10分）")
        private java.math.BigDecimal complianceScore;

        @Schema(description = "照片质量评分")
        private java.math.BigDecimal photoQualityScore;

        @Schema(description = "描述准确性评分")
        private java.math.BigDecimal descriptionAccuracyScore;

        @Schema(description = "设施完整性评分")
        private java.math.BigDecimal facilityCompletenessScore;

        @Schema(description = "位置准确性评分")
        private java.math.BigDecimal locationAccuracyScore;

        @Schema(description = "综合评级：1-优秀，2-良好，3-一般，4-较差")
        private Integer overallRating;

        @Schema(description = "审核意见")
        private String reviewComments;

        @Schema(description = "拒绝原因（JSON格式存储多个原因）")
        private String rejectionReasons;

        @Schema(description = "审核完成时间")
        private Date reviewedAt;
    }
}
