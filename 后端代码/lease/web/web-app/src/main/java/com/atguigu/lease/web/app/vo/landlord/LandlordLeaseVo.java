package com.atguigu.lease.web.app.vo.landlord;

import com.atguigu.lease.model.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 房东租约信息VO
 * 用于房东查看租约列表和详情
 */
@Data
@Schema(description = "房东租约信息")
public class LandlordLeaseVo extends LeaseAgreement {

    @Schema(description = "签约公寓信息")
    private ApartmentInfo apartmentInfo;

    @Schema(description = "签约房间信息")
    private RoomInfo roomInfo;

    @Schema(description = "支付方式")
    private PaymentType paymentType;

    @Schema(description = "租期")
    private LeaseTerm leaseTerm;

    @Schema(description = "剩余天数")
    private Integer remainingDays;
}