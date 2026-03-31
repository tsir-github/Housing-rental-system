package com.atguigu.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.model.entity.AttrValue;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.web.app.service.AttrKeyService;
import com.atguigu.lease.web.app.service.AttrValueService;
import com.atguigu.lease.web.app.mapper.AttrKeyMapper;
import com.atguigu.lease.web.app.mapper.AttrValueMapper;
import com.atguigu.lease.web.app.vo.attr.AttrKeyVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
@Slf4j
public class AttrKeyServiceImpl extends ServiceImpl<AttrKeyMapper, AttrKey>
    implements AttrKeyService{

    @Autowired
    private AttrValueService attrValueService;

    @Autowired
    private AttrValueMapper attrValueMapper;

    @Override
    public List<AttrKeyVo> listAttrInfo() {
        // 查询所有属性键
        List<AttrKey> attrKeyList = this.list();
        
        List<AttrKeyVo> result = new ArrayList<>();
        
        for (AttrKey attrKey : attrKeyList) {
            AttrKeyVo attrKeyVo = new AttrKeyVo();
            BeanUtils.copyProperties(attrKey, attrKeyVo);
            
            // 查询该属性键对应的所有属性值
            LambdaQueryWrapper<AttrValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrValue::getAttrKeyId, attrKey.getId());
            List<AttrValue> attrValueList = attrValueService.list(queryWrapper);
            
            attrKeyVo.setAttrValueList(attrValueList);
            result.add(attrKeyVo);
        }
        
        return result;
    }

    @Override
    public List<AttrKeyVo> getAvailableAttributesForLandlord(Long landlordId) {
        try {
            // 使用更简单的查询逻辑：分别查询然后合并
            List<AttrKey> attrKeyList = new ArrayList<>();
            
            // 1. 查询系统属性 (creator_type = 0)
            LambdaQueryWrapper<AttrKey> systemWrapper = new LambdaQueryWrapper<>();
            systemWrapper.eq(AttrKey::getIsDeleted, 0)
                        .eq(AttrKey::getCreatorType, 0)
                        .orderByDesc(AttrKey::getCreateTime);
            List<AttrKey> systemAttrs = this.list(systemWrapper);
            attrKeyList.addAll(systemAttrs);
            
            // 2. 查询官方属性 (creator_type = 1)
            LambdaQueryWrapper<AttrKey> officialWrapper = new LambdaQueryWrapper<>();
            officialWrapper.eq(AttrKey::getIsDeleted, 0)
                          .eq(AttrKey::getCreatorType, 1)
                          .orderByDesc(AttrKey::getCreateTime);
            List<AttrKey> officialAttrs = this.list(officialWrapper);
            attrKeyList.addAll(officialAttrs);
            
            // 3. 查询该房东的属性 (creator_type = 2 AND creator_id = landlordId)
            LambdaQueryWrapper<AttrKey> landlordWrapper = new LambdaQueryWrapper<>();
            landlordWrapper.eq(AttrKey::getIsDeleted, 0)
                          .eq(AttrKey::getCreatorType, 2)
                          .eq(AttrKey::getCreatorId, landlordId)
                          .orderByDesc(AttrKey::getCreateTime);
            List<AttrKey> landlordAttrs = this.list(landlordWrapper);
            attrKeyList.addAll(landlordAttrs);
            
            log.info("查询到属性数量 - 系统: {}, 官方: {}, 房东: {}, 总计: {}", 
                systemAttrs.size(), officialAttrs.size(), landlordAttrs.size(), attrKeyList.size());
            
            return attrKeyList.stream().map(attrKey -> {
                AttrKeyVo vo = new AttrKeyVo();
                BeanUtils.copyProperties(attrKey, vo);
                
                // 添加发布者类型标识
                if (Integer.valueOf(0).equals(attrKey.getCreatorType()) || 
                    Integer.valueOf(1).equals(attrKey.getCreatorType())) {
                    vo.setName("[官方] " + attrKey.getName());
                } else if (Integer.valueOf(2).equals(attrKey.getCreatorType())) {
                    vo.setName("[我的] " + attrKey.getName());
                }
                
                // 查询该属性的所有属性值
                LambdaQueryWrapper<AttrValue> valueWrapper = new LambdaQueryWrapper<>();
                valueWrapper.eq(AttrValue::getAttrKeyId, attrKey.getId())
                           .eq(AttrValue::getIsDeleted, 0)
                           .orderByAsc(AttrValue::getName);
                
                List<AttrValue> attrValueList = attrValueMapper.selectList(valueWrapper);
                vo.setAttrValueList(attrValueList);
                
                return vo;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取房东可选择的属性列表失败，房东ID: {}", landlordId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public AttrKeyVo createCustomAttribute(com.atguigu.lease.web.app.vo.landlord.CustomAttributeRequest request) {
        try {
            // 1. 检查属性键是否已存在（同一房东下的属性键名不能重复）
            LambdaQueryWrapper<AttrKey> keyCheckWrapper = new LambdaQueryWrapper<>();
            keyCheckWrapper.eq(AttrKey::getName, request.getAttrKeyName())
                          .eq(AttrKey::getCreatorType, 2)
                          .eq(AttrKey::getCreatorId, request.getLandlordId())
                          .eq(AttrKey::getIsDeleted, 0);
            
            AttrKey existingKey = this.getOne(keyCheckWrapper);
            AttrKey attrKey;
            
            if (existingKey != null) {
                // 如果属性键已存在，使用现有的
                attrKey = existingKey;
                log.info("使用现有属性键：{}", attrKey.getName());
            } else {
                // 2. 创建新的属性键
                attrKey = new AttrKey();
                attrKey.setName(request.getAttrKeyName());
                attrKey.setCreatorType(2); // 2表示房东
                attrKey.setCreatorId(request.getLandlordId());
                
                this.save(attrKey);
                log.info("创建新属性键成功：{}", attrKey.getName());
            }
            
            // 3. 检查属性值是否已存在
            LambdaQueryWrapper<AttrValue> valueCheckWrapper = new LambdaQueryWrapper<>();
            valueCheckWrapper.eq(AttrValue::getAttrKeyId, attrKey.getId())
                            .eq(AttrValue::getName, request.getAttrValueName())
                            .eq(AttrValue::getIsDeleted, 0);
            
            AttrValue existingValue = attrValueService.getOne(valueCheckWrapper);
            
            if (existingValue == null) {
                // 4. 创建新的属性值
                AttrValue attrValue = new AttrValue();
                attrValue.setName(request.getAttrValueName());
                attrValue.setAttrKeyId(attrKey.getId());
                
                attrValueService.save(attrValue);
                log.info("创建新属性值成功：{}", attrValue.getName());
            } else {
                log.info("使用现有属性值：{}", existingValue.getName());
            }
            
            // 5. 构建返回结果
            AttrKeyVo result = new AttrKeyVo();
            BeanUtils.copyProperties(attrKey, result);
            result.setName("[我的] " + attrKey.getName());
            
            // 查询该属性的所有属性值
            LambdaQueryWrapper<AttrValue> valueWrapper = new LambdaQueryWrapper<>();
            valueWrapper.eq(AttrValue::getAttrKeyId, attrKey.getId())
                       .eq(AttrValue::getIsDeleted, 0)
                       .orderByAsc(AttrValue::getName);
            
            List<AttrValue> attrValueList = attrValueMapper.selectList(valueWrapper);
            result.setAttrValueList(attrValueList);
            
            return result;
            
        } catch (Exception e) {
            log.error("创建自定义属性失败，房东ID: {}, 属性名: {}, 属性值: {}", 
                request.getLandlordId(), request.getAttrKeyName(), request.getAttrValueName(), e);
            throw new RuntimeException("创建自定义属性失败：" + e.getMessage());
        }
    }
}




