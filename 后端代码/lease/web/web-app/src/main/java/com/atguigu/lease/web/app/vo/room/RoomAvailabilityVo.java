package com.atguigu.lease.web.app.vo.room;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间可用性检查结果VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "房间可用性检查结果")
public class RoomAvailabilityVo {

    @Schema(description = "是否可用")
    private Boolean available;

    @Schema(description = "状态码")
    private String statusCode;

    @Schema(description = "友好提示信息")
    private String message;

    @Schema(description = "详细原因")
    private String reason;

    /**
     * 创建可用状态
     */
    public static RoomAvailabilityVo available() {
        return new RoomAvailabilityVo(true, "AVAILABLE", "房源可预约", "房源当前空闲，可以预约看房或直接入住");
    }

    /**
     * 创建房源不存在状态
     */
    public static RoomAvailabilityVo notFound() {
        return new RoomAvailabilityVo(false, "NOT_FOUND", "房源不存在", "该房源可能已被删除或不存在");
    }

    /**
     * 创建房源未发布状态
     */
    public static RoomAvailabilityVo notReleased() {
        return new RoomAvailabilityVo(false, "NOT_RELEASED", "房源暂未开放", "房东暂时未开放此房源，请关注其他房源");
    }

    /**
     * 创建房源未审核状态
     */
    public static RoomAvailabilityVo notApproved() {
        return new RoomAvailabilityVo(false, "NOT_APPROVED", "房源审核中", "房源正在审核中，暂时无法预约");
    }

    /**
     * 创建房源已被占用状态
     */
    public static RoomAvailabilityVo occupied(String leaseStatus) {
        String message;
        String reason;
        
        switch (leaseStatus) {
            case "SIGNING":
                message = "房源预订中";
                reason = "已有租客预订此房源，正在签约确认中";
                break;
            case "SIGNED":
                message = "房源已出租";
                reason = "此房源已有租客入住，暂时无法预约";
                break;
            case "RENEWING":
                message = "房源续约中";
                reason = "现有租客正在办理续约手续，暂时无法预约";
                break;
            default:
                message = "房源暂不可用";
                reason = "房源当前状态不允许新的预约";
        }
        
        return new RoomAvailabilityVo(false, "OCCUPIED", message, reason);
    }
}