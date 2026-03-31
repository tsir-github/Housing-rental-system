package com.atguigu.lease.web.app.vo.landlord;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义属性请求VO
 * 用于房东创建自定义属性时的数据传输
 */
@Data
@Schema(description = "自定义属性请求")
public class CustomAttributeRequest {

    @Schema(description = "属性键名称", required = true)
    private String attrKeyName;

    @Schema(description = "属性值名称", required = true)
    private String attrValueName;

    @Schema(description = "房东ID", required = true)
    private Long landlordId;

    @Schema(description = "房源ID", required = false)
    private Long roomId;
}