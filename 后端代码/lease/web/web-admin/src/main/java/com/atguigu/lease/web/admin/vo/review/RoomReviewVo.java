package com.atguigu.lease.web.admin.vo.review;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.model.entity.ApartmentInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "房源审核信息")
public class RoomReviewVo extends RoomReview {

    @Schema(description = "房间信息")
    private RoomInfo roomInfo;

    @Schema(description = "公寓信息")
    private ApartmentInfo apartmentInfo;

    @Schema(description = "审核员姓名")
    private String reviewerName;

    @Schema(description = "房东姓名")
    private String landlordName;
}