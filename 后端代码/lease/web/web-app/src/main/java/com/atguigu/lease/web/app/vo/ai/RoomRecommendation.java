package com.atguigu.lease.web.app.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房源推荐VO
 * 用于向前端展示推荐的房源信息
 * 所有字段都来自现有的数据库表，不需要新增字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRecommendation {

    // ==================== 基础房源信息（来自room_info表） ====================

    /**
     * 房源ID（room_info.id）
     */
    private Long roomId;

    /**
     * 房间号（room_info.room_number）
     */
    private String roomNumber;

    /**
     * 房间图片URL列表（来自room_picture表）
     */
    private List<String> imageUrls;

    /**
     * 租金（元/月，room_info.rent）
     */
    private BigDecimal rent;

    /**
     * 审核状态（room_info.review_status）
     */
    private String reviewStatus;

    // ==================== 公寓信息（来自apartment_info表） ====================

    /**
     * 公寓ID（apartment_info.id）
     */
    private Long apartmentId;

    /**
     * 公寓名称（apartment_info.name）
     */
    private String apartmentName;

    /**
     * 位置信息（组合：province_name + city_name + district_name + address_detail）
     */
    private String location;

    /**
     * 详细地址（apartment_info.address_detail）
     */
    private String addressDetail;

    /**
     * 纬度（apartment_info.latitude）
     */
    private String latitude;

    /**
     * 经度（apartment_info.longitude）
     */
    private String longitude;

    // ==================== 房源属性（来自room_attr_value和attr_value表） ====================

    /**
     * 房间属性列表（如"两室一厅"、"朝南"等）
     */
    private List<String> roomAttributes;

    // ==================== 房源设施（来自room_facility和facility_info表） ====================

    /**
     * 房间设施列表
     */
    private List<String> facilities;

    // ==================== 推荐相关信息 ====================

    /**
     * 匹配度评分（基于关键词匹配度计算，0-1）
     */
    private Double matchScore;

    /**
     * 详情页链接（前端路由，格式：/room/{roomId}）
     */
    private String detailPageUrl;
}
