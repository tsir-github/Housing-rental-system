package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationDetailVO;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationSubmitVO;
import com.atguigu.lease.web.app.vo.landlord.LandlordApplicationStatisticsVo;
import com.atguigu.lease.web.app.vo.room.RoomAvailabilityVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 直接入住申请服务接口
 */
public interface DirectMoveInApplicationService extends IService<DirectMoveInApplication> {

    /**
     * 提交直接入住申请
     * @param submitVO 申请信息
     * @return 申请ID
     */
    Long submitApplication(DirectMoveInApplicationSubmitVO submitVO);

    /**
     * 获取用户的申请列表
     * @param userId 用户ID
     * @return 申请列表
     */
    List<DirectMoveInApplicationDetailVO> getApplicationsByUserId(Long userId);

    /**
     * 获取房东的待处理申请列表
     * @param landlordId 房东ID
     * @return 申请列表
     */
    List<DirectMoveInApplicationDetailVO> getPendingApplicationsByLandlord(Long landlordId);

    /**
     * 处理申请（确认或拒绝）
     * @param applicationId 申请ID
     * @param processorId 处理人ID
     * @param processorType 处理人类型：1-官方，2-房东
     * @param approved 是否确认
     * @param rejectionReason 拒绝原因（如果拒绝）
     * @return 是否处理成功
     */
    boolean processApplication(Long applicationId, Long processorId, Integer processorType, 
                             boolean approved, String rejectionReason);

    /**
     * 获取申请详情
     * @param applicationId 申请ID
     * @return 申请详情
     */
    DirectMoveInApplicationDetailVO getApplicationDetail(Long applicationId);

    /**
     * 验证房源可用性（简单版本）
     * @param roomId 房间ID
     * @return 是否可用
     */
    boolean validateRoomAvailability(Long roomId);

    /**
     * 检查房源可用性（详细版本）
     * @param roomId 房间ID
     * @return 详细的可用性信息
     */
    RoomAvailabilityVo checkRoomAvailability(Long roomId);

    /**
     * 获取房东的所有申请（包括所有状态）
     * @param landlordId 房东ID
     * @return 申请列表
     */
    List<DirectMoveInApplicationDetailVO> getApplicationsByLandlord(Long landlordId);

    /**
     * 根据申请ID获取对应的租约ID
     * @param applicationId 申请ID
     * @return 租约ID，如果没有对应租约则返回null
     */
    Long getLeaseIdByApplicationId(Long applicationId);

    /**
     * 取消直接入住申请（软删除）
     * @param applicationId 申请ID
     * @param userId 用户ID
     * @throws RuntimeException 如果申请不存在、无权限或状态不允许取消
     */
    void cancelApplication(Long applicationId, Long userId);

    /**
     * 获取房东申请统计信息
     * @param landlordId 房东ID
     * @return 统计信息
     */
    LandlordApplicationStatisticsVo getApplicationStatisticsByLandlord(Long landlordId);

    /**
     * 生成租约合同（基于直接入住申请）
     * @param applicationId 申请ID
     * @return 租约ID
     */
    Long generateLeaseAgreement(Long applicationId);
}