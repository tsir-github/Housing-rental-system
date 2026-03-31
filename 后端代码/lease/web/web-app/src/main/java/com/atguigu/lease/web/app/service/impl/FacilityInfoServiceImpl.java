package com.atguigu.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.web.app.service.FacilityInfoService;
import com.atguigu.lease.web.app.mapper.FacilityInfoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【facility_info(配套信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
@Slf4j
public class FacilityInfoServiceImpl extends ServiceImpl<FacilityInfoMapper, FacilityInfo>
    implements FacilityInfoService{

    @Override
    public List<FacilityInfo> listByTypeAndLandlord(ItemType type, Long landlordId) {
        LambdaQueryWrapper<FacilityInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FacilityInfo::getType, type)
                   .eq(FacilityInfo::getIsDeleted, 0)
                   .and(wrapper -> wrapper
                       .eq(FacilityInfo::getCreatorType, CreatorType.OFFICIAL)
                       .or(subWrapper -> subWrapper
                           .eq(FacilityInfo::getCreatorType, CreatorType.LANDLORD)
                           .eq(FacilityInfo::getCreatorId, landlordId)
                       )
                   )
                   .orderByAsc(FacilityInfo::getId);
        return list(queryWrapper);
    }

    @Override
    public List<FacilityInfo> getAvailableFacilitiesForLandlord(Long landlordId) {
        try {
            // 使用更简单的查询逻辑：分别查询然后合并
            List<FacilityInfo> facilityList = new ArrayList<>();
            
            // 1. 查询系统配套设施 (creator_type = 0 AND type = ROOM)
            LambdaQueryWrapper<FacilityInfo> systemWrapper = new LambdaQueryWrapper<>();
            systemWrapper.eq(FacilityInfo::getIsDeleted, 0)
                        .eq(FacilityInfo::getCreatorType, CreatorType.SYSTEM)
                        .eq(FacilityInfo::getType, ItemType.ROOM)  // 只查询房间级别的设施
                        .orderByDesc(FacilityInfo::getCreateTime);
            List<FacilityInfo> systemFacilities = this.list(systemWrapper);
            facilityList.addAll(systemFacilities);
            
            // 2. 查询官方配套设施 (creator_type = 1 AND type = ROOM)
            LambdaQueryWrapper<FacilityInfo> officialWrapper = new LambdaQueryWrapper<>();
            officialWrapper.eq(FacilityInfo::getIsDeleted, 0)
                          .eq(FacilityInfo::getCreatorType, CreatorType.OFFICIAL)
                          .eq(FacilityInfo::getType, ItemType.ROOM)  // 只查询房间级别的设施
                          .orderByDesc(FacilityInfo::getCreateTime);
            List<FacilityInfo> officialFacilities = this.list(officialWrapper);
            facilityList.addAll(officialFacilities);
            
            // 3. 查询该房东的配套设施 (creator_type = 2 AND creator_id = landlordId AND type = ROOM)
            LambdaQueryWrapper<FacilityInfo> landlordWrapper = new LambdaQueryWrapper<>();
            landlordWrapper.eq(FacilityInfo::getIsDeleted, 0)
                          .eq(FacilityInfo::getCreatorType, CreatorType.LANDLORD)
                          .eq(FacilityInfo::getCreatorId, landlordId)
                          .eq(FacilityInfo::getType, ItemType.ROOM)  // 只查询房间级别的设施
                          .orderByDesc(FacilityInfo::getCreateTime);
            List<FacilityInfo> landlordFacilities = this.list(landlordWrapper);
            facilityList.addAll(landlordFacilities);
            
            log.info("查询到房间配套设施数量 - 系统: {}, 官方: {}, 房东: {}, 总计: {}", 
                systemFacilities.size(), officialFacilities.size(), landlordFacilities.size(), facilityList.size());
            
            return facilityList.stream().map(facility -> {
                FacilityInfo vo = new FacilityInfo();
                BeanUtils.copyProperties(facility, vo);
                
                // 添加发布者类型标识
                if (CreatorType.SYSTEM.equals(facility.getCreatorType()) || 
                    CreatorType.OFFICIAL.equals(facility.getCreatorType())) {
                    vo.setName("[官方] " + facility.getName());
                } else if (CreatorType.LANDLORD.equals(facility.getCreatorType())) {
                    vo.setName("[我的] " + facility.getName());
                }
                
                return vo;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取房东可选择的配套设施列表失败，房东ID: {}", landlordId, e);
            return new ArrayList<>();
        }
    }
}




