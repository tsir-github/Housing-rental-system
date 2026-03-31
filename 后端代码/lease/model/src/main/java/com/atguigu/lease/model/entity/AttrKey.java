package com.atguigu.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Schema(description = "房间基本属性表")
@TableName(value = "attr_key")
@Data
public class AttrKey extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "属性key")
    @TableField(value = "name")
    private String name;

    @Schema(description = "创建者类型：1-官方，2-房东")
    @TableField(value = "creator_type")
    private Integer creatorType;

    @Schema(description = "创建者ID")
    @TableField(value = "creator_id")
    private Long creatorId;

}