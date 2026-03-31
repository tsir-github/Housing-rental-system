package com.atguigu.lease.web.admin.vo.apartment;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.enums.PublisherType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "后台管理系统公寓列表实体")
public class ApartmentItemVo extends ApartmentInfo {

    @Schema(description = "房间总数")
    private Long totalRoomCount;

    @Schema(description = "空闲房间数")
    private Long freeRoomCount;

    @Schema(description = "发布者类型：1-官方，2-房东")
    private PublisherType publisherType;

}
