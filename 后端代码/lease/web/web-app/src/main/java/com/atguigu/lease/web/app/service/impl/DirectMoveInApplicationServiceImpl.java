package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.DirectMoveInStatus;
import com.atguigu.lease.model.enums.LeaseSourceType;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.web.app.mapper.DirectMoveInApplicationMapper;
import com.atguigu.lease.web.app.mapper.LeaseAgreementMapper;
import com.atguigu.lease.web.app.mapper.RoomInfoMapper;
import com.atguigu.lease.web.app.service.DataValidationService;
import com.atguigu.lease.web.app.service.DirectMoveInApplicationService;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationDetailVO;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationSubmitVO;
import com.atguigu.lease.web.app.vo.landlord.LandlordApplicationStatisticsVo;
import com.atguigu.lease.web.app.vo.room.RoomAvailabilityVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 移动端直接入住申请服务实现类
 */
@Slf4j
@Service
public class DirectMoveInApplicationServiceImpl extends ServiceImpl<DirectMoveInApplicationMapper, DirectMoveInApplication>
        implements DirectMoveInApplicationService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    
    @Autowired
    private DataValidationService dataValidationService;

    @Override
    @Transactional
    public Long submitApplication(DirectMoveInApplicationSubmitVO submitVO) {
        log.info("提交直接入住申请 - userId: {}, roomId: {}", submitVO.getUserId(), submitVO.getRoomId());
        
        try {
            // 1. 数据验证
            if (submitVO.getUserId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (submitVO.getRoomId() == null) {
                throw new IllegalArgumentException("房间ID不能为空");
            }
            
            // 2. 验证房源是否存在且可用
            RoomInfo roomInfo = roomInfoMapper.selectById(submitVO.getRoomId());
            if (roomInfo == null) {
                throw new IllegalArgumentException("房源不存在");
            }
            
            // 3. 检查是否已有待处理的申请
            LambdaQueryWrapper<DirectMoveInApplication> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DirectMoveInApplication::getUserId, submitVO.getUserId())
                       .eq(DirectMoveInApplication::getRoomId, submitVO.getRoomId())
                       .eq(DirectMoveInApplication::getStatus, DirectMoveInStatus.PENDING);
            
            long existingCount = this.count(queryWrapper);
            if (existingCount > 0) {
                throw new IllegalArgumentException("您已对该房源提交过申请，请勿重复提交");
            }
            
            // 4. 创建申请实体
            DirectMoveInApplication application = new DirectMoveInApplication();
            BeanUtils.copyProperties(submitVO, application);
            
            // 设置申请状态和时间
            application.setStatus(DirectMoveInStatus.PENDING);
            application.setCreateTime(new Date());
            application.setUpdateTime(new Date());
            
            // 从房源信息中获取发布者ID（如果是房东发布的房源）
            if (PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
                application.setLandlordId(roomInfo.getPublisherId());
            }
            
            // 5. 数据校验
            dataValidationService.validateDirectMoveInApplication(application);
            
            // 6. 保存申请
            boolean saved = this.save(application);
            if (!saved) {
                throw new RuntimeException("申请提交失败");
            }
            
            log.info("申请提交成功 - applicationId: {}", application.getId());
            return application.getId();
            
        } catch (Exception e) {
            log.error("提交申请失败 - userId: {}, roomId: {}, error: {}", 
                     submitVO.getUserId(), submitVO.getRoomId(), e.getMessage(), e);
            throw new RuntimeException("申请提交失败：" + e.getMessage());
        }
    }

    @Override
    public List<DirectMoveInApplicationDetailVO> getApplicationsByUserId(Long userId) {
        log.info("查询用户申请列表 - userId: {}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        try {
            List<DirectMoveInApplicationDetailVO> applications = baseMapper.selectApplicationsByUserId(userId);
            log.info("查询到用户申请 {} 条 - userId: {}", applications.size(), userId);
            return applications;
        } catch (Exception e) {
            log.error("查询用户申请列表失败 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("查询申请列表失败：" + e.getMessage());
        }
    }

    @Override
    public List<DirectMoveInApplicationDetailVO> getPendingApplicationsByLandlord(Long landlordId) {
        log.info("查询房东待处理申请列表 - landlordId: {}", landlordId);
        return baseMapper.selectPendingApplicationsByLandlord(landlordId);
    }

    @Override
    @Transactional
    public boolean processApplication(Long applicationId, Long processorId, Integer processorType, 
                                    boolean approved, String rejectionReason) {
        log.info("处理申请 - applicationId: {}, processorId: {}, approved: {}", applicationId, processorId, approved);
        
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

        boolean result = this.updateById(application);
        
        // 如果申请通过，自动生成租约合同
        if (approved && result) {
            try {
                Long leaseId = generateLeaseAgreement(applicationId);
                log.info("申请{}通过，已自动生成租约合同 - leaseId: {}", applicationId, leaseId);
            } catch (Exception e) {
                log.error("生成租约合同失败 - applicationId: {}", applicationId, e);
                // 这里可以选择回滚申请状态或者记录错误日志
                throw new RuntimeException("生成租约合同失败：" + e.getMessage());
            }
        }

        return result;
    }

    @Override
    public DirectMoveInApplicationDetailVO getApplicationDetail(Long applicationId) {
        log.info("查询申请详情 - applicationId: {}", applicationId);
        return baseMapper.selectApplicationDetailById(applicationId);
    }

    @Override
    public boolean validateRoomAvailability(Long roomId) {
        log.info("验证房间可用性 - roomId: {}", roomId);
        
        try {
            // 1. 检查房源是否存在
            RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
            if (roomInfo == null) {
                log.warn("房源不存在 - roomId: {}", roomId);
                return false;
            }
            
            // 2. 检查房源是否已发布
            if (roomInfo.getIsRelease() == null || !ReleaseStatus.RELEASED.equals(roomInfo.getIsRelease())) {
                log.warn("房源未发布 - roomId: {}", roomId);
                return false;
            }
            
            // 3. 检查是否有正在进行的租约
            LambdaQueryWrapper<LeaseAgreement> leaseQueryWrapper = new LambdaQueryWrapper<>();
            leaseQueryWrapper.eq(LeaseAgreement::getRoomId, roomId)
                           .in(LeaseAgreement::getStatus, 
                               LeaseStatus.SIGNING,    // 1-签约待确认
                               LeaseStatus.SIGNED,     // 2-已签约  
                               LeaseStatus.RENEWING)   // 7-续约待确认
                           .eq(LeaseAgreement::getIsDeleted, 0);
            
            long activeLeaseCount = leaseAgreementMapper.selectCount(leaseQueryWrapper);
            if (activeLeaseCount > 0) {
                log.warn("房源已有活跃租约 - roomId: {}, activeLeaseCount: {}", roomId, activeLeaseCount);
                return false;
            }
            
            log.info("房源可用 - roomId: {}", roomId);
            return true;
            
        } catch (Exception e) {
            log.error("验证房间可用性失败 - roomId: {}, error: {}", roomId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public RoomAvailabilityVo checkRoomAvailability(Long roomId) {
        log.info("检查房间可用性详情 - roomId: {}", roomId);
        
        try {
            // 1. 检查房源是否存在
            RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
            if (roomInfo == null) {
                log.warn("房源不存在 - roomId: {}", roomId);
                return RoomAvailabilityVo.notFound();
            }
            
            // 2. 检查房源是否已发布（房东意愿）
            if (roomInfo.getIsRelease() == null || !ReleaseStatus.RELEASED.equals(roomInfo.getIsRelease())) {
                log.warn("房源未发布 - roomId: {}", roomId);
                return RoomAvailabilityVo.notReleased();
            }
            
            // 3. 检查审核状态
            if (!ReviewStatus.APPROVED.equals(roomInfo.getReviewStatus())) {
                log.warn("房源未通过审核 - roomId: {}", roomId);
                return RoomAvailabilityVo.notApproved();
            }
            
            // 4. 检查是否有占用房源的租约
            LambdaQueryWrapper<LeaseAgreement> leaseQueryWrapper = new LambdaQueryWrapper<>();
            leaseQueryWrapper.eq(LeaseAgreement::getRoomId, roomId)
                           .in(LeaseAgreement::getStatus, 
                               LeaseStatus.SIGNING,    // 1-签约待确认
                               LeaseStatus.SIGNED,     // 2-已签约  
                               LeaseStatus.RENEWING)   // 7-续约待确认
                           .eq(LeaseAgreement::getIsDeleted, 0);
            
            List<LeaseAgreement> activeLeases = leaseAgreementMapper.selectList(leaseQueryWrapper);
            if (!activeLeases.isEmpty()) {
                // 获取第一个活跃租约的状态，用于生成友好提示
                LeaseStatus leaseStatus = activeLeases.get(0).getStatus();
                log.warn("房源已有活跃租约 - roomId: {}, leaseStatus: {}", roomId, leaseStatus);
                return RoomAvailabilityVo.occupied(leaseStatus.name());
            }
            
            log.info("房源可用 - roomId: {}", roomId);
            return RoomAvailabilityVo.available();
            
        } catch (Exception e) {
            log.error("检查房间可用性详情失败 - roomId: {}, error: {}", roomId, e.getMessage(), e);
            return new RoomAvailabilityVo(false, "ERROR", "系统错误", "检查房源可用性时发生错误，请稍后重试");
        }
    }

    @Override
    public List<DirectMoveInApplicationDetailVO> getApplicationsByLandlord(Long landlordId) {
        log.info("查询房东所有申请 - landlordId: {}", landlordId);
        return baseMapper.selectApplicationsByLandlord(landlordId);
    }

    @Override
    public Long getLeaseIdByApplicationId(Long applicationId) {
        log.info("根据申请ID查询租约ID - applicationId: {}", applicationId);
        
        // 查询租约表中application_id字段对应的租约
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getApplicationId, applicationId)
                   .eq(LeaseAgreement::getIsDeleted, 0)
                   .orderByDesc(LeaseAgreement::getCreateTime)
                   .last("LIMIT 1");
        
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectOne(queryWrapper);
        
        if (leaseAgreement != null) {
            log.info("找到对应租约 - applicationId: {}, leaseId: {}", applicationId, leaseAgreement.getId());
            return leaseAgreement.getId();
        } else {
            log.info("未找到对应租约 - applicationId: {}", applicationId);
            return null;
        }
    }

    @Override
    @Transactional
    public void cancelApplication(Long applicationId, Long userId) {
        log.info("用户取消申请 - applicationId: {}, userId: {}", applicationId, userId);
        
        try {
            // 1. 验证申请是否存在
            DirectMoveInApplication application = this.getById(applicationId);
            if (application == null) {
                log.warn("申请不存在 - applicationId: {}", applicationId);
                throw new RuntimeException("申请不存在");
            }
            
            // 2. 验证申请是否属于该用户
            if (!application.getUserId().equals(userId)) {
                log.warn("用户无权限操作此申请 - applicationId: {}, userId: {}, applicationUserId: {}", 
                        applicationId, userId, application.getUserId());
                throw new RuntimeException("无权限操作此申请");
            }
            
            // 3. 验证申请状态是否允许取消（只有待处理状态才能取消）
            if (!DirectMoveInStatus.PENDING.equals(application.getStatus())) {
                log.warn("申请状态不允许取消 - applicationId: {}, status: {}", applicationId, application.getStatus());
                throw new RuntimeException("只有待处理状态的申请才能取消");
            }
            
            // 4. 执行软删除（MyBatis-Plus会自动将is_deleted设为1）
            boolean deleted = this.removeById(applicationId);
            if (!deleted) {
                log.error("软删除申请失败 - applicationId: {}", applicationId);
                throw new RuntimeException("取消申请失败");
            }
            
            log.info("用户成功取消申请 - applicationId: {}, userId: {}", applicationId, userId);
            
        } catch (RuntimeException e) {
            log.error("取消申请失败 - applicationId: {}, userId: {}, error: {}", 
                     applicationId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("取消申请发生未知错误 - applicationId: {}, userId: {}", 
                     applicationId, userId, e);
            throw new RuntimeException("取消申请失败：" + e.getMessage());
        }
    }

    @Override
    public LandlordApplicationStatisticsVo getApplicationStatisticsByLandlord(Long landlordId) {
        log.info("获取房东申请统计信息 - landlordId: {}", landlordId);
        
        try {
            // 1. 查询总申请数
            LambdaQueryWrapper<DirectMoveInApplication> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(DirectMoveInApplication::getLandlordId, landlordId);
            long totalApplications = this.count(totalWrapper);
            
            // 2. 查询待处理申请数
            LambdaQueryWrapper<DirectMoveInApplication> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(DirectMoveInApplication::getLandlordId, landlordId)
                         .eq(DirectMoveInApplication::getStatus, DirectMoveInStatus.PENDING);
            long pendingApplications = this.count(pendingWrapper);
            
            // 3. 查询已通过申请数
            LambdaQueryWrapper<DirectMoveInApplication> approvedWrapper = new LambdaQueryWrapper<>();
            approvedWrapper.eq(DirectMoveInApplication::getLandlordId, landlordId)
                          .eq(DirectMoveInApplication::getStatus, DirectMoveInStatus.APPROVED);
            long approvedApplications = this.count(approvedWrapper);
            
            // 4. 查询已拒绝申请数
            LambdaQueryWrapper<DirectMoveInApplication> rejectedWrapper = new LambdaQueryWrapper<>();
            rejectedWrapper.eq(DirectMoveInApplication::getLandlordId, landlordId)
                          .eq(DirectMoveInApplication::getStatus, DirectMoveInStatus.REJECTED);
            long rejectedApplications = this.count(rejectedWrapper);
            
            // 5. 计算通过率
            double approvalRate = 0.0;
            long processedApplications = approvedApplications + rejectedApplications;
            if (processedApplications > 0) {
                approvalRate = (double) approvedApplications / processedApplications * 100;
                // 保留两位小数
                approvalRate = Math.round(approvalRate * 100.0) / 100.0;
            }
            
            // 6. 查询今日新申请数
            LambdaQueryWrapper<DirectMoveInApplication> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.eq(DirectMoveInApplication::getLandlordId, landlordId)
                       .apply("DATE(create_time) = CURDATE()");
            long todayApplications = this.count(todayWrapper);
            
            // 7. 查询本月新申请数
            LambdaQueryWrapper<DirectMoveInApplication> monthWrapper = new LambdaQueryWrapper<>();
            monthWrapper.eq(DirectMoveInApplication::getLandlordId, landlordId)
                       .apply("YEAR(create_time) = YEAR(NOW()) AND MONTH(create_time) = MONTH(NOW())");
            long monthApplications = this.count(monthWrapper);
            
            // 8. 构建统计结果
            LandlordApplicationStatisticsVo statistics = LandlordApplicationStatisticsVo.builder()
                    .totalApplications(totalApplications)
                    .pendingApplications(pendingApplications)
                    .approvedApplications(approvedApplications)
                    .rejectedApplications(rejectedApplications)
                    .approvalRate(approvalRate)
                    .todayApplications(todayApplications)
                    .monthApplications(monthApplications)
                    .build();
            
            log.info("房东申请统计信息 - landlordId: {}, 总数: {}, 待处理: {}, 已通过: {}, 已拒绝: {}, 通过率: {}%", 
                    landlordId, totalApplications, pendingApplications, approvedApplications, 
                    rejectedApplications, approvalRate);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取房东申请统计信息失败 - landlordId: {}, error: {}", landlordId, e.getMessage(), e);
            throw new RuntimeException("获取统计信息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Long generateLeaseAgreement(Long applicationId) {
        log.info("开始生成租约合同 - applicationId: {}", applicationId);
        
        try {
            // 1. 获取申请信息
            DirectMoveInApplication application = this.getById(applicationId);
            if (application == null) {
                throw new RuntimeException("申请不存在");
            }

            if (!DirectMoveInStatus.APPROVED.equals(application.getStatus())) {
                throw new RuntimeException("申请未通过，无法生成租约");
            }

            // 2. 检查是否已经生成过租约
            LambdaQueryWrapper<LeaseAgreement> existingLeaseWrapper = new LambdaQueryWrapper<>();
            existingLeaseWrapper.eq(LeaseAgreement::getApplicationId, applicationId)
                              .eq(LeaseAgreement::getIsDeleted, 0);
            
            LeaseAgreement existingLease = leaseAgreementMapper.selectOne(existingLeaseWrapper);
            if (existingLease != null) {
                log.warn("申请已生成租约，返回现有租约ID - applicationId: {}, leaseId: {}", 
                        applicationId, existingLease.getId());
                return existingLease.getId();
            }

            // 3. 验证房源可用性
            if (!validateRoomAvailability(application.getRoomId())) {
                throw new RuntimeException("房源不可用，无法生成租约");
            }

            // 4. 创建租约合同
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
            
            // 设置创建者ID
            if (application.getProcessorType() == 1) {
                // 官方人员：使用处理人ID，如果为空则使用默认值1
                leaseAgreement.setCreatorId(application.getProcessorId() != null ? application.getProcessorId() : 1L);
            } else {
                // 房东：使用处理人ID（房东ID）
                leaseAgreement.setCreatorId(application.getProcessorId());
            }
            
            leaseAgreement.setApplicationId(applicationId);
            leaseAgreement.setAutoGenerated(1); // 自动生成
            leaseAgreement.setAdditionalInfo("由直接入住申请自动生成");
            
            // 设置创建和更新时间
            Date now = new Date();
            leaseAgreement.setCreateTime(now);
            leaseAgreement.setUpdateTime(now);

            // 5. 保存租约
            int insertResult = leaseAgreementMapper.insert(leaseAgreement);
            if (insertResult <= 0) {
                throw new RuntimeException("保存租约失败");
            }

            log.info("成功生成租约合同 - applicationId: {}, leaseId: {}, 状态: 签约待确认", 
                    applicationId, leaseAgreement.getId());
            
            return leaseAgreement.getId();
            
        } catch (Exception e) {
            log.error("生成租约合同失败 - applicationId: {}, error: {}", applicationId, e.getMessage(), e);
            throw new RuntimeException("生成租约合同失败：" + e.getMessage());
        }
    }
}