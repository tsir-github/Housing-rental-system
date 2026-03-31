package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.*;
import com.atguigu.lease.web.app.mapper.*;

import com.atguigu.lease.web.app.service.NotificationService;
import com.atguigu.lease.web.app.service.TransactionManagementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 事务管理服务实现类
 */
@Slf4j
@Service
public class TransactionManagementServiceImpl implements TransactionManagementService {

    @Autowired
    private DirectMoveInApplicationMapper directMoveInApplicationMapper;

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private RoomReviewMapper roomReviewMapper;

    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public LeaseAgreement processApplicationConfirmation(Long applicationId, Long processorId, Integer processorType) {
        log.info("开始处理申请确认事务，申请ID: {}, 处理人ID: {}, 处理人类型: {}", applicationId, processorId, processorType);
        
        try {
            // 1. 获取并锁定申请记录
            DirectMoveInApplication application = directMoveInApplicationMapper.selectById(applicationId);
            if (application == null) {
                throw new RuntimeException("申请不存在");
            }
            
            if (!DirectMoveInStatus.PENDING.equals(application.getStatus())) {
                throw new RuntimeException("申请状态不正确，无法确认");
            }

            // 2. 检查房间可用性（基于租约状态判断）
            RoomInfo roomInfo = roomInfoMapper.selectById(application.getRoomId());
            if (roomInfo == null) {
                throw new RuntimeException("房间不存在");
            }
            
            // 检查房间是否已发布（房东意愿）
            if (!ReleaseStatus.RELEASED.equals(roomInfo.getIsRelease())) {
                throw new RuntimeException("房间未发布，不可申请");
            }
            
            // 检查房间是否已被租用（基于租约状态）
            LambdaQueryWrapper<LeaseAgreement> occupancyQuery = new LambdaQueryWrapper<>();
            occupancyQuery.eq(LeaseAgreement::getRoomId, application.getRoomId())
                         .in(LeaseAgreement::getStatus, 
                             LeaseStatus.SIGNING,    // 签约待确认
                             LeaseStatus.SIGNED,     // 已签约
                             LeaseStatus.RENEWING);  // 续约待确认
            long occupiedCount = leaseAgreementMapper.selectCount(occupancyQuery);
            if (occupiedCount > 0) {
                throw new RuntimeException("房间已被占用，不可申请");
            }

            // 3. 检查是否已有有效租约（防止重复确认）
            LambdaQueryWrapper<LeaseAgreement> leaseQuery = new LambdaQueryWrapper<>();
            leaseQuery.eq(LeaseAgreement::getRoomId, application.getRoomId())
                     .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.SIGNING);
            long existingLeaseCount = leaseAgreementMapper.selectCount(leaseQuery);
            if (existingLeaseCount > 0) {
                throw new RuntimeException("房间已有有效租约，无法重复确认");
            }

            // 4. 更新申请状态
            application.setStatus(DirectMoveInStatus.APPROVED);
            application.setProcessorId(processorId);
            application.setProcessorType(processorType);
            application.setProcessedAt(new Date());
            directMoveInApplicationMapper.updateById(application);

            // 5. 生成租约合同
            LeaseAgreement lease = generateLeaseFromApplication(application);
            leaseAgreementMapper.insert(lease);

            // 6. 发送通知
            notificationService.notifyTenant(application.getUserId(), 
                "入住申请已确认", "您的入住申请已确认，租约合同已生成，请及时签署");

            // 8. 记录日志
            log.info("申请确认成功，申请ID: {}, 租约已生成", applicationId);

            log.info("申请确认事务处理成功，申请ID: {}, 租约ID: {}", applicationId, lease.getId());
            return lease;

        } catch (Exception e) {
            log.error("申请确认事务处理失败，申请ID: {}", applicationId, e);
            // 执行补偿事务
            executeCompensation("APPLICATION_CONFIRMATION", applicationId, e.getMessage());
            throw new RuntimeException("申请确认失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void processLeaseAgreementSigning(Long leaseId, String tenantSignature) {
        log.info("开始处理合同签署事务，租约ID: {}", leaseId);
        
        try {
            // 1. 获取并锁定租约记录
            LeaseAgreement lease = leaseAgreementMapper.selectById(leaseId);
            if (lease == null) {
                throw new RuntimeException("租约不存在");
            }
            
            if (!LeaseStatus.SIGNING.equals(lease.getStatus())) {
                throw new RuntimeException("租约状态不正确，无法签署");
            }

            // 2. 更新租约状态
            lease.setStatus(LeaseStatus.SIGNED);
            // 签署日期使用updateTime字段记录
            // lease.setSignDate(new Date()); // 该字段不存在，删除此行
            // 这里可以保存租客签名信息
            leaseAgreementMapper.updateById(lease);

            // 3. 如果是从申请生成的租约，更新申请状态
            if (lease.getApplicationId() != null) {
                DirectMoveInApplication application = directMoveInApplicationMapper.selectById(lease.getApplicationId());
                if (application != null) {
                    // 申请状态保持为已确认，可以添加新的状态如"已签约"
                    directMoveInApplicationMapper.updateById(application);
                }
            }

            // 5. 发送通知给相关方
            notificationService.notifyTenant(lease.getPhone(), 
                "租约合同已签署", "恭喜！您的租约合同已成功签署");
            
            // 通知房东（通过租约信息获取房源信息）
            if (lease.getRoomId() != null) {
                RoomInfo roomInfo = roomInfoMapper.selectById(lease.getRoomId());
                if (roomInfo != null && PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
                    notificationService.notifyLandlord(roomInfo.getPublisherId(), 
                        "租约合同已签署", "租客已签署租约合同");
                }
            }

            // 6. 记录日志
            log.info("租约合同签署成功，租约ID: {}", leaseId);

            log.info("合同签署事务处理成功，租约ID: {}", leaseId);

        } catch (Exception e) {
            log.error("合同签署事务处理失败，租约ID: {}", leaseId, e);
            executeCompensation("LEASE_SIGNING", leaseId, e.getMessage());
            throw new RuntimeException("合同签署失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRoomPublication(RoomInfo roomInfo) {
        log.info("开始处理房源发布事务，房源ID: {}", roomInfo.getId());
        
        try {
            // 1. 设置房源状态
            roomInfo.setReviewStatus(ReviewStatus.PENDING);
            roomInfo.setIsRelease(ReleaseStatus.NOT_RELEASED);
            roomInfoMapper.updateById(roomInfo);

            // 清除房源详情缓存，确保数据一致性（双删策略）
            try {
                String cacheKey = RedisConstant.APP_ROOM_PREFIX + roomInfo.getId();
                redisTemplate.delete(cacheKey); // 第一次删除
                
                // 延迟删除缓存
                Thread.sleep(100);
                redisTemplate.delete(cacheKey); // 第二次删除
                
                log.info("房源发布后清除缓存成功，房源ID: {}", roomInfo.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("缓存双删延迟被中断，房源ID: {}", roomInfo.getId());
            } catch (Exception e) {
                log.error("房源发布后清除缓存失败，房源ID: {}", roomInfo.getId(), e);
            }

            // 2. 创建审核记录
            RoomReview review = new RoomReview();
            review.setRoomId(roomInfo.getId());
            review.setApartmentId(roomInfo.getApartmentId());
            review.setLandlordId(roomInfo.getPublisherId());
            review.setStatus(ReviewStatus.PENDING);
            roomReviewMapper.insert(review);

            // 3. 发送通知给审核员
            notificationService.notifyReviewers("新房源待审核", 
                String.format("房源 %s 已提交审核，请及时处理", roomInfo.getRoomNumber()));

            // 4. 记录日志
            log.info("房源发布成功，房源ID: {}, 等待审核", roomInfo.getId());

            log.info("房源发布事务处理成功，房源ID: {}", roomInfo.getId());

        } catch (Exception e) {
            log.error("房源发布事务处理失败，房源ID: {}", roomInfo.getId(), e);
            executeCompensation("ROOM_PUBLICATION", roomInfo.getId(), e.getMessage());
            throw new RuntimeException("房源发布失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRoomReviewDecision(Long roomId, Long reviewerId, boolean approved, 
                                        String comments, Double complianceScore) {
        log.info("开始处理房源审核事务，房源ID: {}, 审核员ID: {}, 审核结果: {}", roomId, reviewerId, approved);
        
        try {
            // 1. 获取房源信息
            RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
            if (roomInfo == null) {
                throw new RuntimeException("房源不存在");
            }

            // 2. 更新审核记录
            LambdaQueryWrapper<RoomReview> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoomReview::getRoomId, roomId)
                       .eq(RoomReview::getStatus, ReviewStatus.PENDING);
            RoomReview review = roomReviewMapper.selectOne(queryWrapper);
            
            if (review == null) {
                throw new RuntimeException("未找到待审核记录");
            }

            review.setReviewerId(reviewerId);
            review.setReviewComments(comments);
            review.setComplianceScore(BigDecimal.valueOf(complianceScore));
            review.setReviewedAt(new Date());

            // 3. 更新房源状态
            if (approved) {
                review.setStatus(ReviewStatus.APPROVED);
                roomInfo.setReviewStatus(ReviewStatus.APPROVED);
                roomInfo.setIsRelease(ReleaseStatus.RELEASED);
                roomInfo.setComplianceScore(BigDecimal.valueOf(complianceScore));
                
                // 通知房东审核通过
                notificationService.notifyLandlord(roomInfo.getPublisherId(), 
                    "房源审核通过", "您的房源已通过审核并上架");
            } else {
                review.setStatus(ReviewStatus.REJECTED);
                roomInfo.setReviewStatus(ReviewStatus.REJECTED);
                
                // 通知房东审核未通过
                notificationService.notifyLandlord(roomInfo.getPublisherId(), 
                    "房源审核未通过", "审核意见：" + comments);
            }

            roomReviewMapper.updateById(review);
            roomInfoMapper.updateById(roomInfo);

            // 清除房源详情缓存，确保数据一致性（双删策略）
            try {
                String cacheKey = RedisConstant.APP_ROOM_PREFIX + roomId;
                redisTemplate.delete(cacheKey); // 第一次删除
                
                // 延迟删除缓存
                Thread.sleep(100);
                redisTemplate.delete(cacheKey); // 第二次删除
                
                log.info("房源审核后清除缓存成功，房源ID: {}", roomId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("缓存双删延迟被中断，房源ID: {}", roomId);
            } catch (Exception e) {
                log.error("房源审核后清除缓存失败，房源ID: {}", roomId, e);
            }

            // 4. 记录日志
            log.info("房源审核完成，房源ID: {}, 结果: {}, 意见: {}", roomId, approved ? "通过" : "拒绝", comments);

            log.info("房源审核事务处理成功，房源ID: {}, 结果: {}", roomId, approved ? "通过" : "拒绝");

        } catch (Exception e) {
            log.error("房源审核事务处理失败，房源ID: {}", roomId, e);
            executeCompensation("ROOM_REVIEW", roomId, e.getMessage());
            throw new RuntimeException("房源审核失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processAppointmentCompletion(Long appointmentId, boolean generateLease) {
        log.info("开始处理预约完成事务，预约ID: {}, 是否生成合同: {}", appointmentId, generateLease);
        
        try {
            // 1. 更新预约状态
            ViewAppointment appointment = viewAppointmentMapper.selectById(appointmentId);
            if (appointment == null) {
                throw new RuntimeException("预约不存在");
            }

            appointment.setAppointmentStatus(AppointmentStatus.VIEWED);
            viewAppointmentMapper.updateById(appointment);

            // 2. 如果需要生成合同，创建租约记录
            if (generateLease) {
                // 这里可以根据预约信息生成租约合同
                // 具体实现根据业务需求
            }

            // 3. 发送通知
            notificationService.notifyTenant(appointment.getUserId(), 
                "看房已完成", "感谢您的看房，如有意向请联系我们");

            // 4. 记录日志
            log.info("预约看房完成，预约ID: {}", appointmentId);

            log.info("预约完成事务处理成功，预约ID: {}", appointmentId);

        } catch (Exception e) {
            log.error("预约完成事务处理失败，预约ID: {}", appointmentId, e);
            executeCompensation("APPOINTMENT_COMPLETION", appointmentId, e.getMessage());
            throw new RuntimeException("预约完成失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processLeaseTermination(Long leaseId, String terminationReason) {
        log.info("开始处理退租事务，租约ID: {}, 退租原因: {}", leaseId, terminationReason);
        
        try {
            // 1. 更新租约状态
            LeaseAgreement lease = leaseAgreementMapper.selectById(leaseId);
            if (lease == null) {
                throw new RuntimeException("租约不存在");
            }

            lease.setStatus(LeaseStatus.WITHDRAWING);
            leaseAgreementMapper.updateById(lease);

            // 2. 发送通知
            notificationService.notifyTenant(lease.getPhone(), 
                "退租申请已受理", "您的退租申请已受理，相关事宜将有专人联系您");

            // 4. 记录日志
            log.info("退租处理完成，租约ID: {}, 原因: {}", leaseId, terminationReason);

            log.info("退租事务处理成功，租约ID: {}", leaseId);

        } catch (Exception e) {
            log.error("退租事务处理失败，租约ID: {}", leaseId, e);
            executeCompensation("LEASE_TERMINATION", leaseId, e.getMessage());
            throw new RuntimeException("退租处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncRoomStatus(Long roomId) {
        log.info("开始同步房间状态，房间ID: {}", roomId);
        
        try {
            RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
            if (roomInfo == null) {
                return;
            }

            // 检查是否有有效租约
            LambdaQueryWrapper<LeaseAgreement> leaseQuery = new LambdaQueryWrapper<>();
            leaseQuery.eq(LeaseAgreement::getRoomId, roomId)
                     .eq(LeaseAgreement::getStatus, LeaseStatus.SIGNED);
            long activeLeaseCount = leaseAgreementMapper.selectCount(leaseQuery);

            // 记录房间状态信息，但不修改发布状态
            // 房间的可用性现在完全由租约状态决定，不再修改房间的发布状态
            if (activeLeaseCount > 0) {
                log.info("房间 {} 有 {} 个有效租约，房间已被租用", roomId, activeLeaseCount);
            } else {
                log.info("房间 {} 无有效租约，房间可用", roomId);
            }

            log.info("房间状态同步完成，房间ID: {}", roomId);

        } catch (Exception e) {
            log.error("房间状态同步失败，房间ID: {}", roomId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndFixDataConsistency() {
        log.info("开始数据一致性检查和修复");
        
        try {
            // 1. 检查房间状态与租约状态的一致性
            checkRoomLeaseConsistency();
            
            // 2. 检查申请状态与租约状态的一致性
            checkApplicationLeaseConsistency();
            
            // 3. 检查审核记录与房源状态的一致性
            checkReviewRoomConsistency();

            log.info("数据一致性检查和修复完成");

        } catch (Exception e) {
            log.error("数据一致性检查和修复失败", e);
            throw new RuntimeException("数据一致性检查失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void executeCompensation(String transactionType, Long entityId, String failureReason) {
        log.info("执行补偿事务，事务类型: {}, 实体ID: {}, 失败原因: {}", transactionType, entityId, failureReason);
        
        try {
            switch (transactionType) {
                case "APPLICATION_CONFIRMATION":
                    compensateApplicationConfirmation(entityId, failureReason);
                    break;
                case "LEASE_SIGNING":
                    compensateLeaseAgreementSigning(entityId, failureReason);
                    break;
                case "ROOM_PUBLICATION":
                    compensateRoomPublication(entityId, failureReason);
                    break;
                case "ROOM_REVIEW":
                    compensateRoomReview(entityId, failureReason);
                    break;
                default:
                    log.warn("未知的事务类型，无法执行补偿: {}", transactionType);
            }
        } catch (Exception e) {
            log.error("补偿事务执行失败", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    private LeaseAgreement generateLeaseFromApplication(DirectMoveInApplication application) {
        LeaseAgreement lease = new LeaseAgreement();
        
        // 从申请中复制基本信息
        lease.setPhone(application.getPhone());
        lease.setName(application.getName());
        lease.setIdentificationNumber(application.getIdentificationNumber());
        lease.setApartmentId(application.getApartmentId());
        lease.setRoomId(application.getRoomId());
        lease.setLeaseStartDate(application.getLeaseStartDate());
        lease.setLeaseEndDate(application.getLeaseEndDate());
        lease.setLeaseTermId(application.getLeaseTermId());
        lease.setRent(application.getRent());
        lease.setDeposit(application.getDeposit());
        lease.setPaymentTypeId(application.getPaymentTypeId());
        
        // 设置创建者信息
        lease.setCreatorType(application.getProcessorType());
        lease.setCreatorId(application.getProcessorId());
        lease.setApplicationId(application.getId());
        lease.setAutoGenerated(1);
        
        // 设置租约状态为待签署
        lease.setStatus(LeaseStatus.SIGNING);
        
        return lease;
    }

    private void checkRoomLeaseConsistency() {
        // 检查房间状态与租约状态的一致性
        log.info("检查房间状态与租约状态的一致性");
        
        // 查找有有效租约但房间状态为可出租的情况
        List<RoomInfo> inconsistentRooms = roomInfoMapper.selectList(null);
        for (RoomInfo room : inconsistentRooms) {
            syncRoomStatus(room.getId());
        }
    }

    private void checkApplicationLeaseConsistency() {
        // 检查申请状态与租约状态的一致性
        log.info("检查申请状态与租约状态的一致性");
        // 具体实现根据业务需求
    }

    private void checkReviewRoomConsistency() {
        // 检查审核记录与房源状态的一致性
        log.info("检查审核记录与房源状态的一致性");
        // 具体实现根据业务需求
    }

    private void compensateApplicationConfirmation(Long applicationId, String failureReason) {
        // 申请确认失败的补偿逻辑
        log.info("执行申请确认补偿，申请ID: {}", applicationId);
        
        try {
            DirectMoveInApplication application = directMoveInApplicationMapper.selectById(applicationId);
            if (application != null && DirectMoveInStatus.APPROVED.equals(application.getStatus())) {
                // 回滚申请状态
                application.setStatus(DirectMoveInStatus.PENDING);
                application.setProcessorId(null);
                application.setProcessorType(null);
                application.setProcessedAt(null);
                directMoveInApplicationMapper.updateById(application);
            }
        } catch (Exception e) {
            log.error("申请确认补偿失败", e);
        }
    }

    private void compensateLeaseAgreementSigning(Long leaseId, String failureReason) {
        // 合同签署失败的补偿逻辑
        log.info("执行合同签署补偿，租约ID: {}", leaseId);
        // 具体实现根据业务需求
    }

    private void compensateRoomPublication(Long roomId, String failureReason) {
        // 房源发布失败的补偿逻辑
        log.info("执行房源发布补偿，房源ID: {}", roomId);
        // 具体实现根据业务需求
    }

    private void compensateRoomReview(Long roomId, String failureReason) {
        // 房源审核失败的补偿逻辑
        log.info("执行房源审核补偿，房源ID: {}", roomId);
        // 具体实现根据业务需求
    }
}