package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @TableName lease_term
 */
@TableName(value = "lease_term")
@Data
@Schema(description = "租期信息")
public class LeaseTerm extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "租期名称")
    @TableField("name")
    private String name;

    @Schema(description = "租期月数")
    @TableField("month_count")
    private Integer monthCount;

    @Schema(description = "租期单位:月")
    @TableField("unit")
    private String unit;

    @Schema(description = "租期描述")
    @TableField("description")
    private String description;

    @Schema(description = "创建者类型：1-官方，2-房东")
    @TableField(value = "creator_type")
    private Integer creatorType;

    @Schema(description = "创建者ID")
    @TableField(value = "creator_id")
    private Long creatorId;
}