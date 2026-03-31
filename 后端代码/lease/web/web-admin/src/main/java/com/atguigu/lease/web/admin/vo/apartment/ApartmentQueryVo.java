package com.atguigu.lease.web.admin.vo.apartment;

import com.atguigu.lease.model.enums.PublisherType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "公寓查询实体")
public class ApartmentQueryVo {

    @Schema(description = "省份id")
    private Long provinceId;

    @Schema(description = "城市id")
    private Long cityId;

    @Schema(description = "区域id")
    private Long districtId;

    @Schema(description = "发布者类型：1-官方，2-房东")
    private PublisherType publisherType;
}
