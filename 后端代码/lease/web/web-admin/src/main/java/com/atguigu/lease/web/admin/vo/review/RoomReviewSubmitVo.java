package com.atguigu.lease.web.admin.vo.review;

import com.atguigu.lease.model.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;


import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "房源审核提交信息")
public class RoomReviewSubmitVo {

    @Schema(description = "审核ID（更新时需要）")
    private Long id;

    @NotNull(message = "房间ID不能为空")
    @Schema(description = "房间ID")
    private Long roomId;

    @NotNull(message = "公寓ID不能为空")
    @Schema(description = "公寓ID")
    private Long apartmentId;

    @Schema(description = "房东ID")
    private Long landlordId;

    @NotNull(message = "审核员ID不能为空")
    @Schema(description = "审核员ID")
    private Long reviewerId;

    @NotNull(message = "符合率评分不能为空")
    @DecimalMin(value = "0.0", message = "符合率评分不能小于0")
    @DecimalMax(value = "10.0", message = "符合率评分不能大于10")
    @Schema(description = "符合率评分（0-10分）")
    private BigDecimal complianceScore;

    @DecimalMin(value = "0.0", message = "照片质量评分不能小于0")
    @DecimalMax(value = "10.0", message = "照片质量评分不能大于10")
    @Schema(description = "照片质量评分")
    private BigDecimal photoQualityScore;

    @DecimalMin(value = "0.0", message = "描述准确性评分不能小于0")
    @DecimalMax(value = "10.0", message = "描述准确性评分不能大于10")
    @Schema(description = "描述准确性评分")
    private BigDecimal descriptionAccuracyScore;

    @DecimalMin(value = "0.0", message = "设施完整性评分不能小于0")
    @DecimalMax(value = "10.0", message = "设施完整性评分不能大于10")
    @Schema(description = "设施完整性评分")
    private BigDecimal facilityCompletenessScore;

    @DecimalMin(value = "0.0", message = "位置准确性评分不能小于0")
    @DecimalMax(value = "10.0", message = "位置准确性评分不能大于10")
    @Schema(description = "位置准确性评分")
    private BigDecimal locationAccuracyScore;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态")
    private ReviewStatus status;

    @Min(value = 1, message = "综合评级必须在1-4之间")
    @Max(value = 4, message = "综合评级必须在1-4之间")
    @Schema(description = "综合评级：1-优秀，2-良好，3-一般，4-较差")
    private Integer overallRating;

    @Schema(description = "审核意见")
    private String reviewComments;

    @Schema(description = "拒绝原因列表")
    private List<String> rejectionReasons;
}