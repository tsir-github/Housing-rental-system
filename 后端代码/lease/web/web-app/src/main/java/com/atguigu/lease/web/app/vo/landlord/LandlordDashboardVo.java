package com.atguigu.lease.web.app.vo.landlord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房东仪表板数据VO
 */
@Schema(description = "房东仪表板数据")
@Data
public class LandlordDashboardVo {

    @Schema(description = "房东ID")
    private Long landlordId;

    @Schema(description = "公寓总数")
    private Long apartmentCount;

    @Schema(description = "房间总数")
    private Long roomCount;

    @Schema(description = "已发布房间数")
    private Long publishedRoomCount;

    @Schema(description = "可租房间数")
    private Long availableRoomCount;

    @Schema(description = "已出租房间数")
    private Long occupiedRoomCount;

    @Schema(description = "待处理申请数")
    private Long pendingApplicationCount;

    @Schema(description = "待确认预约数")
    private Long pendingAppointmentCount;

    @Schema(description = "今日预约数")
    private Long todayAppointmentCount;

    @Schema(description = "出租率（百分比）")
    private Double occupancyRate;

    @Schema(description = "本月新增房源数")
    private Long monthlyNewRooms;

    @Schema(description = "本月成交数")
    private Long monthlyDeals;

    @Schema(description = "本月收入")
    private Double monthlyIncome;

    @Schema(description = "数据更新时间")
    private LocalDateTime lastUpdateTime;

    @Schema(description = "房东等级")
    private String landlordLevel;

    @Schema(description = "信用评分")
    private Integer creditScore;
}