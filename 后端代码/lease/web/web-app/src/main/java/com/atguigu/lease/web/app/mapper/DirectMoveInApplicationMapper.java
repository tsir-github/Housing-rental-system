package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.DirectMoveInApplication;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationDetailVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 移动端直接入住申请Mapper接口
 */
public interface DirectMoveInApplicationMapper extends BaseMapper<DirectMoveInApplication> {

    /**
     * 查询房东的待处理申请列表（包含关联信息）
     */
    List<DirectMoveInApplicationDetailVO> selectPendingApplicationsByLandlord(@Param("landlordId") Long landlordId);

    /**
     * 查询房东的所有申请列表（包含关联信息）
     */
    List<DirectMoveInApplicationDetailVO> selectApplicationsByLandlord(@Param("landlordId") Long landlordId);

    /**
     * 根据ID查询申请详情（包含关联信息）
     */
    DirectMoveInApplicationDetailVO selectApplicationDetailById(@Param("id") Long id);

    /**
     * 查询用户的申请列表（包含关联信息）
     */
    List<DirectMoveInApplicationDetailVO> selectApplicationsByUserId(@Param("userId") Long userId);

    /**
     * 统计房东的申请数量
     */
    Long countLandlordApplications(@Param("landlordId") Long landlordId);
}