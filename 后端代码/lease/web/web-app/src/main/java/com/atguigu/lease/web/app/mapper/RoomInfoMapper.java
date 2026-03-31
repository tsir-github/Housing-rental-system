package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewDetailVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.atguigu.lease.model.entity.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo);

    BigDecimal selectMinRentByApartmentId(Long id);

    IPage<RoomItemVo> pageItemByApartmentId(Page<RoomItemVo> page, Long id);

    /**
     * 根据房源ID查询房源详情
     * @param roomId 房源ID
     * @return 房源详情
     */
    RoomDetailVo selectRoomDetailById(Long roomId);

    /**
     * 根据房源ID查询房源属性列表
     * @param roomId 房源ID
     * @return 属性列表
     */
    List<AttrValueVo> selectAttrValuesByRoomId(Long roomId);

    /**
     * 根据房源ID查询审核详情
     * @param roomId 房源ID
     * @return 审核详情
     */
    RoomReviewDetailVo selectRoomReviewByRoomId(Long roomId);

    // 房东房源管理相关方法
    void deleteRoomAttrValueByRoomId(@Param("roomId") Long roomId);
    
    void deleteRoomFacilityByRoomId(@Param("roomId") Long roomId);
    
    void deleteRoomLabelByRoomId(@Param("roomId") Long roomId);
    
    void deleteRoomPaymentTypeByRoomId(@Param("roomId") Long roomId);
    
    void deleteRoomLeaseTermByRoomId(@Param("roomId") Long roomId);
    
    void insertRoomGraphBatch(@Param("roomId") Long roomId, @Param("graphList") List<GraphVo> graphList);
    
    void insertRoomAttrValueBatch(@Param("roomId") Long roomId, @Param("attrValueIds") List<Long> attrValueIds);
    
    void insertRoomFacilityBatch(@Param("roomId") Long roomId, @Param("facilityIds") List<Long> facilityIds);
    
    void insertRoomLabelBatch(@Param("roomId") Long roomId, @Param("labelIds") List<Long> labelIds);
    
    void insertRoomPaymentTypeBatch(@Param("roomId") Long roomId, @Param("paymentTypeIds") List<Long> paymentTypeIds);
    
    void insertRoomLeaseTermBatch(@Param("roomId") Long roomId, @Param("leaseTermIds") List<Long> leaseTermIds);
}