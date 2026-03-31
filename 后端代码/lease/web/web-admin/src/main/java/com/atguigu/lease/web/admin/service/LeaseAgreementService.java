package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.atguigu.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface LeaseAgreementService extends IService<LeaseAgreement> {

    IPage<AgreementVo> pageAgreementByQuery(Page<AgreementVo> page, AgreementQueryVo queryVo);

    AgreementVo getAgreementById(Long id);

    /**
     * 获取所有租约列表（官方后台统一查看）
     * 支持按创建者类型、来源类型筛选
     */
    IPage<AgreementVo> getAllLeases(Page<AgreementVo> page, AgreementQueryVo queryVo);

    /**
     * 更新租约状态
     * @param leaseId 租约ID
     * @param newStatus 新状态
     * @param reason 变更原因
     */
    void updateLeaseStatus(Long leaseId, String newStatus, String reason);

    /**
     * 检查即将到期的租约
     * @return 即将到期的租约列表
     */
    List<LeaseAgreement> checkExpiringLeases();

    /**
     * 发送租约到期提醒
     * @param lease 租约信息
     */
    void sendExpirationNotification(LeaseAgreement lease);

    /**
     * 记录租约状态变更日志
     * @param leaseId 租约ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param reason 变更原因
     */
    void recordLeaseStatusChange(Long leaseId, String oldStatus, String newStatus, String reason);

    /**
     * 获取租约统计数据
     * @return 统计信息
     */
    Object getLeaseStatistics();

    /**
     * 支持租约修改和重新发送
     * @param leaseId 租约ID
     * @param updatedLease 更新的租约信息
     */
    void updateAndResendLease(Long leaseId, LeaseAgreement updatedLease);

    /**
     * 支持租约终止流程（退租）
     * @param leaseId 租约ID
     * @param terminationReason 终止原因
     */
    void terminateLease(Long leaseId, String terminationReason);

    /**
     * 保存或更新租约信息（增强版）
     * 处理手动创建租约的业务逻辑，自动设置相关字段
     * @param leaseAgreement 租约信息
     * @return 操作结果
     */
    com.atguigu.lease.common.result.Result saveOrUpdateLease(LeaseAgreement leaseAgreement);
}
