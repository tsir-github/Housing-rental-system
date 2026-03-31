package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息中心实体
 */
@Schema(description = "消息中心")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("message_center")
public class MessageCenter implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "接收人ID")
    private Long recipientId;

    @Schema(description = "接收人类型：1-官方人员，2-房东，3-租客")
    private Integer recipientType;

    @Schema(description = "消息类型：SYSTEM-系统消息，BUSINESS-业务消息，NOTIFICATION-通知消息")
    private String messageType;

    @Schema(description = "消息分类：REVIEW-审核，APPLICATION-申请，APPOINTMENT-预约，LEASE-租约，PAYMENT-支付")
    private String messageCategory;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "关联实体类型")
    private String relatedEntityType;

    @Schema(description = "关联实体ID")
    private Long relatedEntityId;

    @Schema(description = "消息优先级：1-高，2-中，3-低")
    private Integer priority;

    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "阅读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @Schema(description = "是否需要处理：0-不需要，1-需要")
    private Integer needAction;

    @Schema(description = "处理状态：0-未处理，1-已处理")
    private Integer actionStatus;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionTime;

    @Schema(description = "扩展信息（JSON格式）")
    private String extendInfo;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    private Integer isDeleted;
}