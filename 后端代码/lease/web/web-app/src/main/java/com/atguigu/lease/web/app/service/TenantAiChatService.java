package com.atguigu.lease.web.app.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atguigu.lease.model.entity.AiConversation;
import com.atguigu.lease.web.app.vo.ai.ChatRequest;
import com.atguigu.lease.web.app.vo.ai.ChatResponse;

/**
 * 租客AI聊天Service接口
 * 负责AI对话的业务逻辑处理
 */
public interface TenantAiChatService {

    /**
     * 发送消息给AI并获取回复
     *
     * @param userId 用户ID
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse sendMessage(Long userId, ChatRequest request);

    /**
     * 分页获取用户的对话历史
     *
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<AiConversation> getConversationHistory(Long userId, Integer page, Integer pageSize);

    /**
     * 分页获取用户在指定时间范围内的对话历史
     *
     * @param userId 用户ID
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<AiConversation> getConversationHistoryByDateRange(
        Long userId,
        String startDate,
        String endDate,
        Integer page,
        Integer pageSize
    );

    /**
     * 清空用户的所有对话记录
     *
     * @param userId 用户ID
     */
    void clearConversationHistory(Long userId);

    /**
     * 获取用户的对话统计信息
     *
     * @param userId 用户ID
     * @param startDate 开始日期（yyyy-MM-dd，可选）
     * @param endDate 结束日期（yyyy-MM-dd，可选）
     * @return 统计信息
     */
    ConversationStatistics getConversationStatistics(Long userId, String startDate, String endDate);

    /**
     * 对话统计信息内部类
     */
    class ConversationStatistics {
        /**
         * 总消息数
         */
        public long totalMessages;

        /**
         * 用户消息数
         */
        public long userMessages;

        /**
         * AI消息数
         */
        public long aiMessages;

        /**
         * 总token消耗数
         */
        public long totalTokensUsed;

        /**
         * 平均响应时间（毫秒）
         */
        public Double averageResponseTime;

        /**
         * 总推荐房源数
         */
        public long totalRecommendations;

        /**
         * 推荐点击率
         */
        public Double recommendationClickRate;
    }
}
