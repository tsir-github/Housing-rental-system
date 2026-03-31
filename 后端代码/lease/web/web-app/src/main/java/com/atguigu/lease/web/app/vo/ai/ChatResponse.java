package com.atguigu.lease.web.app.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI聊天响应VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 本次对话的唯一ID
     */
    private String conversationId;

    /**
     * 用户输入的原始消息
     */
    private String userMessage;

    /**
     * AI的回复内容
     */
    private String aiReply;

    /**
     * 提取的关键词信息
     */
    private KeywordExtractionResult keywords;

    /**
     * 推荐的房源列表（最多5个）
     */
    private List<RoomRecommendation> recommendations;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 本次调用消耗的token数
     */
    private Integer tokensUsed;
}
