package com.atguigu.lease.web.admin.vo.application;

import com.atguigu.lease.model.enums.DirectMoveInStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 直接入住申请详情VO
 * 包含关联信息的完整申请数据
 */
@Schema(description = "直接入住申请详情VO")
@Data
public class DirectMoveInApplicationDetailVO {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "租客用户ID")
    private Long userId;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "公寓ID")
    private Long apartmentId;

    @Schema(description = "房东ID")
    private Long landlordId;

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

    @Schema(description = "申请状态")
    private DirectMoveInStatus status;

    @Schema(description = "处理人ID")
    private Long processorId;

    @Schema(description = "处理人类型：1-官方，2-房东")
    private Integer processorType;

    @Schema(description = "拒绝原因")
    private String rejectionReason;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date processedAt;

    @Schema(description = "备注信息")
    private String additionalInfo;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // 关联信息
    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "公寓名称")
    private String apartmentName;

    @Schema(description = "租期名称")
    private String leaseTermName;

    @Schema(description = "支付方式名称")
    private String paymentTypeName;

    @Schema(description = "房东姓名")
    private String landlordName;

    @Schema(description = "房东手机号")
    private String landlordPhone;

    @Schema(description = "处理人姓名")
    private String processorName;

    @Schema(description = "房源发布者类型：1-官方，2-房东")
    private Integer publisherType;

    @Schema(description = "房源发布者类型名称")
    private String publisherTypeName;
}