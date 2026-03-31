package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.FacilityInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author liubo
* @description 针对表【facility_info(配套信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.atguigu.lease.model.entity.FacilityInfo
*/
public interface FacilityInfoMapper extends BaseMapper<FacilityInfo> {

    List<FacilityInfo> selectListByRoomId(Long id);

    List<FacilityInfo> selectListByApartmentId(Long id);

    /**
     * 查询房东可用的配套设施（官方+房东发布）
     * @param landlordId 房东ID
     * @return 配套设施列表
     */
    List<FacilityInfo> selectAvailableForLandlord(Long landlordId);

    /**
     * 查询公寓关联的配套设施
     * @param apartmentId 公寓ID
     * @return 配套设施列表
     */
    List<FacilityInfo> selectByApartmentId(Long apartmentId);
}




