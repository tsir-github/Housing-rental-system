package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【facility_info(配套信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface FacilityInfoService extends IService<FacilityInfo> {

    /**
     * 根据类型和房东ID获取配套设施列表（包含系统默认和房东创建的）
     */
    List<FacilityInfo> listByTypeAndLandlord(ItemType type, Long landlordId);

    /**
     * 获取房东可选择的配套设施列表（包括官方发布的和房东发布的）
     * @param landlordId 房东ID
     * @return 可选择的配套设施列表
     */
    List<FacilityInfo> getAvailableFacilitiesForLandlord(Long landlordId);
}
