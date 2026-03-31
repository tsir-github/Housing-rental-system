package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.LabelInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author liubo
* @description 针对表【label_info(标签信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.atguigu.lease.model.entity.LabelInfo
*/
public interface LabelInfoMapper extends BaseMapper<LabelInfo> {

    List<LabelInfo> selectListByRoomId(Long id);

    List<LabelInfo> selectListByApartmentId(Long id);

    /**
     * 查询房东可用的标签（官方+房东发布）
     * @param landlordId 房东ID
     * @return 标签列表
     */
    List<LabelInfo> selectAvailableForLandlord(Long landlordId);

    /**
     * 查询公寓关联的标签
     * @param apartmentId 公寓ID
     * @return 标签列表
     */
    List<LabelInfo> selectByApartmentId(Long apartmentId);
}




