package com.atguigu.lease.web.app.service;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.atguigu.lease.web.app.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface RoomInfoService extends IService<RoomInfo> {
    IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo);

    RoomDetailVo getDetailById(Long id);

    IPage<RoomItemVo> pageItemByApartmentId(Page<RoomItemVo> page, Long id);

    /**
     * 保存或更新房间信息（房东发布房源）
     */
    void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo);

    /**
     * 删除房间信息（房东删除房源）
     */
    void removeRoomById(Long roomId);

    /**
     * 房东获取房源详情（包含权限验证）
     * @param roomId 房源ID
     * @param landlordId 房东ID
     * @return 房源详情
     */
    Result<RoomDetailVo> getLandlordRoomDetail(Long roomId, Long landlordId);

    /**
     * 根据房间ID获取公寓信息
     * @param roomId 房间ID
     * @return 公寓信息
     */
    ApartmentInfo getApartmentInfoByRoomId(Long roomId);

    /**
     * 清除指定房源的详情缓存
     * @param roomId 房源ID
     */
    void clearRoomDetailCache(Long roomId);

    /**
     * 清除所有房源详情缓存
     */
    void clearAllRoomDetailCache();

    /**
     * 验证房东对房源的编辑权限
     * @param roomId 房源ID
     * @param landlordId 房东ID
     * @return 是否有权限
     */
    boolean validateRoomEditPermission(Long roomId, Long landlordId);

    /**
     * 验证房东对公寓的访问权限
     * @param apartmentId 公寓ID
     * @param landlordId 房东ID
     * @return 是否有权限
     */
    boolean validateApartmentAccessPermission(Long apartmentId, Long landlordId);

    /**
     * 验证房东对属性的访问权限
     * @param attrKeyId 属性键ID
     * @param landlordId 房东ID
     * @return 是否有权限
     */
    boolean validateAttributeAccessPermission(Long attrKeyId, Long landlordId);

    /**
     * 验证房东对配套设施的访问权限
     * @param facilityId 配套设施ID
     * @param landlordId 房东ID
     * @return 是否有权限
     */
    boolean validateFacilityAccessPermission(Long facilityId, Long landlordId);

    /**
     * 验证房东对标签的访问权限
     * @param labelId 标签ID
     * @param landlordId 房东ID
     * @return 是否有权限
     */
    boolean validateLabelAccessPermission(Long labelId, Long landlordId);

    /**
     * 验证房东对支付方式的访问权限
     * @param paymentTypeId 支付方式ID
     * @param landlordId 房东ID
     * @return 是否有权限
     */
    boolean validatePaymentTypeAccessPermission(Long paymentTypeId, Long landlordId);

    /**
     * 房东修改房源发布状态
     * @param roomId 房源ID
     * @param status 发布状态
     * @param landlordId 房东ID
     * @return 操作结果
     */
    Result updateReleaseStatus(Long roomId, com.atguigu.lease.model.enums.ReleaseStatus status, Long landlordId);

    /**
     * 房东重新提交审核
     * @param roomId 房源ID
     * @param landlordId 房东ID
     * @return 操作结果
     */
    Result resubmitForReview(Long roomId, Long landlordId);
}
