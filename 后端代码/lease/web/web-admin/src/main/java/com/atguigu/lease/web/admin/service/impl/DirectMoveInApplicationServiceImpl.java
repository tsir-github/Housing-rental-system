package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.DirectMoveInStatus;
import com.atguigu.lease.model.enums.LeaseSourceType;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.mapper.DirectMoveInApplicationMapper;
import com.atguigu.lease.web.admin.mapper.RoomInfoMapper;
import com.atguigu.lease.web.admin.service.DirectMoveInApplicationService;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.atguigu.lease.web.admin.vo.application.DirectMoveInApplicationDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author system
 * @description 直接入住申请服务实现类
 * @createDate 2024-01-05 00:00:00
 */
@Slf4j
@Service
public class DirectMoveInApplicationServiceImpl extends ServiceImpl<DirectMoveInApplicationMapper, DirectMoveInApplication>
        implements DirectMoveInApplicationService {

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Override
    public IPage<DirectMoveInApplicationDetailVO> getPendingApplicationsWithDetails(
            Page<DirectMoveInApplicationDetailVO> page,
            Long processorId, Integer processorType, Integer publisherType) {
        
        log.info("查询待处理申请列表 - processorId: {}, processorType: {}, publisherType: {}", 
                processorId, processorType, publisherType);
        
        return baseMapper.selectPendingApplicationsWithDetails(page, processorId, processorType, publisherType);
    }

    @Override
    public IPage<DirectMoveInApplicationDetailVO> getApplicationHistoryWithDetails(
            Page<DirectMoveInApplicationDetailVO> page,
            Long userId, Integer status, Long processorId, Integer processorType, Integer publisherType) {
        
        log.info("查询申请历史 - userId: {}, status: {}, processorId: {}, processorType: {}, publisherType: {}", 
                userId, status, processorId, processorType, publisherType);
        
        return baseMapper.selectApplicationHistoryWithDetails(page, userId, status, processorId, processorType, publisherType);
    }

    @Override
    public DirectMoveInApplicationDetailVO getApplicationDetailById(Long id) {
        log.info("查询申请详情 - id: {}", id);
        return baseMapper.selectApplicationDetailById(id);
    }

    @Override
    @Transactional
    public void processApplication(Long applicationId, Long processorId, Integer processorType, 
                                 boolean approved, String rejectionReason) {
        DirectMoveInApplication application = this.getById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        if (application.getStatus() != DirectMoveInStatus.PENDING) {
            throw new RuntimeException("申请已处理，无法重复处理");
        }

        // 更新申请状态
        application.setStatus(approved ? DirectMoveInStatus.APPROVED : DirectMoveInStatus.REJECTED);
        application.setProcessorId(processorId);
        application.setProcessorType(processorType);
        application.setProcessedAt(new Date());
        
        if (!approved && rejectionReason != null) {
            application.setRejectionReason(rejectionReason);
        }

        this.updateById(application);

        // 如果申请通过，自动生成租约合同
        if (approved) {
            try {
                generateLeaseAgreement(applicationId);
                log.info("申请{}通过，已自动生成租约合同", applicationId);
            } catch (Exception e) {
                log.error("生成租约合同失败", e);
                // 这里可以选择回滚申请状态或者记录错误日志
                throw new RuntimeException("生成租约合同失败：" + e.getMessage());
            }
        }

        log.info("处理直接入住申请完成 - 申请ID:{}, 处理结果:{}", applicationId, approved ? "通过" : "拒绝");
    }

    @Override
    @Transactional
    public Long generateLeaseAgreement(Long applicationId) {
        DirectMoveInApplication application = this.getById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        if (application.getStatus() != DirectMoveInStatus.APPROVED) {
            throw new RuntimeException("申请未通过，无法生成租约");
        }

        // 检查房间是否可用
        if (!isRoomAvailable(application.getRoomId())) {
            throw new RuntimeException("房间不可用，无法生成租约");
        }

        // 创建租约合同
        LeaseAgreement leaseAgreement = new LeaseAgreement();
        
        // 复制申请信息到租约
        leaseAgreement.setPhone(application.getPhone());
        leaseAgreement.setName(application.getName());
        leaseAgreement.setIdentificationNumber(application.getIdentificationNumber());
        leaseAgreement.setApartmentId(application.getApartmentId());
        leaseAgreement.setRoomId(application.getRoomId());
        leaseAgreement.setLeaseStartDate(application.getLeaseStartDate());
        leaseAgreement.setLeaseEndDate(application.getLeaseEndDate());
        leaseAgreement.setLeaseTermId(application.getLeaseTermId());
        leaseAgreement.setRent(application.getRent());
        leaseAgreement.setDeposit(application.getDeposit());
        leaseAgreement.setPaymentTypeId(application.getPaymentTypeId());
        
        // 设置租约状态和来源
        leaseAgreement.setStatus(LeaseStatus.SIGNING); // 1 - 签约待确认
        leaseAgreement.setSourceType(LeaseSourceType.DIRECT_APPLICATION); // 3 - 直接申请
        leaseAgreement.setCreatorType(application.getProcessorType()); // 处理人类型
        
        // 设置创建者ID：如果是官方人员且获取ID复杂，使用默认值1
        if (application.getProcessorType() == 1) {
            // 官方人员：使用处理人ID，如果为空则使用默认值1
            leaseAgreement.setCreatorId(application.getProcessorId() != null ? application.getProcessorId() : 1L);
        } else {
            // 房东：使用处理人ID
            leaseAgreement.setCreatorId(application.getProcessorId());
        }
        
        leaseAgreement.setApplicationId(applicationId);
        leaseAgreement.setAutoGenerated(1); // 自动生成
        leaseAgreement.setAdditionalInfo("由直接入住申请自动生成");

        // 保存租约
        leaseAgreementService.save(leaseAgreement);

        log.info("成功生成租约合同 - 申请ID:{}, 租约ID:{}, 状态:签约待确认", applicationId, leaseAgreement.getId());
        return leaseAgreement.getId();
    }

    @Override
    public boolean isRoomAvailable(Long roomId) {
        // 检查房间是否存在且已发布
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null || roomInfo.getIsRelease() == null) {
            return false;
        }

        // 检查是否有有效的租约
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getRoomId, roomId)
                   .in(LeaseAgreement::getStatus, LeaseStatus.SIGNING, LeaseStatus.SIGNED)
                   .eq(LeaseAgreement::getIsDeleted, 0);
        
        long activeLeaseCount = leaseAgreementService.count(queryWrapper);
        
        return activeLeaseCount == 0;
    }
}