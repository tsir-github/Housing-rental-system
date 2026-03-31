package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.atguigu.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.atguigu.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Slf4j
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Override
    public IPage<AgreementVo> pageAgreementByQuery(Page<AgreementVo> page, AgreementQueryVo queryVo) {
        return leaseAgreementMapper.pageAgreementByQuery(page, queryVo);
    }

    @Override
    public AgreementVo getAgreementById(Long id) {
        //租约基础信息
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        //根据租约基础信息的签约公寓id，获取公寓的信息；
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());

        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());

        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());

        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());

        AgreementVo agreementVo = new AgreementVo();
        //agreementVo.setPhone(leaseAgreement.getPhone());
        BeanUtils.copyProperties(leaseAgreement,agreementVo);
        agreementVo.setApartmentInfo(apartmentInfo);
        agreementVo.setRoomInfo(roomInfo);
        agreementVo.setPaymentType(paymentType);
        agreementVo.setLeaseTerm(leaseTerm);

        return agreementVo;
    }

    @Override
    public IPage<AgreementVo> getAllLeases(Page<AgreementVo> page, AgreementQueryVo queryVo) {
        // 官方后台统一查看所有租约，无论是官方还是房东生成的
        // 这里应该调用专门的查询方法，支持更多筛选条件
        return leaseAgreementMapper.getAllLeases(page, queryVo);
    }

    @Override
    @Transactional
    public void updateLeaseStatus(Long leaseId, String newStatus, String reason) {
        LeaseAgreement lease = getById(leaseId);
        if (lease == null) {
            throw new RuntimeException("租约不存在");
        }
        
        String oldStatus = lease.getStatus() != null ? lease.getStatus().name() : null;
        LeaseStatus oldLeaseStatus = lease.getStatus();
        LeaseStatus newLeaseStatus = LeaseStatus.valueOf(newStatus);
        
        // 如果是从"续约待确认"改为"已签约"，需要计算新的结束日期并更新
        if (oldLeaseStatus == LeaseStatus.RENEWING && newLeaseStatus == LeaseStatus.SIGNED) {
            log.info("检测到续约确认操作，将自动延长租约日期");
            
            // 查询租期信息
            LeaseTerm leaseTerm = leaseTermMapper.selectById(lease.getLeaseTermId());
            if (leaseTerm == null) {
                throw new RuntimeException("租期信息不存在");
            }
            
            // 从原结束日期开始延长
            Date oldEndDate = lease.getLeaseEndDate();
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(oldEndDate);
            calendar.add(java.util.Calendar.MONTH, leaseTerm.getMonthCount());
            Date newEndDate = calendar.getTime();
            
            log.info("续约日期计算：原结束日期={}, 续约月数={}, 新结束日期={}", 
                oldEndDate, leaseTerm.getMonthCount(), newEndDate);
            
            // 更新租约：新的结束日期和状态
            lease.setLeaseEndDate(newEndDate);
        }
        
        // 更新租约状态
        lease.setStatus(newLeaseStatus);
        lease.setUpdateTime(new Date());
        updateById(lease);
        
        // 记录状态变更日志
        recordLeaseStatusChange(leaseId, oldStatus, newStatus, reason);
        
        // 发送状态变更通知
        sendStatusChangeNotification(lease, oldStatus, newStatus);
        
        log.info("租约状态更新成功，租约ID: {}, 原状态: {}, 新状态: {}, 原因: {}", 
                leaseId, oldStatus, newStatus, reason);
    }

    @Override
    public List<LeaseAgreement> checkExpiringLeases() {
        LocalDate now = LocalDate.now();
        
        // 查询即将到期的租约
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getStatus, LeaseStatus.SIGNED)
                   .le(LeaseAgreement::getLeaseEndDate, Date.from(now.plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        return list(queryWrapper);
    }

    @Override
    public void sendExpirationNotification(LeaseAgreement lease) {
        try {
            // 计算剩余天数
            LocalDate endDate = lease.getLeaseEndDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate now = LocalDate.now();
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, endDate);
            
            // 根据租期长短决定提醒时机
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                    lease.getLeaseStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    endDate
            );
            
            // 动态计算提醒时机
            long reminderThreshold;
            if (totalDays <= 2) {
                // 租期2天以内，剩余6小时提醒
                reminderThreshold = 0; // 当天提醒
            } else if (totalDays <= 30) {
                // 租期30天以内，提前3天提醒
                reminderThreshold = 3;
            } else {
                // 租期超过30天，提前30天提醒
                reminderThreshold = 30;
            }
            
            if (daysRemaining <= reminderThreshold) {
                // 发送提醒通知（这里可以集成短信、邮件等通知服务）
                log.info("发送租约到期提醒，租约ID: {}, 承租人: {}, 剩余天数: {}", 
                        lease.getId(), lease.getName(), daysRemaining);
                
                // TODO: 集成实际的通知服务
                // notificationService.sendExpirationReminder(lease, daysRemaining);
            }
        } catch (Exception e) {
            log.error("发送租约到期提醒失败，租约ID: {}", lease.getId(), e);
        }
    }

    @Override
    public void recordLeaseStatusChange(Long leaseId, String oldStatus, String newStatus, String reason) {
        try {
            // 记录状态变更日志（可以存储到专门的日志表中）
            log.info("租约状态变更记录 - 租约ID: {}, 原状态: {}, 新状态: {}, 变更原因: {}, 变更时间: {}", 
                    leaseId, oldStatus, newStatus, reason, LocalDateTime.now());
            
            // TODO: 如果需要持久化日志，可以创建专门的日志表
            // LeaseStatusLog statusLog = new LeaseStatusLog();
            // statusLog.setLeaseId(leaseId);
            // statusLog.setOldStatus(oldStatus);
            // statusLog.setNewStatus(newStatus);
            // statusLog.setReason(reason);
            // statusLog.setChangeTime(LocalDateTime.now());
            // leaseStatusLogMapper.insert(statusLog);
        } catch (Exception e) {
            log.error("记录租约状态变更日志失败，租约ID: {}", leaseId, e);
        }
    }

    @Override
    public Object getLeaseStatistics() {
        try {
            // 统计各种状态的租约数量
            LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
            
            // 总租约数
            long totalCount = count();
            
            // 已签署租约数
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getStatus, LeaseStatus.SIGNED);
            long signedCount = count(queryWrapper);
            
            // 待签署租约数
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getStatus, LeaseStatus.SIGNING);
            long signingCount = count(queryWrapper);
            
            // 已取消租约数
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getStatus, LeaseStatus.CANCELED);
            long canceledCount = count(queryWrapper);
            
            // 官方创建的租约数
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getCreatorType, 1);
            long officialCount = count(queryWrapper);
            
            // 房东创建的租约数
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getCreatorType, 2);
            long landlordCount = count(queryWrapper);
            
            // 按来源统计租约数量
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getSourceType, com.atguigu.lease.model.enums.LeaseSourceType.NEW);
            long newLeaseCount = count(queryWrapper);
            
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getSourceType, com.atguigu.lease.model.enums.LeaseSourceType.RENEW);
            long renewLeaseCount = count(queryWrapper);
            
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getSourceType, com.atguigu.lease.model.enums.LeaseSourceType.DIRECT_APPLICATION);
            long directApplicationCount = count(queryWrapper);
            
            // 自动生成的租约数量
            queryWrapper.clear();
            queryWrapper.eq(LeaseAgreement::getAutoGenerated, 1);
            long autoGeneratedCount = count(queryWrapper);
            
            // 构建统计结果
            return new Object() {
                public final long total = totalCount;
                public final long signed = signedCount;
                public final long signing = signingCount;
                public final long canceled = canceledCount;
                public final long official = officialCount;
                public final long landlord = landlordCount;
                public final long newLease = newLeaseCount;
                public final long renewLease = renewLeaseCount;
                public final long directApplication = directApplicationCount;
                public final long autoGenerated = autoGeneratedCount;
            };
        } catch (Exception e) {
            log.error("获取租约统计数据失败", e);
            throw new RuntimeException("获取统计数据失败");
        }
    }

    @Override
    @Transactional
    public void updateAndResendLease(Long leaseId, LeaseAgreement updatedLease) {
        try {
            LeaseAgreement existingLease = getById(leaseId);
            if (existingLease == null) {
                throw new RuntimeException("租约不存在");
            }
            
            // 更新租约信息
            updatedLease.setId(leaseId);
            updatedLease.setUpdateTime(new Date());
            updateById(updatedLease);
            
            // 重新发送给租客
            sendLeaseToTenant(updatedLease);
            
            log.info("租约修改并重新发送成功，租约ID: {}", leaseId);
        } catch (Exception e) {
            log.error("租约修改并重新发送失败，租约ID: {}", leaseId, e);
            throw new RuntimeException("租约修改失败");
        }
    }

    @Override
    @Transactional
    public void terminateLease(Long leaseId, String terminationReason) {
        try {
            LeaseAgreement lease = getById(leaseId);
            if (lease == null) {
                throw new RuntimeException("租约不存在");
            }
            
            // 更新租约状态为已退租
            lease.setStatus(LeaseStatus.CANCELED);
            lease.setAdditionalInfo("退租原因: " + terminationReason);
            lease.setUpdateTime(new Date());
            updateById(lease);
            
            // 记录终止日志
            recordLeaseStatusChange(leaseId, lease.getStatus().name(), 
                    LeaseStatus.CANCELED.name(), "租约终止: " + terminationReason);
            
            // 发送终止通知
            sendTerminationNotification(lease, terminationReason);
            
            log.info("租约终止成功，租约ID: {}, 终止原因: {}", leaseId, terminationReason);
        } catch (Exception e) {
            log.error("租约终止失败，租约ID: {}", leaseId, e);
            throw new RuntimeException("租约终止失败");
        }
    }

    /**
     * 定时任务：检查即将到期的租约
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天9点执行
    public void checkLeaseExpirationTask() {
        try {
            List<LeaseAgreement> expiringLeases = checkExpiringLeases();
            
            for (LeaseAgreement lease : expiringLeases) {
                sendExpirationNotification(lease);
            }
            
            log.info("租约到期检查任务执行完成，处理了 {} 个即将到期的租约", expiringLeases.size());
        } catch (Exception e) {
            log.error("租约到期检查任务执行失败", e);
        }
    }

    /**
     * 发送状态变更通知
     */
    private void sendStatusChangeNotification(LeaseAgreement lease, String oldStatus, String newStatus) {
        try {
            // TODO: 集成实际的通知服务
            log.info("发送租约状态变更通知，租约ID: {}, 承租人: {}, 状态变更: {} -> {}", 
                    lease.getId(), lease.getName(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("发送状态变更通知失败，租约ID: {}", lease.getId(), e);
        }
    }

    /**
     * 发送租约给租客
     */
    private void sendLeaseToTenant(LeaseAgreement lease) {
        try {
            // TODO: 集成实际的通知服务，发送租约给租客
            log.info("发送租约给租客，租约ID: {}, 承租人: {}", lease.getId(), lease.getName());
        } catch (Exception e) {
            log.error("发送租约给租客失败，租约ID: {}", lease.getId(), e);
        }
    }

    /**
     * 发送租约终止通知
     */
    private void sendTerminationNotification(LeaseAgreement lease, String reason) {
        try {
            // TODO: 集成实际的通知服务
            log.info("发送租约终止通知，租约ID: {}, 承租人: {}, 终止原因: {}", 
                    lease.getId(), lease.getName(), reason);
        } catch (Exception e) {
            log.error("发送租约终止通知失败，租约ID: {}", lease.getId(), e);
        }
    }

    @Override
    @Transactional
    public com.atguigu.lease.common.result.Result saveOrUpdateLease(LeaseAgreement leaseAgreement) {
        try {
            log.info("开始保存或更新租约信息，租约ID: {}", leaseAgreement.getId());
            
            // 1. 参数验证
            if (leaseAgreement == null) {
                return com.atguigu.lease.common.result.Result.fail("租约信息不能为空");
            }
            
            // 2. 业务规则验证
            com.atguigu.lease.common.result.Result validationResult = validateLeaseAgreement(leaseAgreement);
            if (!validationResult.getCode().equals(200)) {
                return validationResult;
            }
            
            // 3. 设置手动创建租约的默认字段
            if (leaseAgreement.getId() == null || leaseAgreement.getId().toString().isEmpty()) {
                // 新增租约
                setupNewLeaseDefaults(leaseAgreement);
            } else {
                // 更新租约
                setupUpdateLeaseDefaults(leaseAgreement);
            }
            
            // 4. 保存或更新租约
            boolean success = saveOrUpdate(leaseAgreement);
            
            if (success) {
                log.info("租约保存成功，租约ID: {}, 承租人: {}, 创建方式: {}", 
                        leaseAgreement.getId(), leaseAgreement.getName(), 
                        leaseAgreement.getAutoGenerated() == 1 ? "自动生成" : "手动创建");
                
                // 5. 发送通知给租客
                sendLeaseToTenant(leaseAgreement);
                
                return com.atguigu.lease.common.result.Result.ok("租约保存成功");
            } else {
                return com.atguigu.lease.common.result.Result.fail("租约保存失败");
            }
            
        } catch (Exception e) {
            log.error("保存或更新租约信息失败", e);
            return com.atguigu.lease.common.result.Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 验证租约信息
     */
    private com.atguigu.lease.common.result.Result validateLeaseAgreement(LeaseAgreement leaseAgreement) {
        // 1. 必填字段验证
        if (leaseAgreement.getName() == null || leaseAgreement.getName().trim().isEmpty()) {
            return com.atguigu.lease.common.result.Result.fail("承租人姓名不能为空");
        }
        
        if (leaseAgreement.getPhone() == null || leaseAgreement.getPhone().trim().isEmpty()) {
            return com.atguigu.lease.common.result.Result.fail("手机号码不能为空");
        }
        
        if (leaseAgreement.getApartmentId() == null) {
            return com.atguigu.lease.common.result.Result.fail("公寓信息不能为空");
        }
        
        if (leaseAgreement.getRoomId() == null) {
            return com.atguigu.lease.common.result.Result.fail("房间信息不能为空");
        }
        
        if (leaseAgreement.getLeaseStartDate() == null || leaseAgreement.getLeaseEndDate() == null) {
            return com.atguigu.lease.common.result.Result.fail("租约起止时间不能为空");
        }
        
        // 2. 业务规则验证
        if (leaseAgreement.getLeaseStartDate().after(leaseAgreement.getLeaseEndDate())) {
            return com.atguigu.lease.common.result.Result.fail("租约开始时间不能晚于结束时间");
        }
        
        // 3. 检查房间是否可用（新增租约时）
        if (leaseAgreement.getId() == null || leaseAgreement.getId().toString().isEmpty()) {
            if (!isRoomAvailable(leaseAgreement.getRoomId(), leaseAgreement.getLeaseStartDate(), leaseAgreement.getLeaseEndDate())) {
                return com.atguigu.lease.common.result.Result.fail("该房间在指定时间段内不可用");
            }
        }
        
        return com.atguigu.lease.common.result.Result.ok();
    }

    /**
     * 设置新增租约的默认字段
     */
    private void setupNewLeaseDefaults(LeaseAgreement leaseAgreement) {
        // 设置手动创建租约的默认字段
        leaseAgreement.setAutoGenerated(0); // 手动创建
        leaseAgreement.setCreatorType(1); // 官方创建
        leaseAgreement.setCreatorId(1L); // 默认官方人员ID为1
        leaseAgreement.setApplicationId(null); // 手动创建时为空
        
        // 设置默认状态
        if (leaseAgreement.getStatus() == null) {
            leaseAgreement.setStatus(LeaseStatus.SIGNING); // 签约待确认
        }
        
        // 设置默认来源类型
        if (leaseAgreement.getSourceType() == null) {
            leaseAgreement.setSourceType(com.atguigu.lease.model.enums.LeaseSourceType.NEW); // 新签
        }
        
        // 设置创建时间
        leaseAgreement.setCreateTime(new Date());
        leaseAgreement.setUpdateTime(new Date());
        leaseAgreement.setIsDeleted((byte) 0);
        
        log.info("设置新增租约默认字段完成 - 手动创建，官方人员，待签约状态");
    }

    /**
     * 设置更新租约的默认字段
     */
    private void setupUpdateLeaseDefaults(LeaseAgreement leaseAgreement) {
        // 更新时间
        leaseAgreement.setUpdateTime(new Date());
        
        // 获取原有租约信息，保持某些字段不变
        LeaseAgreement existingLease = getById(leaseAgreement.getId());
        if (existingLease != null) {
            // 保持创建相关字段不变
            leaseAgreement.setAutoGenerated(existingLease.getAutoGenerated());
            leaseAgreement.setCreatorType(existingLease.getCreatorType());
            leaseAgreement.setCreatorId(existingLease.getCreatorId());
            leaseAgreement.setApplicationId(existingLease.getApplicationId());
            leaseAgreement.setCreateTime(existingLease.getCreateTime());
            leaseAgreement.setIsDeleted(existingLease.getIsDeleted());
        }
        
        log.info("设置更新租约字段完成 - 保持原有创建信息不变");
    }

    /**
     * 检查房间在指定时间段内是否可用
     */
    private boolean isRoomAvailable(Long roomId, Date startDate, Date endDate) {
        try {
            // 查询该房间在指定时间段内是否有有效租约
            LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LeaseAgreement::getRoomId, roomId)
                       .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.SIGNING)
                       .and(wrapper -> wrapper
                           .le(LeaseAgreement::getLeaseStartDate, endDate)
                           .ge(LeaseAgreement::getLeaseEndDate, startDate)
                       );
            
            long conflictCount = count(queryWrapper);
            boolean available = conflictCount == 0;
            
            log.info("房间可用性检查 - 房间ID: {}, 时间段: {} 至 {}, 可用: {}", 
                    roomId, startDate, endDate, available);
            
            return available;
        } catch (Exception e) {
            log.error("检查房间可用性失败，房间ID: {}", roomId, e);
            return false;
        }
    }
}




