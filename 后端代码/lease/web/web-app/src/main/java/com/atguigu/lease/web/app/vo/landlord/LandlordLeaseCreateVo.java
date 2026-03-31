package com.atguigu.lease.web.app.vo.landlord;

import com.atguigu.lease.model.enums.LeaseSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 房东创建租约VO
 * 用于房东创建新租约时的数据传输
 */
@Data
@Schema(description = "房东创建租约信息")
public class LandlordLeaseCreateVo {

    @Schema(description = "承租人手机号码")
    private String phone;

    @Schema(description = "承租人姓名")
    private String name;

    @Schema(description = "承租人身份证号码")
    private String identificationNumber;

    @Schema(description = "签约公寓id")
    private Long apartmentId;

    @Schema(description = "签约房间id")
    private Long roomId;

    @Schema(description = "租约开始日期")
    private Date leaseStartDate;

    @Schema(description = "租约结束日期")
    private Date leaseEndDate;

    @Schema(description = "租期id")
    private Long leaseTermId;

    @Schema(description = "租金（元/月）")
    private BigDecimal rent;

    @Schema(description = "押金（元）")
    private BigDecimal deposit;

    @Schema(description = "支付类型id")
    private Long paymentTypeId;

    @Schema(description = "租约来源")
    private LeaseSourceType sourceType;

    @Schema(description = "备注信息")
    private String additionalInfo;
}