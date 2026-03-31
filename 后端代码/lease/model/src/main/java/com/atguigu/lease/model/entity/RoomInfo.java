package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.model.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Schema(description = "房间信息表")
@TableName(value = "room_info")
@Data
public class RoomInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "房间号")
    @TableField(value = "room_number")
    private String roomNumber;

    @Schema(description = "租金（元/月）")
    @TableField(value = "rent")
    private BigDecimal rent;

    @Schema(description = "所属公寓id")
    @TableField(value = "apartment_id")
    private Long apartmentId;

    @Schema(description = "发布者类型：1-官方，2-房东")
    @TableField(value = "publisher_type")
    private PublisherType publisherType;

    @Schema(description = "发布者ID（房东ID或官方人员ID）")
    @TableField(value = "publisher_id")
    private Long publisherId;

    @Schema(description = "是否发布")
    @TableField(value = "is_release")
    private ReleaseStatus isRelease;

    @Schema(description = "审核状态：1-待审核，2-审核通过，3-审核拒绝")
    @TableField(value = "review_status")
    private ReviewStatus reviewStatus;

    @Schema(description = "符合率评分")
    @TableField(value = "compliance_score")
    private BigDecimal complianceScore;

    @Schema(description = "最后审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "last_reviewed_at")
    private Date lastReviewedAt;

}