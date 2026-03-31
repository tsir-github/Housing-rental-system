package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.MessageCenter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息中心Mapper
 */
public interface MessageCenterMapper extends BaseMapper<MessageCenter> {

    /**
     * 获取用户未读消息数量
     */
    Integer getUnreadCount(@Param("recipientId") Long recipientId, @Param("recipientType") Integer recipientType);

    /**
     * 批量标记为已读
     */
    Integer batchMarkAsRead(@Param("recipientId") Long recipientId, @Param("recipientType") Integer recipientType, @Param("messageIds") List<Long> messageIds);

    /**
     * 获取用户消息列表（分页）
     */
    List<MessageCenter> getUserMessages(@Param("recipientId") Long recipientId, @Param("recipientType") Integer recipientType, @Param("messageType") String messageType, @Param("isRead") Integer isRead);
}