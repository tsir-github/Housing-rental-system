package com.atguigu.lease.web.app.vo.application;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "直接入住申请提交VO")
@Data
public class DirectMoveInApplicationSubmitVO {

    @Schema(description = "租客用户ID")
    private Long userId;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "公寓ID")
    private Long apartmentId;

    @Schema(description = "租客姓名")
    private String name;

    @Schema(description = "租客手机号")
    private String phone;

    @Schema(description = "租客身份证号")
    private String identificationNumber;

    @Schema(description = "租客邮箱")
    private String email;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;

    @Schema(description = "租期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseStartDate;

    @Schema(description = "租期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseEndDate;

    @Schema(description = "租期ID")
    private Long leaseTermId;

    @Schema(description = "月租金")
    private BigDecimal rent;

    @Schema(description = "押金金额")
    private BigDecimal deposit;

    @Schema(description = "支付方式ID")
    private Long paymentTypeId;

    @Schema(description = "备注信息")
    private String additionalInfo;
}