package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.app.vo.landlord.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 房东租约管理服务接口
 * 职责：处理房东租约相关的业务逻辑
 */
public interface LandlordLeaseService {

    /**
     * 分页查询房东租约列表
     * @param page 分页参数
     * @param landlordId 房东ID
     * @param queryVo 查询条件
     * @return 分页结果
     */
    IPage<LandlordLeaseVo> pageLandlordLeaseByQuery(Page<LandlordLeaseVo> page, Long landlordId, LandlordLeaseQueryVo queryVo);

    /**
     * 根据ID查询房东租约基本信息
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     * @return 租约基本信息
     */
    LandlordLeaseVo getLandlordLeaseById(Long leaseId, Long landlordId);

    /**
     * 根据ID查询房东租约详情
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     * @return 租约详情
     */
    LandlordLeaseDetailVo getLandlordLeaseDetailById(Long leaseId, Long landlordId);

    /**
     * 房东更新租约状态
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     * @param status 新状态
     * @param approved 是否同意（用于续约场景，true=同意续约延长日期，false=拒绝续约不延长日期）
     * @param reason 变更原因
     */
    void updateLandlordLeaseStatus(Long leaseId, Long landlordId, LeaseStatus status, Boolean approved, String reason);

    /**
     * 获取房东租约统计数据
     * @param landlordId 房东ID
     * @return 统计数据
     */
    LandlordLeaseStatisticsVo getLandlordLeaseStatistics(Long landlordId);

    /**
     * 获取房东即将到期的租约
     * @param landlordId 房东ID
     * @return 即将到期的租约列表
     */
    List<LandlordLeaseVo> getLandlordExpiringLeases(Long landlordId);

    /**
     * 房东创建新租约
     * @param createVo 租约创建信息
     * @param landlordId 房东ID
     * @return 租约ID
     */
    Long createLandlordLease(LandlordLeaseCreateVo createVo, Long landlordId);

    /**
     * 房东修改租约信息
     * @param leaseId 租约ID
     * @param updateVo 租约更新信息
     * @param landlordId 房东ID
     */
    void updateLandlordLease(Long leaseId, LandlordLeaseUpdateVo updateVo, Long landlordId);

    /**
     * 房东发送租约到期提醒
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     */
    void sendLandlordLeaseReminder(Long leaseId, Long landlordId);

    /**
     * 房东重发租约
     * @param leaseId 原租约ID
     * @param landlordId 房东ID
     * @return 新租约ID
     */
    Long resendLandlordLease(Long leaseId, Long landlordId);

    /**
     * 房东重发租约（带修改数据）
     * @param leaseId 原租约ID
     * @param createVo 修改后的租约数据
     * @param landlordId 房东ID
     * @return 新租约ID
     */
    Long resendLandlordLeaseWithData(Long leaseId, LandlordLeaseCreateVo createVo, Long landlordId);

    /**
     * 房东删除租约
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     */
    void removeLandlordLease(Long leaseId, Long landlordId);

    /**
     * 验证房东对租约的操作权限
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     * @return 租约信息
     */
    LeaseAgreement validateLandlordLeasePermission(Long leaseId, Long landlordId);
}