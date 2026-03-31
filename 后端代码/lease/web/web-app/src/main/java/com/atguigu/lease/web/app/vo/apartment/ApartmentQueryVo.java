package com.atguigu.lease.web.app.vo.apartment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 房东公寓查询条件VO
 */
@Schema(description = "房东公寓查询条件")
@Data
public class ApartmentQueryVo {

    @Schema(description = "公寓名称")
    private String name;

    @Schema(description = "省份ID")
    private Long provinceId;

    @Schema(description = "城市ID")
    private Long cityId;

    @Schema(description = "区县ID")
    private Long districtId;

    @Schema(description = "发布状态")
    private Integer isRelease;

    @Schema(description = "房东ID")
    private Long landlordId;

}