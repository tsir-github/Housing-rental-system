package com.atguigu.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.CreatorType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Schema(description = "标签信息表")
@TableName(value = "label_info")
@Data
public class LabelInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "类型")
    @TableField(value = "type")
    private ItemType type;

    @Schema(description = "标签名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "创建者类型")
    @TableField(value = "creator_type")
    private CreatorType creatorType;

    @Schema(description = "创建者ID")
    @TableField(value = "creator_id")
    private Long creatorId;

}