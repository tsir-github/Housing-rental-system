package com.atguigu.lease.web.app.vo.landlord;

import com.atguigu.lease.web.app.vo.room.RoomSubmitVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房源更新请求VO
 * 继承自RoomSubmitVo，添加权限验证相关字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "房源更新请求")
public class RoomUpdateRequest extends RoomSubmitVo {

    @Schema(description = "房东ID", required = true)
    private Long landlordId;

    @Schema(description = "是否验证权限", defaultValue = "true")
    private Boolean validatePermissions = true;
}