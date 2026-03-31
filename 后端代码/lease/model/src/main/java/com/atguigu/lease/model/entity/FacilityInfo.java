package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.CreatorType;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Schema(description = "配套信息表")
@TableName(value = "facility_info")
@Data
public class FacilityInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Schema(description = "配套所属对象类型")
    @TableField(value = "type")
    private ItemType type;

    @Schema(description = "名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "图标")
    @TableField(value = "icon")
    private String icon;

    @Schema(description = "创建者类型")
    @TableField(value = "creator_type")
    private CreatorType creatorType;

    @Schema(description = "创建者ID")
    @TableField(value = "creator_id")
    private Long creatorId;

}