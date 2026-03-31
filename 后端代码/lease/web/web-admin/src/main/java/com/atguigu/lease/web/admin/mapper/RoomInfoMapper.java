package com.atguigu.lease.web.admin.mapper;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.atguigu.lease.model.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomItemVo> pageRoomItemByQuery(IPage<RoomItemVo> page, RoomQueryVo queryVo);

    /**
     * 根据发布者类型查询房源列表
     */
    List<RoomInfo> selectByPublisherType(@Param("publisherType") Integer publisherType);

    /**
     * 根据发布者ID查询房源列表
     */
    List<RoomInfo> selectByPublisherId(@Param("publisherId") Long publisherId);

    /**
     * 根据审核状态查询房源列表
     */
    List<RoomInfo> selectByReviewStatus(@Param("reviewStatus") Integer reviewStatus);

    /**
     * 查询待审核房源列表
     */
    List<RoomInfo> selectPendingReview();

    /**
     * 根据房东ID查询房源列表
     */
    List<RoomInfo> selectByLandlordId(@Param("landlordId") Long landlordId);
}




