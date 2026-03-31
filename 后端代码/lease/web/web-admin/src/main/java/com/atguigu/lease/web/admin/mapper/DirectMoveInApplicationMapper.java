package com.atguigu.lease.web.admin.mapper;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.web.admin.vo.application.DirectMoveInApplicationDetailVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author system
 * @description 针对表【direct_move_in_application(直接入住申请表)】的数据库操作Mapper
 * @createDate 2024-01-05 00:00:00
 * @Entity com.atguigu.lease.model.entity.DirectMoveInApplication
 */
public interface DirectMoveInApplicationMapper extends BaseMapper<DirectMoveInApplication> {

    /**
     * 分页查询待处理申请（包含关联信息）
     */
    IPage<DirectMoveInApplicationDetailVO> selectPendingApplicationsWithDetails(
            Page<DirectMoveInApplicationDetailVO> page,
            @Param("processorId") Long processorId,
            @Param("processorType") Integer processorType,
            @Param("publisherType") Integer publisherType);

    /**
     * 分页查询申请历史（包含关联信息）
     */
    IPage<DirectMoveInApplicationDetailVO> selectApplicationHistoryWithDetails(
            Page<DirectMoveInApplicationDetailVO> page,
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("processorId") Long processorId,
            @Param("processorType") Integer processorType,
            @Param("publisherType") Integer publisherType);

    /**
     * 根据ID查询申请详情（包含关联信息）
     */
    DirectMoveInApplicationDetailVO selectApplicationDetailById(@Param("id") Long id);

    /**
     * 根据处理人ID和类型查询申请列表
     */
    List<DirectMoveInApplication> selectByProcessorIdAndType(@Param("processorId") Long processorId, 
                                                           @Param("processorType") Integer processorType);

    /**
     * 根据房东ID查询申请列表
     */
    List<DirectMoveInApplication> selectByLandlordId(@Param("landlordId") Long landlordId);

    /**
     * 根据租客ID查询申请列表
     */
    List<DirectMoveInApplication> selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询待处理申请
     */
    IPage<DirectMoveInApplication> selectPendingApplications(IPage<DirectMoveInApplication> page);

    /**
     * 统计申请数量按状态分组
     */
    List<Object> selectApplicationStatistics();

}