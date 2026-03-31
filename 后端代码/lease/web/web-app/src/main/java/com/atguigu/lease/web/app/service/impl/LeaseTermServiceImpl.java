package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.LeaseTerm;
import com.atguigu.lease.web.app.mapper.LeaseTermMapper;
import com.atguigu.lease.web.app.service.LeaseTermService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;

/**
 * @author liubo
 * @description 针对表【lease_term(租期)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class LeaseTermServiceImpl extends ServiceImpl<LeaseTermMapper, LeaseTerm>
        implements LeaseTermService {

    @Autowired
    private LeaseTermMapper leaseTermMapper;
    
    @Override
    public List<LeaseTerm> listByRoomId(Long id) {
        return leaseTermMapper.selectListByRoomId(id);
    }

    @Override
    public List<LeaseTerm> getAvailableLeaseTermsForLandlord(Long landlordId) {
        try {
            log.info("开始获取房东可选择的租期列表，房东ID: {}", landlordId);
            
            // 分别查询三种类型的租期
            List<LeaseTerm> allLeaseTerms = new ArrayList<>();
            
            // 1. 查询系统租期 (creator_type = 0)
            LambdaQueryWrapper<LeaseTerm> systemWrapper = new LambdaQueryWrapper<>();
            systemWrapper.eq(LeaseTerm::getIsDeleted, 0)
                        .eq(LeaseTerm::getCreatorType, 0)
                        .orderByAsc(LeaseTerm::getMonthCount);
            List<LeaseTerm> systemLeaseTerms = this.list(systemWrapper);
            log.info("查询到系统租期数量: {}", systemLeaseTerms.size());
            
            // 2. 查询官方租期 (creator_type = 1)
            LambdaQueryWrapper<LeaseTerm> officialWrapper = new LambdaQueryWrapper<>();
            officialWrapper.eq(LeaseTerm::getIsDeleted, 0)
                          .eq(LeaseTerm::getCreatorType, 1)
                          .orderByAsc(LeaseTerm::getMonthCount);
            List<LeaseTerm> officialLeaseTerms = this.list(officialWrapper);
            log.info("查询到官方租期数量: {}", officialLeaseTerms.size());
            
            // 3. 查询房东租期 (creator_type = 2 AND creator_id = landlordId)
            LambdaQueryWrapper<LeaseTerm> landlordWrapper = new LambdaQueryWrapper<>();
            landlordWrapper.eq(LeaseTerm::getIsDeleted, 0)
                          .eq(LeaseTerm::getCreatorType, 2)
                          .eq(LeaseTerm::getCreatorId, landlordId)
                          .orderByAsc(LeaseTerm::getMonthCount);
            List<LeaseTerm> landlordLeaseTerms = this.list(landlordWrapper);
            log.info("查询到房东租期数量: {}", landlordLeaseTerms.size());
            
            // 合并所有租期
            allLeaseTerms.addAll(systemLeaseTerms);
            allLeaseTerms.addAll(officialLeaseTerms);
            allLeaseTerms.addAll(landlordLeaseTerms);
            
            log.info("总共查询到租期数量: {}", allLeaseTerms.size());
            
            return allLeaseTerms;
            
        } catch (Exception e) {
            log.error("获取房东可选择的租期列表失败，房东ID: {}", landlordId, e);
            return new ArrayList<>();
        }
    }
}




