package com.atguigu.lease.web.admin.vo.room;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 房间打回请求VO
 */
@Data
@Schema(description = "房间打回请求")
public class RoomRejectVo {

    @Schema(description = "房间ID")
    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @Schema(description = "打回原因")
    @NotBlank(message = "打回原因不能为空")
    private String rejectReason;
}