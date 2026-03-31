package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI对话记录实体类
 * 用于存储租客与AI助手的对话记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_conversation")
@AllArgsConstructor
public class AiConversation extends BaseEntity {

    /**
     * 用户ID（租客）
     */
    private Long userId;

    /**
     * 消息类型：USER-用户消息，AI-AI回复
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String message;

    /**
     * AI模型名称
     */
    private String aiModel;

    /**
     * 使用的token数量（仅AI消息）
     */
    private Integer tokensUsed;

    /**
     * 响应时间（毫秒，仅AI消息）
     */
    private Integer responseTime;

    /**
     * 提取的关键词（JSON格式）
     */
    private String keywords;

    /**
     * 推荐的房源ID列表（JSON格式）
     */
    private String recommendedRooms;

    public AiConversation() {
        this.aiModel = "deepseek-chat";
        this.tokensUsed = 0;
        this.responseTime = 0;
    }
}
