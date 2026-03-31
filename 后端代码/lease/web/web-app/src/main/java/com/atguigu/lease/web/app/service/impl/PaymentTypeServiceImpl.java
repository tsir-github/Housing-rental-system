package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.PaymentType;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.web.app.mapper.PaymentTypeMapper;
import com.atguigu.lease.web.app.mapper.RoomInfoMapper;
import com.atguigu.lease.web.app.service.PaymentTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【payment_type(支付方式表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
@Slf4j
public class PaymentTypeServiceImpl extends ServiceImpl<PaymentTypeMapper, PaymentType>
    implements PaymentTypeService{


    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Override
    public List<PaymentType> listByRoomId(Long id) {

        return paymentTypeMapper.selectListByRoomId(id);
    }

    @Override
    public List<PaymentType> getAvailablePaymentTypesForLandlord(Long landlordId) {
        try {
            log.info("开始获取房东可选择的支付方式列表，房东ID: {}", landlordId);
            
            // 分别查询三种类型的支付方式
            List<PaymentType> allPaymentTypes = new ArrayList<>();
            
            // 1. 查询系统支付方式 (creator_type = 0)
            LambdaQueryWrapper<PaymentType> systemWrapper = new LambdaQueryWrapper<>();
            systemWrapper.eq(PaymentType::getIsDeleted, 0)
                        .eq(PaymentType::getCreatorType, 0)
                        .orderByDesc(PaymentType::getCreateTime);
            List<PaymentType> systemPaymentTypes = this.list(systemWrapper);
            log.info("查询到系统支付方式数量: {}", systemPaymentTypes.size());
            
            // 2. 查询官方支付方式 (creator_type = 1)
            LambdaQueryWrapper<PaymentType> officialWrapper = new LambdaQueryWrapper<>();
            officialWrapper.eq(PaymentType::getIsDeleted, 0)
                          .eq(PaymentType::getCreatorType, 1)
                          .orderByDesc(PaymentType::getCreateTime);
            List<PaymentType> officialPaymentTypes = this.list(officialWrapper);
            log.info("查询到官方支付方式数量: {}", officialPaymentTypes.size());
            
            // 3. 查询房东支付方式 (creator_type = 2 AND creator_id = landlordId)
            LambdaQueryWrapper<PaymentType> landlordWrapper = new LambdaQueryWrapper<>();
            landlordWrapper.eq(PaymentType::getIsDeleted, 0)
                          .eq(PaymentType::getCreatorType, 2)
                          .eq(PaymentType::getCreatorId, landlordId)
                          .orderByDesc(PaymentType::getCreateTime);
            List<PaymentType> landlordPaymentTypes = this.list(landlordWrapper);
            log.info("查询到房东支付方式数量: {}", landlordPaymentTypes.size());
            
            // 合并所有支付方式
            allPaymentTypes.addAll(systemPaymentTypes);
            allPaymentTypes.addAll(officialPaymentTypes);
            allPaymentTypes.addAll(landlordPaymentTypes);
            
            log.info("总共查询到支付方式数量: {}", allPaymentTypes.size());
            
            // 转换并添加发布者标识
            return allPaymentTypes.stream().map(paymentType -> {
                PaymentType vo = new PaymentType();
                BeanUtils.copyProperties(paymentType, vo);
                
                // 添加发布者类型标识
                if (Integer.valueOf(0).equals(paymentType.getCreatorType()) || 
                    Integer.valueOf(1).equals(paymentType.getCreatorType())) {
                    vo.setName("[官方] " + paymentType.getName());
                } else if (Integer.valueOf(2).equals(paymentType.getCreatorType())) {
                    vo.setName("[我的] " + paymentType.getName());
                }
                
                return vo;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取房东可选择的支付方式列表失败，房东ID: {}", landlordId, e);
            return new ArrayList<>();
        }
    }
}




