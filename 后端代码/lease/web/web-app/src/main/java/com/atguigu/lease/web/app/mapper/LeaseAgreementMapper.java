package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.atguigu.lease.web.app.vo.lease.LeaseAgreementVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.atguigu.lease.model.entity.LeaseAgreement
*/
public interface LeaseAgreementMapper extends BaseMapper<LeaseAgreement> {

    List<AgreementItemVo> listItemByPhone(String phone);

    /**
     * 查询房东的租约列表（分页）
     */
    IPage<LeaseAgreementVo> selectLandlordLeases(Page<LeaseAgreementVo> page, 
                                               @Param("landlordId") Long landlordId, 
                                               @Param("status") Integer status);

    /**
     * 计算房东的总租金收入
     */
    BigDecimal calculateLandlordTotalRent(@Param("landlordId") Long landlordId, 
                                        @Param("startDate") String startDate, 
                                        @Param("endDate") String endDate);

    /**
     * 计算房东的总押金收入
     */
    BigDecimal calculateLandlordTotalDeposit(@Param("landlordId") Long landlordId, 
                                           @Param("startDate") String startDate, 
                                           @Param("endDate") String endDate);

    /**
     * 统计房东的活跃租约数量
     */
    long countActiveLandlordLeases(@Param("landlordId") Long landlordId);
}




