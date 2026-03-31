package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.entity.ViewAppointment;

/**
 * 事务管理服务接口
 * 处理复杂业务流程的事务一致性
 */
public interface TransactionManagementService {

    /**
     * 申请确认事务
     * 确认申请 -> 生成合同 -> 更新房间状态 -> 发送通知
     */
    LeaseAgreement processApplicationConfirmation(Long applicationId, Long processorId, Integer processorType);

    /**
     * 合同签署事务
     * 签署合同 -> 更新房间状态 -> 更新申请状态 -> 发送通知
     */
    void processLeaseAgreementSigning(Long leaseId, String tenantSignature);

    /**
     * 房源发布事务
     * 发布房源 -> 创建审核记录 -> 发送通知
     */
    void processRoomPublication(RoomInfo roomInfo);

    /**
     * 房源审核事务
     * 审核房源 -> 更新房源状态 -> 更新审核记录 -> 发送通知
     */
    void processRoomReviewDecision(Long roomId, Long reviewerId, boolean approved, String comments, Double complianceScore);

    /**
     * 预约完成事务
     * 完成预约 -> 更新预约状态 -> 可选生成合同
     */
    void processAppointmentCompletion(Long appointmentId, boolean generateLease);

    /**
     * 退租事务
     * 退租申请 -> 更新合同状态 -> 释放房间 -> 处理押金 -> 发送通知
     */
    void processLeaseTermination(Long leaseId, String terminationReason);

    /**
     * 房间状态同步
     * 确保房间状态与相关业务实体状态保持一致
     */
    void syncRoomStatus(Long roomId);

    /**
     * 数据一致性检查
     * 检查并修复数据不一致问题
     */
    void checkAndFixDataConsistency();

    /**
     * 补偿事务处理
     * 当事务失败时执行补偿操作
     */
    void executeCompensation(String transactionType, Long entityId, String failureReason);
}