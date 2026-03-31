package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.DirectMoveInStatus;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "直接入住申请表")
@TableName(value = "direct_move_in_application")
@Data
public class DirectMoveInApplication extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "租客用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "房间ID")
    @TableField(value = "room_id")
    private Long roomId;

    @Schema(description = "公寓ID")
    @TableField(value = "apartment_id")
    private Long apartmentId;

    @Schema(description = "房东ID（如果是房东发布的房源）")
    @TableField(value = "landlord_id")
    private Long landlordId;

    @Schema(description = "租客姓名")
    @TableField(value = "name")
    private String name;

    @Schema(description = "租客手机号")
    @TableField(value = "phone")
    private String phone;

    @Schema(description = "租客身份证号")
    @TableField(value = "identification_number")
    private String identificationNumber;

    @Schema(description = "租客邮箱")
    @TableField(value = "email")
    private String email;

    @Schema(description = "紧急联系人")
    @TableField(value = "emergency_contact")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话")
    @TableField(value = "emergency_phone")
    private String emergencyPhone;

    @Schema(description = "租期开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField(value = "lease_start_date")
    private Date leaseStartDate;

    @Schema(description = "租期结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField(value = "lease_end_date")
    private Date leaseEndDate;

    @Schema(description = "租期ID")
    @TableField(value = "lease_term_id")
    private Long leaseTermId;

    @Schema(description = "月租金")
    @TableField(value = "rent")
    private BigDecimal rent;

    @Schema(description = "押金金额")
    @TableField(value = "deposit")
    private BigDecimal deposit;

    @Schema(description = "支付方式ID")
    @TableField(value = "payment_type_id")
    private Long paymentTypeId;

    @Schema(description = "申请状态")
    @TableField(value = "status")
    private DirectMoveInStatus status;

    @Schema(description = "处理人ID（房东或官方人员）")
    @TableField(value = "processor_id")
    private Long processorId;

    @Schema(description = "处理人类型：1-官方，2-房东")
    @TableField(value = "processor_type")
    private Integer processorType;

    @Schema(description = "拒绝原因")
    @TableField(value = "rejection_reason")
    private String rejectionReason;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "processed_at")
    private Date processedAt;

    @Schema(description = "备注信息")
    @TableField(value = "additional_info")
    private String additionalInfo;
}