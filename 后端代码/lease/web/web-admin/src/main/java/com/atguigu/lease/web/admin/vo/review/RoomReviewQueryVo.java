package com.atguigu.lease.web.admin.vo.review;

import com.atguigu.lease.model.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "房源审核查询条件")
public class RoomReviewQueryVo {

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "公寓ID")
    private Long apartmentId;

    @Schema(description = "房东ID")
    private Long landlordId;

    @Schema(description = "审核员ID")
    private Long reviewerId;

    @Schema(description = "审核状态")
    private ReviewStatus status;

    @Schema(description = "符合率评分最小值")
    private Double minComplianceScore;

    @Schema(description = "符合率评分最大值")
    private Double maxComplianceScore;

    @Schema(description = "审核开始时间")
    private Date reviewStartTime;

    @Schema(description = "审核结束时间")
    private Date reviewEndTime;
}