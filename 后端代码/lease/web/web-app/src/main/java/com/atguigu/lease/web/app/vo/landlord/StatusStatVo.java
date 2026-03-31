package com.atguigu.lease.web.app.vo.landlord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 状态统计VO
 * 用于租约状态分布统计
 */
@Data
@Schema(description = "状态统计信息")
public class StatusStatVo {

    @Schema(description = "状态代码")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "百分比")
    private Double percentage;

    public StatusStatVo() {}

    public StatusStatVo(Integer status, String statusName, Long count) {
        this.status = status;
        this.statusName = statusName;
        this.count = count;
    }

    public StatusStatVo(Integer status, String statusName, Long count, Double percentage) {
        this.status = status;
        this.statusName = statusName;
        this.count = count;
        this.percentage = percentage;
    }
}