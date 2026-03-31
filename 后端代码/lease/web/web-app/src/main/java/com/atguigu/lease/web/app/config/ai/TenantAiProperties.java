package com.atguigu.lease.web.app.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 租客AI助手配置属性类
 * 用于读取application.yml中的tenant.ai配置
 * 注意:API连接配置使用spring.ai.openai,这里只配置业务相关参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "tenant.ai")
public class TenantAiProperties {

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 对话上下文配置 - 保持的历史对话轮数
     */
    private Integer maxHistory = 10;

    /**
     * 会话超时时间(毫秒)
     */
    private Long sessionTimeout = 3600000L;

    /**
     * 默认欢迎消息
     */
    private String defaultWelcome;

    /**
     * 响应超时时间(毫秒)
     */
    private Integer responseTimeout = 10000;

    /**
     * 单条消息最大长度
     */
    private Integer maxMessageLength = 1000;

    /**
     * 最多推荐房源数
     */
    private Integer maxRecommendations = 5;

    /**
     * 推荐查询超时时间(毫秒)
     */
    private Integer recommendationTimeout = 2000;

    /**
     * 是否启用自动清理
     */
    private Boolean autoCleanupEnabled = true;

    /**
     * 清理多少天前的记录
     */
    private Integer cleanupDays = 90;

    /**
     * 清理任务执行时间
     */
    private String cleanupCron = "0 2 * * *";

    /**
     * 关键词提取置信度阈值
     */
    private Double keywordConfidenceThreshold = 0.5;

    /**
     * 是否启用NLP增强
     */
    private Boolean enableNlpEnhancement = false;

    /**
     * 是否记录所有对话
     */
    private Boolean logAllConversations = true;

    /**
     * 是否记录关键词提取过程
     */
    private Boolean logKeywordExtraction = false;

    /**
     * 是否记录推荐过程
     */
    private Boolean logRecommendationProcess = false;
}
