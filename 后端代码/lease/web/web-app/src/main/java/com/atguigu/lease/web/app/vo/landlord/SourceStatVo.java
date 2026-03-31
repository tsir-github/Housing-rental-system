package com.atguigu.lease.web.app.vo.landlord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 来源统计VO
 * 用于租约来源分布统计
 */
@Data
@Schema(description = "来源统计信息")
public class SourceStatVo {

    @Schema(description = "来源类型代码")
    private Integer sourceType;

    @Schema(description = "来源类型名称")
    private String sourceTypeName;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "百分比")
    private Double percentage;

    public SourceStatVo() {}

    public SourceStatVo(Integer sourceType, String sourceTypeName, Long count) {
        this.sourceType = sourceType;
        this.sourceTypeName = sourceTypeName;
        this.count = count;
    }

    public SourceStatVo(Integer sourceType, String sourceTypeName, Long count, Double percentage) {
        this.sourceType = sourceType;
        this.sourceTypeName = sourceTypeName;
        this.count = count;
        this.percentage = percentage;
    }
}