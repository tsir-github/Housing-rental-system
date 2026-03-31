package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewDetailVo;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewStatusVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author system
 * @description 针对表【room_review(房源审核表)】的数据库操作Service - 移动端
 * @createDate 2024-01-05 00:00:00
 */
public interface RoomReviewService extends IService<RoomReview> {

    /**
     * 根据房间ID获取最新审核记录
     */
    RoomReview getLatestReviewByRoomId(Long roomId);

    /**
     * 根据房东ID获取审核记录列表
     */
    List<RoomReview> getReviewsByLandlordId(Long landlordId);

    /**
     * 分页查询房东房源审核状态（关联公寓信息）
     */
    IPage<RoomReviewStatusVo> getLandlordRoomReviewStatus(
            Page<RoomReviewStatusVo> page,
            Long landlordId,
            Integer reviewStatus
    );

    /**
     * 获取房源审核详情（包含公寓信息和审核历史）
     */
    RoomReviewDetailVo getRoomReviewDetail(Long roomId, Long landlordId);

    /**
     * 根据房间ID获取所有审核历史记录
     */
    List<RoomReview> getAllReviewHistoryByRoomId(Long roomId, Long landlordId);

}