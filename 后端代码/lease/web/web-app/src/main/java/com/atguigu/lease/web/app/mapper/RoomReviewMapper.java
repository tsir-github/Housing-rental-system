package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewStatusVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author system
 * @description 针对表【room_review(房源审核表)】的数据库操作Mapper - 移动端
 * @createDate 2024-01-05 00:00:00
 * @Entity com.atguigu.lease.model.entity.RoomReview
 */
public interface RoomReviewMapper extends BaseMapper<RoomReview> {

    /**
     * 根据房间ID查询最新的审核记录
     */
    RoomReview selectLatestByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据房东ID查询审核记录列表
     */
    List<RoomReview> selectByLandlordId(@Param("landlordId") Long landlordId);

    /**
     * 分页查询房东房源审核状态（关联公寓信息）
     */
    IPage<RoomReviewStatusVo> selectLandlordRoomReviewStatus(
            Page<RoomReviewStatusVo> page,
            @Param("landlordId") Long landlordId,
            @Param("reviewStatus") Integer reviewStatus
    );

}