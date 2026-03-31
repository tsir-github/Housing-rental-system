package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.web.admin.vo.application.DirectMoveInApplicationDetailVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author system
 * @description 针对表【direct_move_in_application(直接入住申请表)】的数据库操作Service
 * @createDate 2024-01-05 00:00:00
 */
public interface DirectMoveInApplicationService extends IService<DirectMoveInApplication> {

    /**
     * 分页查询待处理申请（包含关联信息）
     */
    IPage<DirectMoveInApplicationDetailVO> getPendingApplicationsWithDetails(
            Page<DirectMoveInApplicationDetailVO> page,
            Long processorId, Integer processorType, Integer publisherType);

    /**
     * 分页查询申请历史（包含关联信息）
     */
    IPage<DirectMoveInApplicationDetailVO> getApplicationHistoryWithDetails(
            Page<DirectMoveInApplicationDetailVO> page,
            Long userId, Integer status, Long processorId, Integer processorType, Integer publisherType);

    /**
     * 根据ID查询申请详情（包含关联信息）
     */
    DirectMoveInApplicationDetailVO getApplicationDetailById(Long id);

    /**
     * 处理直接入住申请
     */
    void processApplication(Long applicationId, Long processorId, Integer processorType, 
                          boolean approved, String rejectionReason);

    /**
     * 自动生成租约合同
     */
    Long generateLeaseAgreement(Long applicationId);

    /**
     * 检查房间是否可用
     */
    boolean isRoomAvailable(Long roomId);
}