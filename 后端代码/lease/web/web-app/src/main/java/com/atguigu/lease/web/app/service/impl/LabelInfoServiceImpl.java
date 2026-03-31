package com.atguigu.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.web.app.service.LabelInfoService;
import com.atguigu.lease.web.app.mapper.LabelInfoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【label_info(标签信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
@Slf4j
public class LabelInfoServiceImpl extends ServiceImpl<LabelInfoMapper, LabelInfo>
    implements LabelInfoService{

    @Override
    public List<LabelInfo> listByTypeAndLandlord(ItemType type, Long landlordId) {
        LambdaQueryWrapper<LabelInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LabelInfo::getType, type)
                   .eq(LabelInfo::getIsDeleted, 0)
                   .and(wrapper -> wrapper
                       .eq(LabelInfo::getCreatorType, CreatorType.OFFICIAL)
                       .or(subWrapper -> subWrapper
                           .eq(LabelInfo::getCreatorType, CreatorType.LANDLORD)
                           .eq(LabelInfo::getCreatorId, landlordId)
                       )
                   )
                   .orderByAsc(LabelInfo::getId);
        return list(queryWrapper);
    }

    @Override
    public List<LabelInfo> getAvailableLabelsForLandlord(Long landlordId) {
        try {
            // 使用更简单的查询逻辑：分别查询然后合并
            List<LabelInfo> labelList = new ArrayList<>();
            
            // 1. 查询系统标签 (creator_type = 0 AND type = ROOM)
            LambdaQueryWrapper<LabelInfo> systemWrapper = new LambdaQueryWrapper<>();
            systemWrapper.eq(LabelInfo::getIsDeleted, 0)
                        .eq(LabelInfo::getCreatorType, CreatorType.SYSTEM)
                        .eq(LabelInfo::getType, ItemType.ROOM)  // 只查询房间级别的标签
                        .orderByDesc(LabelInfo::getCreateTime);
            List<LabelInfo> systemLabels = this.list(systemWrapper);
            labelList.addAll(systemLabels);
            
            // 2. 查询官方标签 (creator_type = 1 AND type = ROOM)
            LambdaQueryWrapper<LabelInfo> officialWrapper = new LambdaQueryWrapper<>();
            officialWrapper.eq(LabelInfo::getIsDeleted, 0)
                          .eq(LabelInfo::getCreatorType, CreatorType.OFFICIAL)
                          .eq(LabelInfo::getType, ItemType.ROOM)  // 只查询房间级别的标签
                          .orderByDesc(LabelInfo::getCreateTime);
            List<LabelInfo> officialLabels = this.list(officialWrapper);
            labelList.addAll(officialLabels);
            
            // 3. 查询该房东的标签 (creator_type = 2 AND creator_id = landlordId AND type = ROOM)
            LambdaQueryWrapper<LabelInfo> landlordWrapper = new LambdaQueryWrapper<>();
            landlordWrapper.eq(LabelInfo::getIsDeleted, 0)
                          .eq(LabelInfo::getCreatorType, CreatorType.LANDLORD)
                          .eq(LabelInfo::getCreatorId, landlordId)
                          .eq(LabelInfo::getType, ItemType.ROOM)  // 只查询房间级别的标签
                          .orderByDesc(LabelInfo::getCreateTime);
            List<LabelInfo> landlordLabels = this.list(landlordWrapper);
            labelList.addAll(landlordLabels);
            
            log.info("查询到房间标签数量 - 系统: {}, 官方: {}, 房东: {}, 总计: {}", 
                systemLabels.size(), officialLabels.size(), landlordLabels.size(), labelList.size());
            
            return labelList.stream().map(label -> {
                LabelInfo vo = new LabelInfo();
                BeanUtils.copyProperties(label, vo);
                
                // 添加发布者类型标识
                if (CreatorType.SYSTEM.equals(label.getCreatorType()) || 
                    CreatorType.OFFICIAL.equals(label.getCreatorType())) {
                    vo.setName("[官方] " + label.getName());
                } else if (CreatorType.LANDLORD.equals(label.getCreatorType())) {
                    vo.setName("[我的] " + label.getName());
                }
                
                return vo;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取房东可选择的标签列表失败，房东ID: {}", landlordId, e);
            return new ArrayList<>();
        }
    }
}




