package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.web.app.vo.landlord.LandlordLeaseDetailVo;
import com.atguigu.lease.web.app.vo.landlord.LandlordLeaseQueryVo;
import com.atguigu.lease.web.app.vo.landlord.LandlordLeaseStatisticsVo;
import com.atguigu.lease.web.app.vo.landlord.LandlordLeaseVo;
import com.atguigu.lease.web.app.vo.landlord.SourceStatVo;
import com.atguigu.lease.web.app.vo.landlord.StatusStatVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 房东租约管理Mapper
 * 职责：处理房东租约相关的数据库操作
 */
public interface LandlordLeaseMapper extends BaseMapper<LeaseAgreement> {

    /**
     * 分页查询房东租约列表
     * @param page 分页参数
     * @param landlordId 房东ID
     * @param queryVo 查询条件
     * @return 分页结果
     */
    IPage<LandlordLeaseVo> pageLandlordLeaseByQuery(Page<LandlordLeaseVo> page, 
                                                   @Param("landlordId") Long landlordId, 
                                                   @Param("queryVo") LandlordLeaseQueryVo queryVo);

    /**
     * 根据ID查询房东租约详情
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     * @return 租约详情
     */
    LandlordLeaseDetailVo selectLandlordLeaseDetailById(@Param("leaseId") Long leaseId, 
                                                       @Param("landlordId") Long landlordId);

    /**
     * 根据ID查询房东租约基本信息
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     * @return 租约基本信息
     */
    LandlordLeaseVo selectLandlordLeaseById(@Param("leaseId") Long leaseId, 
                                           @Param("landlordId") Long landlordId);

    /**
     * 获取房东租约统计数据
     * @param landlordId 房东ID
     * @return 统计数据
     */
    LandlordLeaseStatisticsVo selectLandlordLeaseStatistics(@Param("landlordId") Long landlordId);

    /**
     * 获取房东租约状态统计
     * @param landlordId 房东ID
     * @return 状态统计列表
     */
    List<StatusStatVo> selectLandlordLeaseStatusStats(@Param("landlordId") Long landlordId);

    /**
     * 获取房东租约来源统计
     * @param landlordId 房东ID
     * @return 来源统计列表
     */
    List<SourceStatVo> selectLandlordLeaseSourceStats(@Param("landlordId") Long landlordId);

    /**
     * 获取房东即将到期的租约
     * @param landlordId 房东ID
     * @param expirationDate 到期日期（30天后）
     * @return 即将到期的租约列表
     */
    List<LandlordLeaseVo> selectLandlordExpiringLeases(@Param("landlordId") Long landlordId, 
                                                      @Param("expirationDate") Date expirationDate);

    /**
     * 验证房东对租约的权限
     * @param leaseId 租约ID
     * @param landlordId 房东ID
     * @return 是否有权限
     */
    Integer validateLandlordLeasePermission(@Param("leaseId") Long leaseId, 
                                          @Param("landlordId") Long landlordId);
}