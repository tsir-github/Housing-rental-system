package com.atguigu.lease.web.app.vo.room;


import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Schema(description = "APP房间列表实体")
@Data
public class RoomItemVo {

    @Schema(description = "房间id")
    private Long id;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "租金（元/月）")
    private BigDecimal rent;

    @Schema(description = "所属公寓id")
    private Long apartmentId;

    @Schema(description = "所属公寓名称")
    private String apartmentName;

    @Schema(description = "发布者类型")
    private PublisherType publisherType;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "是否发布")
    private ReleaseStatus isRelease;

    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;

    @Schema(description = "符合率评分")
    private BigDecimal complianceScore;

    @Schema(description = "综合评分")
    private BigDecimal overallRating;

    @Schema(description = "最后审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastReviewedAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Schema(description = "房间图片列表")
    private List<GraphVo> graphVoList;

    @Schema(description = "房间标签列表")
    private List<LabelInfo> labelInfoList;

    @Schema(description = "房间所属公寓信息")
    private ApartmentInfo apartmentInfo;

}
