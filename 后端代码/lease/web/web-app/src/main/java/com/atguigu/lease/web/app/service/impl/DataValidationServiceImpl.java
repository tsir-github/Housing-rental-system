package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.*;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.DataValidationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 数据校验服务实现类
 */
@Slf4j
@Service
public class DataValidationServiceImpl implements DataValidationService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private DirectMoveInApplicationMapper directMoveInApplicationMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private LandlordPermissionMapper landlordPermissionMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 常用正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    @Override
    public void validateRoomPublication(RoomInfo roomInfo) {
        log.info("校验房源发布数据，房源ID: {}", roomInfo.getId());
        
        // 1. 基础信息校验
        if (!StringUtils.hasText(roomInfo.getRoomNumber())) {
            throw new IllegalArgumentException("房间号不能为空");
        }
        
        if (roomInfo.getRent() == null || roomInfo.getRent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("租金必须大于0");
        }
        
        if (roomInfo.getApartmentId() == null) {
            throw new IllegalArgumentException("公寓ID不能为空");
        }

        // 2. 房间号唯一性校验
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomInfo::getApartmentId, roomInfo.getApartmentId())
                   .eq(RoomInfo::getRoomNumber, roomInfo.getRoomNumber())
                   .ne(roomInfo.getId() != null, RoomInfo::getId, roomInfo.getId());
        
        long count = roomInfoMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("该公寓下房间号已存在");
        }

        // 3. 发布者权限校验
        if (PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
            if (roomInfo.getPublisherId() == null) {
                throw new IllegalArgumentException("房东ID不能为空");
            }
            
            // 校验房东是否有发布权限
            if (!validateUserPermission(roomInfo.getPublisherId(), "manage_rooms", null)) {
                throw new IllegalArgumentException("房东没有房源管理权限");
            }
        }

        // 4. 业务规则校验
        validateBusinessRules("ROOM_PUBLICATION", roomInfo);
        
        log.info("房源发布数据校验通过，房源ID: {}", roomInfo.getId());
    }

    @Override
    public void validateDirectMoveInApplication(DirectMoveInApplication application) {
        log.info("校验直接入住申请数据，申请ID: {}", application.getId());
        
        // 1. 基础信息校验
        if (!StringUtils.hasText(application.getName())) {
            throw new IllegalArgumentException("申请人姓名不能为空");
        }
        
        if (!StringUtils.hasText(application.getPhone())) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        
        if (!PHONE_PATTERN.matcher(application.getPhone()).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        
        if (!StringUtils.hasText(application.getIdentificationNumber())) {
            throw new IllegalArgumentException("身份证号不能为空");
        }
        
        if (!ID_CARD_PATTERN.matcher(application.getIdentificationNumber()).matches()) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }

        // 2. 租期校验
        if (application.getLeaseStartDate() == null || application.getLeaseEndDate() == null) {
            throw new IllegalArgumentException("租期开始和结束日期不能为空");
        }
        
        LocalDate startDate = application.getLeaseStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = application.getLeaseEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("租期开始日期必须早于结束日期");
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween < 30) {
            throw new IllegalArgumentException("租期不能少于30天");
        }

        // 3. 租金校验
        if (application.getRent() == null || application.getRent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("租金必须大于0");
        }
        
        if (application.getDeposit() == null || application.getDeposit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("押金不能为负数");
        }

        // 4. 房源可用性校验
        if (!validateRoomAvailability(application.getRoomId())) {
            throw new IllegalArgumentException("房源不可用");
        }

        // 5. 重复申请校验
        LambdaQueryWrapper<DirectMoveInApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DirectMoveInApplication::getUserId, application.getUserId())
                   .eq(DirectMoveInApplication::getRoomId, application.getRoomId())
                   .eq(DirectMoveInApplication::getStatus, DirectMoveInStatus.PENDING)
                   .ne(application.getId() != null, DirectMoveInApplication::getId, application.getId());
        
        long count = directMoveInApplicationMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("您已对该房源提交过申请，请勿重复提交");
        }

        log.info("直接入住申请数据校验通过，申请ID: {}", application.getId());
    }

    @Override
    public void validateLeaseAgreement(LeaseAgreement lease) {
        log.info("校验租约合同数据，租约ID: {}", lease.getId());
        
        // 1. 基础信息校验
        if (!StringUtils.hasText(lease.getName())) {
            throw new IllegalArgumentException("租客姓名不能为空");
        }
        
        if (!StringUtils.hasText(lease.getPhone())) {
            throw new IllegalArgumentException("租客手机号不能为空");
        }
        
        if (!PHONE_PATTERN.matcher(lease.getPhone()).matches()) {
            throw new IllegalArgumentException("租客手机号格式不正确");
        }

        // 2. 租期校验
        if (lease.getLeaseStartDate() == null || lease.getLeaseEndDate() == null) {
            throw new IllegalArgumentException("租期开始和结束日期不能为空");
        }

        // 3. 租金校验
        if (lease.getRent() == null || lease.getRent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("租金必须大于0");
        }

        // 4. 房源校验
        if (lease.getRoomId() == null) {
            throw new IllegalArgumentException("房间ID不能为空");
        }
        
        RoomInfo roomInfo = roomInfoMapper.selectById(lease.getRoomId());
        if (roomInfo == null) {
            throw new IllegalArgumentException("房间不存在");
        }

        // 5. 重复租约校验
        if (LeaseStatus.SIGNING.equals(lease.getStatus()) || LeaseStatus.SIGNED.equals(lease.getStatus())) {
            LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LeaseAgreement::getRoomId, lease.getRoomId())
                       .in(LeaseAgreement::getStatus, LeaseStatus.SIGNING, LeaseStatus.SIGNED)
                       .ne(lease.getId() != null, LeaseAgreement::getId, lease.getId());
            
            long count = leaseAgreementMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new IllegalArgumentException("该房间已有有效租约");
            }
        }

        log.info("租约合同数据校验通过，租约ID: {}", lease.getId());
    }

    @Override
    public boolean validateRoomAvailability(Long roomId) {
        log.info("校验房源可用性，房源ID: {}", roomId);
        
        // 1. 房源存在性校验
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null) {
            log.warn("房源不存在，房源ID: {}", roomId);
            return false;
        }

        // 2. 房源状态校验
        if (!ReleaseStatus.RELEASED.equals(roomInfo.getIsRelease())) {
            log.warn("房源未发布，房源ID: {}", roomId);
            return false;
        }

        // 3. 审核状态校验
        if (!ReviewStatus.APPROVED.equals(roomInfo.getReviewStatus())) {
            log.warn("房源未通过审核，房源ID: {}", roomId);
            return false;
        }

        // 4. 租约状态校验 - 检查是否有占用房源的租约
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getRoomId, roomId)
                   .in(LeaseAgreement::getStatus, 
                       LeaseStatus.SIGNING,    // 1-签约待确认
                       LeaseStatus.SIGNED,     // 2-已签约
                       LeaseStatus.RENEWING)   // 7-续约待确认
                   .eq(LeaseAgreement::getIsDeleted, 0);
        
        long leaseCount = leaseAgreementMapper.selectCount(queryWrapper);
        if (leaseCount > 0) {
            log.warn("房源已有有效租约，房源ID: {}", roomId);
            return false;
        }

        log.info("房源可用性校验通过，房源ID: {}", roomId);
        return true;
    }

    @Override
    public boolean validateUserPermission(Long userId, String operation, Long resourceId) {
        log.info("校验用户权限，用户ID: {}, 操作: {}, 资源ID: {}", userId, operation, resourceId);
        
        try {
            // 1. 用户存在性校验
            UserInfo user = userInfoMapper.selectById(userId);
            if (user == null) {
                log.warn("用户不存在，用户ID: {}", userId);
                return false;
            }

            // 2. 用户状态校验
            if (user.getStatus() != BaseStatus.ENABLE) {
                log.warn("用户状态异常，用户ID: {}, 状态: {}", userId, user.getStatus());
                return false;
            }

            // 3. 房东权限校验
            boolean hasPermission = landlordPermissionMapper.hasPermission(userId, operation);
            if (!hasPermission) {
                log.warn("用户没有操作权限，用户ID: {}, 操作: {}", userId, operation);
                return false;
            }

            // 4. 资源权限校验（如果需要）
            if (resourceId != null) {
                // 这里可以添加具体的资源权限校验逻辑
                // 比如校验房东是否拥有特定房源的管理权限
            }

            log.info("用户权限校验通过，用户ID: {}, 操作: {}", userId, operation);
            return true;

        } catch (Exception e) {
            log.error("用户权限校验失败", e);
            return false;
        }
    }

    @Override
    public void validateBusinessRules(String ruleType, Object data) {
        log.info("校验业务规则，规则类型: {}", ruleType);
        
        switch (ruleType) {
            case "ROOM_PUBLICATION":
                validateRoomPublicationRules((RoomInfo) data);
                break;
            case "APPLICATION_SUBMISSION":
                validateApplicationSubmissionRules((DirectMoveInApplication) data);
                break;
            case "LEASE_GENERATION":
                validateLeaseGenerationRules((LeaseAgreement) data);
                break;
            default:
                log.warn("未知的业务规则类型: {}", ruleType);
        }
    }

    @Override
    public void validateDataIntegrity(String entityType, Long entityId) {
        log.info("校验数据完整性，实体类型: {}, 实体ID: {}", entityType, entityId);
        
        switch (entityType) {
            case "ROOM":
                validateRoomDataIntegrity(entityId);
                break;
            case "LEASE":
                validateLeaseDataIntegrity(entityId);
                break;
            case "APPLICATION":
                validateApplicationDataIntegrity(entityId);
                break;
            default:
                log.warn("未知的实体类型: {}", entityType);
        }
    }

    @Override
    public boolean validateConcurrentOperation(String operationType, Long resourceId, String lockKey) {
        log.info("校验并发操作，操作类型: {}, 资源ID: {}, 锁键: {}", operationType, resourceId, lockKey);
        
        try {
            // 使用Redis分布式锁防止并发操作
            String key = String.format("lock:%s:%s:%s", operationType, resourceId, lockKey);
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(key, "locked", 30, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(lockAcquired)) {
                log.info("并发操作校验通过，获取锁成功: {}", key);
                return true;
            } else {
                log.warn("并发操作校验失败，获取锁失败: {}", key);
                return false;
            }
        } catch (Exception e) {
            log.error("并发操作校验异常", e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    private void validateRoomPublicationRules(RoomInfo roomInfo) {
        // 房源发布业务规则校验
        if (roomInfo.getRent().compareTo(new BigDecimal("50000")) > 0) {
            throw new IllegalArgumentException("租金不能超过50000元");
        }
        
        // 可以添加更多业务规则
    }

    private void validateApplicationSubmissionRules(DirectMoveInApplication application) {
        // 申请提交业务规则校验
        LocalDate today = LocalDate.now();
        LocalDate startDate = application.getLeaseStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        if (ChronoUnit.DAYS.between(today, startDate) > 90) {
            throw new IllegalArgumentException("租期开始日期不能超过90天后");
        }
    }

    private void validateLeaseGenerationRules(LeaseAgreement lease) {
        // 租约生成业务规则校验
        // 可以添加具体的业务规则
    }

    private void validateRoomDataIntegrity(Long roomId) {
        // 房源数据完整性校验
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null) {
            throw new IllegalArgumentException("房源数据不存在");
        }
        
        // 检查关联数据的完整性
        // 比如公寓信息、配套设施等
    }

    private void validateLeaseDataIntegrity(Long leaseId) {
        // 租约数据完整性校验
        LeaseAgreement lease = leaseAgreementMapper.selectById(leaseId);
        if (lease == null) {
            throw new IllegalArgumentException("租约数据不存在");
        }
        
        // 检查关联数据的完整性
    }

    private void validateApplicationDataIntegrity(Long applicationId) {
        // 申请数据完整性校验
        DirectMoveInApplication application = directMoveInApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("申请数据不存在");
        }
        
        // 检查关联数据的完整性
    }
}