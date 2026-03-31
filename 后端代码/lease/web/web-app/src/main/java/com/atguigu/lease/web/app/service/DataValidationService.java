package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.RoomInfo;

/**
 * 数据校验服务接口
 * 提供关键业务操作的数据校验
 */
public interface DataValidationService {

    /**
     * 校验房源发布数据
     */
    void validateRoomPublication(RoomInfo roomInfo);

    /**
     * 校验直接入住申请数据
     */
    void validateDirectMoveInApplication(DirectMoveInApplication application);

    /**
     * 校验租约合同数据
     */
    void validateLeaseAgreement(LeaseAgreement lease);

    /**
     * 校验房源可用性
     */
    boolean validateRoomAvailability(Long roomId);

    /**
     * 校验用户权限
     */
    boolean validateUserPermission(Long userId, String operation, Long resourceId);

    /**
     * 校验业务规则
     */
    void validateBusinessRules(String ruleType, Object data);

    /**
     * 校验数据完整性
     */
    void validateDataIntegrity(String entityType, Long entityId);

    /**
     * 校验并发操作
     */
    boolean validateConcurrentOperation(String operationType, Long resourceId, String lockKey);
}