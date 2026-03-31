package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.LeaseTerm;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.app.mapper.LandlordLeaseMapper;
import com.atguigu.lease.web.app.mapper.LeaseTermMapper;
import com.atguigu.lease.web.app.service.LandlordLeaseService;
import com.atguigu.lease.web.app.vo.landlord.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 房东租约管理服务实现类
 * 职责：处理房东租约相关的业务逻辑，确保数据权限控制
 */
@Slf4j
@Service
public class LandlordLeaseServiceImpl implements LandlordLeaseService {

    @Autowired
    private LandlordLeaseMapper landlordLeaseMapper;
    
    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Override
    public IPage<LandlordLeaseVo> pageLandlordLeaseByQuery(Page<LandlordLeaseVo> page, Long landlordId, LandlordLeaseQueryVo queryVo) {
        log.info("查询房东租约列表，房东ID: {}", landlordId);
        return landlordLeaseMapper.pageLandlordLeaseByQuery(page, landlordId, queryVo);
    }

    @Override
    public LandlordLeaseVo getLandlordLeaseById(Long leaseId, Long landlordId) {
        log.info("查询房东租约基本信息，租约ID: {}, 房东ID: {}", leaseId, landlordId);
        
        // 验证权限
        validateLandlordLeasePermission(leaseId, landlordId);
        
        return landlordLeaseMapper.selectLandlordLeaseById(leaseId, landlordId);
    }

    @Override
    public LandlordLeaseDetailVo getLandlordLeaseDetailById(Long leaseId, Long landlordId) {
        log.info("查询房东租约详情，租约ID: {}, 房东ID: {}", leaseId, landlordId);
        
        // 验证权限
        validateLandlordLeasePermission(leaseId, landlordId);
        
        return landlordLeaseMapper.selectLandlordLeaseDetailById(leaseId, landlordId);
    }

    @Override
    @Transactional
    public void updateLandlordLeaseStatus(Long leaseId, Long landlordId, LeaseStatus status, Boolean approved, String reason) {
        log.info("房东更新租约状态，租约ID: {}, 房东ID: {}, 新状态: {}, 是否同意: {}", leaseId, landlordId, status, approved);
        
        // 验证权限
        LeaseAgreement lease = validateLandlordLeasePermission(leaseId, landlordId);
        
        // 验证状态变更规则
        validateStatusChange(lease.getStatus(), status);
        
        // 如果是从"续约待确认"改为"已签约"，根据approved参数决定是否延长日期
        if (lease.getStatus() == LeaseStatus.RENEWING && status == LeaseStatus.SIGNED) {
            if (approved != null && approved) {
                // 同意续约：延长日期
                log.info("房东同意续约，将自动延长租约日期");
                
                // 查询租期信息
                LeaseTerm leaseTerm = leaseTermMapper.selectById(lease.getLeaseTermId());
                if (leaseTerm == null) {
                    throw new RuntimeException("租期信息不存在");
                }
                
                // 从原结束日期开始延长
                Date oldEndDate = lease.getLeaseEndDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(oldEndDate);
                calendar.add(Calendar.MONTH, leaseTerm.getMonthCount());
                Date newEndDate = calendar.getTime();
                
                log.info("续约日期计算：原结束日期={}, 续约月数={}, 新结束日期={}", 
                    oldEndDate, leaseTerm.getMonthCount(), newEndDate);
                
                // 更新租约：新的结束日期和状态
                LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(LeaseAgreement::getId, leaseId)
                            .set(LeaseAgreement::getStatus, status)
                            .set(LeaseAgreement::getLeaseEndDate, newEndDate)
                            .set(LeaseAgreement::getUpdateTime, new Date());
                
                landlordLeaseMapper.update(null, updateWrapper);
            } else {
                // 拒绝续约：只更新状态，不延长日期
                log.info("房东拒绝续约，不延长租约日期");
                
                LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(LeaseAgreement::getId, leaseId)
                            .set(LeaseAgreement::getStatus, status)
                            .set(LeaseAgreement::getUpdateTime, new Date());
                
                landlordLeaseMapper.update(null, updateWrapper);
            }
        } else {
            // 其他情况只更新状态
            LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(LeaseAgreement::getId, leaseId)
                        .set(LeaseAgreement::getStatus, status)
                        .set(LeaseAgreement::getUpdateTime, new Date());
            
            landlordLeaseMapper.update(null, updateWrapper);
        }
        
        // 记录操作日志
        recordLeaseStatusChange(leaseId, lease.getStatus().name(), status.name(), reason, landlordId);
        
        // 发送通知给租客
        sendStatusChangeNotification(lease, status);
        
        log.info("房东租约状态更新成功");
    }

    @Override
    public LandlordLeaseStatisticsVo getLandlordLeaseStatistics(Long landlordId) {
        log.info("获取房东租约统计数据，房东ID: {}", landlordId);
        
        // 获取基础统计数据
        LandlordLeaseStatisticsVo statistics = landlordLeaseMapper.selectLandlordLeaseStatistics(landlordId);
        
        // 获取状态统计数据
        List<StatusStatVo> statusStats = landlordLeaseMapper.selectLandlordLeaseStatusStats(landlordId);
        statistics.setStatusStats(statusStats);
        
        // 获取来源统计数据
        List<SourceStatVo> sourceStats = landlordLeaseMapper.selectLandlordLeaseSourceStats(landlordId);
        statistics.setSourceStats(sourceStats);
        
        return statistics;
    }

    @Override
    public List<LandlordLeaseVo> getLandlordExpiringLeases(Long landlordId) {
        log.info("获取房东即将到期租约，房东ID: {}", landlordId);
        
        LocalDate now = LocalDate.now();
        Date expirationDate = Date.from(now.plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        return landlordLeaseMapper.selectLandlordExpiringLeases(landlordId, expirationDate);
    }

    @Override
    @Transactional
    public Long createLandlordLease(LandlordLeaseCreateVo createVo, Long landlordId) {
        log.info("房东创建新租约，房东ID: {}", landlordId);
        
        // 转换VO为实体
        LeaseAgreement leaseAgreement = new LeaseAgreement();
        BeanUtils.copyProperties(createVo, leaseAgreement);
        
        // 设置房东创建的租约字段
        leaseAgreement.setCreatorType(2); // 房东创建
        leaseAgreement.setCreatorId(landlordId);
        leaseAgreement.setAutoGenerated(0); // 手动创建
        leaseAgreement.setStatus(LeaseStatus.SIGNING); // 签约待确认
        
        // 根据创建场景设置租约来源类型
        setLeaseSourceType(leaseAgreement, createVo);
        
        leaseAgreement.setCreateTime(new Date());
        leaseAgreement.setUpdateTime(new Date());
        leaseAgreement.setIsDeleted((byte) 0);
        
        // 验证业务规则
        validateLeaseAgreement(leaseAgreement);
        
        // 保存租约
        landlordLeaseMapper.insert(leaseAgreement);
        
        // 发送通知给租客
        sendLeaseToTenant(leaseAgreement);
        
        log.info("房东创建租约成功，租约ID: {}, 来源类型: {}", leaseAgreement.getId(), leaseAgreement.getSourceType());
        return leaseAgreement.getId();
    }

    @Override
    @Transactional
    public void updateLandlordLease(Long leaseId, LandlordLeaseUpdateVo updateVo, Long landlordId) {
        log.info("房东修改租约，租约ID: {}, 房东ID: {}", leaseId, landlordId);
        
        // 验证权限
        LeaseAgreement existingLease = validateLandlordLeasePermission(leaseId, landlordId);
        
        // 验证是否可以编辑（只有特定状态下才能编辑）
        if (existingLease.getStatus() != LeaseStatus.SIGNING) {
            throw new RuntimeException("只有签约待确认状态的租约才能编辑");
        }
        
        // 转换VO为实体
        LeaseAgreement leaseAgreement = new LeaseAgreement();
        BeanUtils.copyProperties(updateVo, leaseAgreement);
        
        // 设置更新字段
        leaseAgreement.setId(leaseId);
        leaseAgreement.setUpdateTime(new Date());
        
        // 验证业务规则
        validateLeaseAgreement(leaseAgreement);
        
        // 更新租约
        landlordLeaseMapper.updateById(leaseAgreement);
        
        log.info("房东修改租约成功");
    }

    @Override
    public void sendLandlordLeaseReminder(Long leaseId, Long landlordId) {
        log.info("房东发送租约提醒，租约ID: {}, 房东ID: {}", leaseId, landlordId);
        
        // 验证权限
        LeaseAgreement lease = validateLandlordLeasePermission(leaseId, landlordId);
        
        // 发送到期提醒
        sendExpirationNotification(lease);
        
        log.info("房东发送租约提醒成功");
    }

    @Override
    @Transactional
    public Long resendLandlordLease(Long leaseId, Long landlordId) {
        log.info("房东重发租约，原租约ID: {}, 房东ID: {}", leaseId, landlordId);
        
        // 验证权限
        LeaseAgreement originalLease = validateLandlordLeasePermission(leaseId, landlordId);
        
        // 验证原租约状态（只有已取消的租约才能重发）
        if (originalLease.getStatus() != LeaseStatus.CANCELED) {
            throw new RuntimeException("只有已取消的租约才能重发");
        }
        
        // 创建新租约，复制原租约的所有信息
        LeaseAgreement newLease = new LeaseAgreement();
        
        // 复制租约基本信息
        newLease.setPhone(originalLease.getPhone());
        newLease.setName(originalLease.getName());
        newLease.setIdentificationNumber(originalLease.getIdentificationNumber());
        newLease.setApartmentId(originalLease.getApartmentId());
        newLease.setRoomId(originalLease.getRoomId());
        newLease.setLeaseStartDate(originalLease.getLeaseStartDate());
        newLease.setLeaseEndDate(originalLease.getLeaseEndDate());
        newLease.setLeaseTermId(originalLease.getLeaseTermId());
        newLease.setRent(originalLease.getRent());
        newLease.setDeposit(originalLease.getDeposit());
        newLease.setPaymentTypeId(originalLease.getPaymentTypeId());
        
        // 设置新租约的状态和元信息
        newLease.setStatus(LeaseStatus.SIGNING); // 重新设为签约待确认
        newLease.setSourceType(originalLease.getSourceType()); // 保持原来的来源类型
        newLease.setCreatorType(originalLease.getCreatorType()); // 保持原来的创建者类型
        newLease.setCreatorId(originalLease.getCreatorId()); // 保持原来的创建者ID
        newLease.setApplicationId(originalLease.getApplicationId()); // 保持关联的申请ID
        newLease.setAutoGenerated(0); // 标记为手动重发
        newLease.setAdditionalInfo("基于租约ID:" + leaseId + " 重新发送");
        
        // 设置时间戳
        Date now = new Date();
        newLease.setCreateTime(now);
        newLease.setUpdateTime(now);
        newLease.setIsDeleted((byte) 0);
        
        // 验证房间是否仍然可用
        if (!isRoomStillAvailable(originalLease.getRoomId(), leaseId)) {
            throw new RuntimeException("房间已被其他租约占用，无法重发");
        }
        
        // 保存新租约
        landlordLeaseMapper.insert(newLease);
        
        // 发送通知给租客
        sendLeaseToTenant(newLease);
        
        // 记录重发操作日志
        recordLeaseResendLog(leaseId, newLease.getId(), landlordId);
        
        log.info("房东重发租约成功，原租约ID: {}, 新租约ID: {}", leaseId, newLease.getId());
        return newLease.getId();
    }

    @Override
    @Transactional
    public Long resendLandlordLeaseWithData(Long leaseId, LandlordLeaseCreateVo createVo, Long landlordId) {
        log.info("房东重发租约（带修改数据），原租约ID: {}, 房东ID: {}", leaseId, landlordId);
        
        // 验证权限
        LeaseAgreement originalLease = validateLandlordLeasePermission(leaseId, landlordId);
        
        // 验证原租约状态（只有已取消的租约才能重发）
        if (originalLease.getStatus() != LeaseStatus.CANCELED) {
            throw new RuntimeException("只有已取消的租约才能重发");
        }
        
        // 创建新租约，使用修改后的数据
        LeaseAgreement newLease = new LeaseAgreement();
        BeanUtils.copyProperties(createVo, newLease);
        
        // 设置新租约的状态和元信息
        newLease.setStatus(LeaseStatus.SIGNING); // 重新设为签约待确认
        newLease.setSourceType(originalLease.getSourceType()); // 保持原来的来源类型
        newLease.setCreatorType(originalLease.getCreatorType()); // 保持原来的创建者类型
        newLease.setCreatorId(originalLease.getCreatorId()); // 保持原来的创建者ID
        newLease.setApplicationId(originalLease.getApplicationId()); // 保持关联的申请ID
        newLease.setAutoGenerated(0); // 标记为手动重发
        
        // 如果没有提供备注信息，则使用默认的重发说明
        if (createVo.getAdditionalInfo() == null || createVo.getAdditionalInfo().trim().isEmpty()) {
            newLease.setAdditionalInfo("基于租约ID:" + leaseId + " 重新发送");
        }
        
        // 设置时间戳
        Date now = new Date();
        newLease.setCreateTime(now);
        newLease.setUpdateTime(now);
        newLease.setIsDeleted((byte) 0);
        
        // 验证修改后的租约信息
        validateLeaseAgreement(newLease);
        
        // 验证房间是否仍然可用（如果房间发生了变化）
        if (!originalLease.getRoomId().equals(createVo.getRoomId())) {
            if (!isRoomStillAvailable(createVo.getRoomId(), leaseId)) {
                throw new RuntimeException("新选择的房间已被其他租约占用，无法重发");
            }
        }
        
        // 保存新租约
        landlordLeaseMapper.insert(newLease);
        
        // 发送通知给租客
        sendLeaseToTenant(newLease);
        
        // 记录重发操作日志（包含修改信息）
        recordLeaseResendWithDataLog(leaseId, newLease.getId(), originalLease, createVo, landlordId);
        
        log.info("房东重发租约（带修改数据）成功，原租约ID: {}, 新租约ID: {}", leaseId, newLease.getId());
        return newLease.getId();
    }

    @Override
    @Transactional
    public void removeLandlordLease(Long leaseId, Long landlordId) {
        log.info("房东删除租约，租约ID: {}, 房东ID: {}", leaseId, landlordId);
        
        // 验证权限
        LeaseAgreement lease = validateLandlordLeasePermission(leaseId, landlordId);
        
        // 验证是否可以删除（只有特定状态下才能删除）
        if (lease.getStatus() != LeaseStatus.SIGNING && lease.getStatus() != LeaseStatus.CANCELED) {
            throw new RuntimeException("只有签约待确认或已取消状态的租约才能删除");
        }
        
        // 软删除租约
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LeaseAgreement::getId, leaseId)
                    .set(LeaseAgreement::getIsDeleted, 1)
                    .set(LeaseAgreement::getUpdateTime, new Date());
        
        landlordLeaseMapper.update(null, updateWrapper);
        
        log.info("房东删除租约成功");
    }

    @Override
    public LeaseAgreement validateLandlordLeasePermission(Long leaseId, Long landlordId) {
        // 使用Mapper中的权限验证方法
        Integer count = landlordLeaseMapper.validateLandlordLeasePermission(leaseId, landlordId);
        if (count == null || count == 0) {
            throw new RuntimeException("租约不存在或无权限操作");
        }
        
        // 获取租约详情
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getId, leaseId)
                   .eq(LeaseAgreement::getCreatorType, 2)
                   .eq(LeaseAgreement::getCreatorId, landlordId)
                   .eq(LeaseAgreement::getIsDeleted, 0);
        
        return landlordLeaseMapper.selectOne(queryWrapper);
    }

    /**
     * 根据创建场景设置租约来源类型
     * 1. NEW(1, "新签") - 普通的新租约创建
     * 2. RENEW(2, "续约") - 基于现有租约的续约
     * 3. DIRECT_APPLICATION(3, "直接入住申请") - 基于直接入住申请创建的租约
     */
    private void setLeaseSourceType(LeaseAgreement leaseAgreement, LandlordLeaseCreateVo createVo) {
        // 导入枚举类
        com.atguigu.lease.model.enums.LeaseSourceType sourceType;
        
        // 检查是否是基于预约创建的租约
        String additionalInfo = createVo.getAdditionalInfo();
        if (additionalInfo != null && additionalInfo.contains("基于预约ID:")) {
            // 基于预约创建的租约，设置为新签
            sourceType = com.atguigu.lease.model.enums.LeaseSourceType.NEW;
            log.info("检测到基于预约创建的租约，设置来源类型为: NEW(新签)");
        }
        // 检查是否是基于直接入住申请创建的租约
        else if (leaseAgreement.getApplicationId() != null && leaseAgreement.getApplicationId() > 0) {
            // 有关联的申请ID，说明是基于直接入住申请创建的
            sourceType = com.atguigu.lease.model.enums.LeaseSourceType.DIRECT_APPLICATION;
            log.info("检测到基于直接入住申请创建的租约，设置来源类型为: DIRECT_APPLICATION(直接入住申请)");
        }
        // 检查是否是续约
        else if (additionalInfo != null && (additionalInfo.contains("续约") || additionalInfo.contains("renew"))) {
            sourceType = com.atguigu.lease.model.enums.LeaseSourceType.RENEW;
            log.info("检测到续约租约，设置来源类型为: RENEW(续约)");
        }
        // 默认情况：新签租约
        else {
            sourceType = com.atguigu.lease.model.enums.LeaseSourceType.NEW;
            log.info("默认设置来源类型为: NEW(新签)");
        }
        
        leaseAgreement.setSourceType(sourceType);
    }

    /**
     * 验证状态变更规则
     */
    private void validateStatusChange(LeaseStatus currentStatus, LeaseStatus newStatus) {
        // 根据业务规则验证状态变更是否合法
        boolean isValidTransition = false;
        
        switch (currentStatus) {
            case SIGNING:
                isValidTransition = newStatus == LeaseStatus.SIGNED || newStatus == LeaseStatus.CANCELED;
                break;
            case SIGNED:
                isValidTransition = newStatus == LeaseStatus.WITHDRAWING || newStatus == LeaseStatus.RENEWING;
                break;
            case WITHDRAWING:
                isValidTransition = newStatus == LeaseStatus.WITHDRAWN || newStatus == LeaseStatus.SIGNED;
                break;
            case RENEWING:
                isValidTransition = newStatus == LeaseStatus.SIGNED || newStatus == LeaseStatus.CANCELED;
                break;
            default:
                isValidTransition = false;
        }
        
        if (!isValidTransition) {
            throw new RuntimeException("不允许的状态变更：从 " + currentStatus.getName() + " 到 " + newStatus.getName());
        }
    }

    /**
     * 验证租约信息
     */
    private void validateLeaseAgreement(LeaseAgreement leaseAgreement) {
        if (leaseAgreement.getName() == null || leaseAgreement.getName().trim().isEmpty()) {
            throw new RuntimeException("承租人姓名不能为空");
        }
        
        if (leaseAgreement.getPhone() == null || leaseAgreement.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号码不能为空");
        }
        
        if (leaseAgreement.getApartmentId() == null) {
            throw new RuntimeException("公寓信息不能为空");
        }
        
        if (leaseAgreement.getRoomId() == null) {
            throw new RuntimeException("房间信息不能为空");
        }
        
        if (leaseAgreement.getLeaseStartDate() == null || leaseAgreement.getLeaseEndDate() == null) {
            throw new RuntimeException("租约起止时间不能为空");
        }
        
        if (leaseAgreement.getLeaseStartDate().after(leaseAgreement.getLeaseEndDate())) {
            throw new RuntimeException("租约开始时间不能晚于结束时间");
        }
        
        if (leaseAgreement.getRent() == null || leaseAgreement.getRent().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("租金必须大于0");
        }
        
        if (leaseAgreement.getDeposit() == null || leaseAgreement.getDeposit().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("押金不能为负数");
        }
    }

    /**
     * 记录租约状态变更日志
     */
    private void recordLeaseStatusChange(Long leaseId, String oldStatus, String newStatus, String reason, Long operatorId) {
        log.info("租约状态变更记录 - 租约ID: {}, 原状态: {}, 新状态: {}, 变更原因: {}, 操作人: {}", 
                leaseId, oldStatus, newStatus, reason, operatorId);
        // TODO: 如果需要持久化日志，可以创建专门的日志表
    }

    /**
     * 发送状态变更通知给租客
     */
    private void sendStatusChangeNotification(LeaseAgreement lease, LeaseStatus newStatus) {
        log.info("发送租约状态变更通知，租约ID: {}, 承租人: {}, 新状态: {}", 
                lease.getId(), lease.getName(), newStatus.getName());
        // TODO: 集成实际的通知服务
    }

    /**
     * 发送租约给租客
     */
    private void sendLeaseToTenant(LeaseAgreement lease) {
        log.info("发送租约给租客，租约ID: {}, 承租人: {}", lease.getId(), lease.getName());
        // TODO: 集成实际的通知服务，发送租约给租客
    }

    /**
     * 发送租约到期提醒
     */
    private void sendExpirationNotification(LeaseAgreement lease) {
        log.info("发送租约到期提醒，租约ID: {}, 承租人: {}", lease.getId(), lease.getName());
        // TODO: 集成实际的通知服务
    }

    /**
     * 检查房间是否仍然可用（排除指定的租约ID）
     */
    private boolean isRoomStillAvailable(Long roomId, Long excludeLeaseId) {
        log.info("检查房间可用性 - roomId: {}, excludeLeaseId: {}", roomId, excludeLeaseId);
        
        // 查询该房间是否有其他有效的租约（排除当前租约）
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getRoomId, roomId)
                   .in(LeaseAgreement::getStatus, LeaseStatus.SIGNING, LeaseStatus.SIGNED)
                   .ne(LeaseAgreement::getId, excludeLeaseId)
                   .eq(LeaseAgreement::getIsDeleted, 0);
        
        long activeLeaseCount = landlordLeaseMapper.selectCount(queryWrapper);
        
        boolean available = activeLeaseCount == 0;
        log.info("房间可用性检查结果 - roomId: {}, available: {}", roomId, available);
        return available;
    }

    /**
     * 记录租约重发操作日志
     */
    private void recordLeaseResendLog(Long originalLeaseId, Long newLeaseId, Long operatorId) {
        log.info("租约重发记录 - 原租约ID: {}, 新租约ID: {}, 操作人: {}", 
                originalLeaseId, newLeaseId, operatorId);
        // TODO: 如果需要持久化日志，可以创建专门的日志表
    }

    /**
     * 记录带修改数据的租约重发操作日志
     */
    private void recordLeaseResendWithDataLog(Long originalLeaseId, Long newLeaseId, 
                                            LeaseAgreement originalLease, LandlordLeaseCreateVo createVo, Long operatorId) {
        log.info("租约重发（带修改数据）记录 - 原租约ID: {}, 新租约ID: {}, 操作人: {}", 
                originalLeaseId, newLeaseId, operatorId);
        
        // 记录具体的修改内容
        StringBuilder changes = new StringBuilder();
        
        if (!originalLease.getName().equals(createVo.getName())) {
            changes.append("租客姓名从\"").append(originalLease.getName()).append("\"改为\"").append(createVo.getName()).append("\"; ");
        }
        
        if (!originalLease.getPhone().equals(createVo.getPhone())) {
            changes.append("手机号从\"").append(originalLease.getPhone()).append("\"改为\"").append(createVo.getPhone()).append("\"; ");
        }
        
        if (!originalLease.getIdentificationNumber().equals(createVo.getIdentificationNumber())) {
            changes.append("身份证号从\"").append(originalLease.getIdentificationNumber()).append("\"改为\"").append(createVo.getIdentificationNumber()).append("\"; ");
        }
        
        if (!originalLease.getApartmentId().equals(createVo.getApartmentId())) {
            changes.append("公寓ID从\"").append(originalLease.getApartmentId()).append("\"改为\"").append(createVo.getApartmentId()).append("\"; ");
        }
        
        if (!originalLease.getRoomId().equals(createVo.getRoomId())) {
            changes.append("房间ID从\"").append(originalLease.getRoomId()).append("\"改为\"").append(createVo.getRoomId()).append("\"; ");
        }
        
        if (originalLease.getRent().compareTo(createVo.getRent()) != 0) {
            changes.append("月租金从\"").append(originalLease.getRent()).append("元\"改为\"").append(createVo.getRent()).append("元\"; ");
        }
        
        if (originalLease.getDeposit().compareTo(createVo.getDeposit()) != 0) {
            changes.append("押金从\"").append(originalLease.getDeposit()).append("元\"改为\"").append(createVo.getDeposit()).append("元\"; ");
        }
        
        if (!originalLease.getLeaseTermId().equals(createVo.getLeaseTermId())) {
            changes.append("租期ID从\"").append(originalLease.getLeaseTermId()).append("\"改为\"").append(createVo.getLeaseTermId()).append("\"; ");
        }
        
        if (!originalLease.getPaymentTypeId().equals(createVo.getPaymentTypeId())) {
            changes.append("支付方式ID从\"").append(originalLease.getPaymentTypeId()).append("\"改为\"").append(createVo.getPaymentTypeId()).append("\"; ");
        }
        
        if (!originalLease.getLeaseStartDate().equals(createVo.getLeaseStartDate())) {
            changes.append("开始时间从\"").append(originalLease.getLeaseStartDate()).append("\"改为\"").append(createVo.getLeaseStartDate()).append("\"; ");
        }
        
        if (!originalLease.getLeaseEndDate().equals(createVo.getLeaseEndDate())) {
            changes.append("结束时间从\"").append(originalLease.getLeaseEndDate()).append("\"改为\"").append(createVo.getLeaseEndDate()).append("\"; ");
        }
        
        String originalAdditionalInfo = originalLease.getAdditionalInfo() != null ? originalLease.getAdditionalInfo() : "";
        String newAdditionalInfo = createVo.getAdditionalInfo() != null ? createVo.getAdditionalInfo() : "";
        if (!originalAdditionalInfo.equals(newAdditionalInfo)) {
            changes.append("备注信息已修改; ");
        }
        
        if (changes.length() > 0) {
            log.info("租约重发修改详情: {}", changes.toString());
        } else {
            log.info("租约重发未发现数据修改");
        }
        
        // TODO: 如果需要持久化日志，可以创建专门的日志表
    }
}