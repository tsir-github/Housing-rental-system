package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.AiConversation;
import com.atguigu.lease.web.app.vo.ai.KeywordExtractionResult;

import java.util.List;

/**
 * DeepSeek AI Service接口
 * 负责调用DeepSeek API获取AI回复
 */
public interface DeepSeekAiService {

    /**
     * 调用DeepSeek API获取AI回复
     *
     * @param userMessage 用户消息
     * @param context 对话上下文（历史消息）
     * @param keywords 提取的关键词
     * @return AI的回复内容
     */
    String callDeepSeekApi(String userMessage, List<AiConversation> context, KeywordExtractionResult keywords);

    /**
     * 估算消息的token数量
     *
     * @param message 消息内容
     * @return 估算的token数
     */
    int estimateTokens(String message);

    /**
     * 获取API调用的统计信息
     *
     * @return 统计信息
     */
    ApiStatistics getApiStatistics();

    /**
     * API调用统计信息
     */
    class ApiStatistics {
        /**
         * 总调用次数
         */
        public long totalCalls;

        /**
         * 总token消耗
         */
        public long totalTokens;

        /**
         * 平均响应时间（毫秒）
         */
        public Double averageResponseTime;

        /**
         * 错误次数
         */
        public long errorCount;
    }
}
