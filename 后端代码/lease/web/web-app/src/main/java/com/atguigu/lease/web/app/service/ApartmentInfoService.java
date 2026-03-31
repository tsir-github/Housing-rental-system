package com.atguigu.lease.web.app.service;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service
 * @createDate 2023-07-26 11:12:39
 */
public interface ApartmentInfoService extends IService<ApartmentInfo> {
    ApartmentItemVo selectApartmentItemVoById(Long id);

    ApartmentDetailVo getApartmentDetailById(Long id);

    /**
     * 房东保存或更新公寓信息
     */
    void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo);

    /**
     * 房东分页查询公寓列表
     */
    IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo);

    /**
     * 房东根据ID获取公寓详细信息
     */
    ApartmentDetailVo getDetailById(Long id);

    /**
     * 房东删除公寓信息
     */
    void removeApartmentById(Long id);

    /**
     * 获取房东可选择的公寓列表（包括自己发布的和官方发布的）
     * @param landlordId 房东ID
     * @return 可选择的公寓列表
     */
    List<ApartmentItemVo> getAvailableApartmentsForLandlord(Long landlordId);

    /**
     * 获取公寓的杂费信息（房东权限验证）
     * @param apartmentId 公寓ID
     * @param landlordId 房东ID
     * @return 公寓杂费信息
     */
    Result<List<Map<String, Object>>> getApartmentFeesByIdForLandlord(Long apartmentId, Long landlordId);

    /**
     * 获取公寓详细信息用于编辑回显（包含权限验证）
     * @param apartmentId 公寓ID
     * @param landlordId 房东ID
     * @return 公寓详细信息
     */
    Result<ApartmentDetailVo> getApartmentDetailForEdit(Long apartmentId, Long landlordId);
}
