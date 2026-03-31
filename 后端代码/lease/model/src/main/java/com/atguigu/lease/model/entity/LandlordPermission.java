package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "房东权限表")
@TableName(value = "landlord_permission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房东用户ID
     */
    @Schema(description = "房东用户ID")
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 权限代码
     */
    @Schema(description = "权限代码")
    @TableField(value = "permission_code")
    private String permissionCode;

    /**
     * 授权时间
     */
    @Schema(description = "权限授予时间")
    @TableField(value = "granted_at")
    private LocalDateTime grantedAt;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除
     * 1: 已删除
     * 0: 未删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}