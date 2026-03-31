package com.atguigu.lease.web.app.vo.landlord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房东申请统计信息VO
 */
@Schema(description = "房东申请统计信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordApplicationStatisticsVo {

    @Schema(description = "总申请数")
    private Long totalApplications;

    @Schema(description = "待处理申请数")
    private Long pendingApplications;

    @Schema(description = "已通过申请数")
    private Long approvedApplications;

    @Schema(description = "已拒绝申请数")
    private Long rejectedApplications;

    @Schema(description = "通过率（百分比，保留两位小数）")
    private Double approvalRate;

    @Schema(description = "今日新申请数")
    private Long todayApplications;

    @Schema(description = "本月新申请数")
    private Long monthApplications;
}