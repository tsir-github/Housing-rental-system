package com.atguigu.lease.web.app.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 关键词提取结果VO
 * 用于存储从用户消息中提取的搜索条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractionResult {

    /**
     * 省份ID列表（对应province_info.id）
     */
    private List<Long> provinceIds;

    /**
     * 城市ID列表（对应city_info.id）
     */
    private List<Long> cityIds;

    /**
     * 区域ID列表（对应district_info.id -> apartment_info.district_id）
     */
    private List<Long> districtIds;

    /**
     * 详细地址关键词（用于模糊匹配apartment_info.address_detail）
     */
    private String addressDetail;

    /**
     * 房间属性值ID列表（对应attr_value.id，通过room_attr_value关联）
     */
    private List<Long> attrValueIds;

    /**
     * 房间配套ID列表（对应facility_info.id where type=2，通过room_facility关联）
     */
    private List<Long> facilityIds;

    /**
     * 房间标签ID列表（对应label_info.id where type=2，通过room_label关联）
     */
    private List<Long> labelIds;

    /**
     * 价格范围（对应room_info.rent，支持范围查询）
     */
    private PriceRange priceRange;

    /**
     * 发布者类型（对应room_info.publisher_type: LANDLORD/OFFICIAL）
     */
    private String publisherType;

    /**
     * 入住时间
     */
    private LocalDate moveInDate;

    /**
     * 租期（月）
     */
    private Integer leaseDuration;

    /**
     * 原始文本
     */
    private String rawText;

    /**
     * 提取置信度（0-1）
     */
    private Double confidence;

    /**
     * 价格范围内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRange {
        /**
         * 最小价格（元/月）
         */
        private Integer min;
        
        /**
         * 最大价格（元/月）
         */
        private Integer max;
    }
}
