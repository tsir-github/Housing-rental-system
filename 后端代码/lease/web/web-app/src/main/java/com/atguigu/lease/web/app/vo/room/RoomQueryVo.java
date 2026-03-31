package com.atguigu.lease.web.app.vo.room;

import com.atguigu.lease.model.enums.PublisherType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "房间查询实体")
public class RoomQueryVo {

    @Schema(description = "所属公寓ID")
    private Long apartmentId;

    @Schema(description = "省份Id")
    private Long provinceId;

    @Schema(description = "城市Id")
    private Long cityId;

    @Schema(description = "区域Id")
    private Long districtId;

    @Schema(description = "最小租金")
    private BigDecimal minRent;

    @Schema(description = "最大租金")
    private BigDecimal maxRent;

    @Schema(description = "支付方式")
    private Long paymentTypeId;

    @Schema(description = "价格排序方式", allowableValues = {"desc", "asc"})
    private String orderType;

    @Schema(description = "发布者类型")
    private PublisherType publisherType;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布状态")
    private Integer isRelease;

    @Schema(description = "审核状态")
    private Integer reviewStatus;

}
