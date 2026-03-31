package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "公寓信息表")
@TableName(value = "apartment_info")
@Data
public class ApartmentInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公寓名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "公寓介绍")
    @TableField(value = "introduction")
    private String introduction;

    @Schema(description = "所处区域id")
    @TableField(value = "district_id")
    private Long districtId;

    @Schema(description = "所处区域名称")
    @TableField(value = "district_name")
    private String districtName;

    @Schema(description = "所处城市id")
    @TableField(value = "city_id")
    private Long cityId;

    @Schema(description = "所处城市名称")
    @TableField(value = "city_name")
    private String cityName;

    @Schema(description = "所处省份id")
    @TableField(value = "province_id")
    private Long provinceId;

    @Schema(description = "所处区域名称")
    @TableField(value = "province_name")
    private String provinceName;

    @Schema(description = "详细地址")
    @TableField(value = "address_detail")
    private String addressDetail;

    @Schema(description = "纬度")
    @TableField(value = "latitude")
    private String latitude;

    @Schema(description = "经度")
    @TableField(value = "longitude")
    private String longitude;

    @Schema(description = "公寓前台电话")
    @TableField(value = "phone")
    private String phone;

    @Schema(description = "房东ID（关联user_info表）")
    @TableField(value = "landlord_id")
    private Long landlordId;

    @Schema(description = "是否发布")
    @TableField(value = "is_release")
    private ReleaseStatus isRelease;

    /**
     * 获取发布者类型
     * @return 发布者类型：1-官方，2-房东
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public PublisherType getPublisherType() {
        return landlordId == null ? PublisherType.OFFICIAL : PublisherType.LANDLORD;
    }

}