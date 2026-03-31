package com.atguigu.lease.web.app.service;

/**
 * 消息中心服务接口
 */
public interface MessageCenterService {

    /**
     * 发送消息
     * @param userId 用户ID
     * @param userType 用户类型 (1:官方人员, 2:房东, 3:租客)
     * @param messageType 消息类型
     * @param messageCategory 消息分类
     * @param title 消息标题
     * @param content 消息内容
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @param priority 优先级 (1:低, 2:中, 3:高)
     */
    void sendMessage(Long userId, Integer userType, String messageType, String messageCategory,
                    String title, String content, Long relatedId, String relatedType, Integer priority);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 批量标记消息为已读
     * @param userId 用户ID
     * @param messageIds 消息ID列表
     */
    void markAsReadBatch(Long userId, java.util.List<Long> messageIds);

    /**
     * 删除消息
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void deleteMessage(Long messageId, Long userId);

    /**
     * 获取用户未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    long getUnreadCount(Long userId);
}