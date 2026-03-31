package com.atguigu.lease.web.admin.mapper;

import com.atguigu.lease.model.entity.RoomReview;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author liubo
 * @description 针对表【room_review(房源审核表)】的数据库操作Mapper
 * @createDate 2023-07-24 15:48:00
 * @Entity com.atguigu.lease.model.entity.RoomReview
 */
public interface RoomReviewMapper extends BaseMapper<RoomReview> {

    /**
     * 根据房间ID获取最新审核记录
     */
    RoomReview selectLatestByRoomId(Long roomId);

}