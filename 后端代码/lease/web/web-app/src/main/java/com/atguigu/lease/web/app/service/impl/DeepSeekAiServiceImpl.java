package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.AiConversation;
import com.atguigu.lease.web.app.config.ai.TenantAiProperties;
import com.atguigu.lease.web.app.service.DeepSeekAiService;
import com.atguigu.lease.web.app.vo.ai.KeywordExtractionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DeepSeek AI Service实现类
 * 负责调用DeepSeek API获取AI回复
 * 使用Spring AI框架集成DeepSeek API
 */
@Slf4j
@Service
public class DeepSeekAiServiceImpl implements DeepSeekAiService {

    @Autowired
    private TenantAiProperties aiProperties;

    @Autowired(required = false)
    private OpenAiChatModel chatModel;

    /**
     * 调用DeepSeek API获取AI回复
     * 使用Spring AI框架调用DeepSeek API
     */
    @Override
    public String callDeepSeekApi(String userMessage, List<AiConversation> context, KeywordExtractionResult keywords) {
        log.info("调用DeepSeek API，用户消息: {}", userMessage);

        try {
            // 检查ChatModel是否已初始化
            if (chatModel == null) {
                log.warn("ChatModel未初始化，返回模拟回复");
                return generateMockReply(userMessage, keywords);
            }

            // 构建消息列表
            List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
            
            // 添加系统提示词
            messages.add(new SystemMessage(aiProperties.getSystemPrompt()));
            
            // 添加历史消息作为上下文
            if (context != null && !context.isEmpty()) {
                for (AiConversation conv : context) {
                    if ("USER".equals(conv.getMessageType())) {
                        messages.add(new UserMessage(conv.getMessage()));
                    } else {
                        messages.add(new org.springframework.ai.chat.messages.AssistantMessage(conv.getMessage()));
                    }
                }
            }
            
            // 添加当前用户消息
            messages.add(new UserMessage(userMessage));
            
            // 构建请求选项(使用默认配置,已在SpringAiConfig中设置)
            // 如果需要覆盖默认配置,可以在这里指定
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .build();
            
            // 构建Prompt
            Prompt prompt = new Prompt(messages, options);
            
            // 调用API获取回复
            String reply = chatModel.call(prompt).getResult().getOutput().getText();
            
            log.info("DeepSeek API调用成功，返回内容长度: {}", reply.length());
            return reply;

        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            return "抱歉，我暂时无法处理您的请求，请稍后重试。";
        }
    }

    /**
     * 估算消息的token数量
     * 简单估算：中文字符约1个token，英文单词约0.3个token
     */
    @Override
    public int estimateTokens(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        int tokens = 0;
        for (char c : message.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                // 中文字符
                tokens += 1;
            } else if (Character.isLetter(c)) {
                // 英文字母
                tokens += 0.3;
            } else if (Character.isDigit(c)) {
                // 数字
                tokens += 0.3;
            }
        }

        return Math.max(1, (int) Math.ceil(tokens));
    }

    /**
     * 获取API调用的统计信息
     */
    @Override
    public ApiStatistics getApiStatistics() {
        // TODO: 实现统计信息收集逻辑
        ApiStatistics stats = new ApiStatistics();
        stats.totalCalls = 0;
        stats.totalTokens = 0;
        stats.averageResponseTime = 0.0;
        stats.errorCount = 0;
        return stats;
    }

    /**
     * 生成模拟回复（用于开发测试）
     */
    private String generateMockReply(String userMessage, KeywordExtractionResult keywords) {
        StringBuilder reply = new StringBuilder();

        // 使用配置的系统提示词中的欢迎语
        reply.append("好的，我为您分析一下租房需求。\n\n");

        // 根据提取的关键词生成回复
        if (keywords != null) {
            if (keywords.getDistrictIds() != null && !keywords.getDistrictIds().isEmpty()) {
                reply.append("📍 地区需求：您想在指定区域找房源。\n");
            }

            if (keywords.getAttrValueIds() != null && !keywords.getAttrValueIds().isEmpty()) {
                reply.append("🏠 房型需求：您需要特定的房型配置。\n");
            }

            if (keywords.getPriceRange() != null) {
                int min = keywords.getPriceRange().getMin();
                int max = keywords.getPriceRange().getMax();
                if (max < Integer.MAX_VALUE) {
                    reply.append(String.format("💰 价格范围：%d-%d元/月。\n", min, max));
                }
            }

            if (keywords.getFacilityIds() != null && !keywords.getFacilityIds().isEmpty()) {
                reply.append("🛠️ 设施需求：您需要特定的房源设施。\n");
            }

            if (keywords.getMoveInDate() != null) {
                reply.append(String.format("📅 入住时间：%s。\n", keywords.getMoveInDate()));
            }

            if (keywords.getLeaseDuration() != null) {
                reply.append(String.format("⏱️ 租期：%d个月。\n", keywords.getLeaseDuration()));
            }

            reply.append("\n");
            reply.append("根据您的需求，我已经为您筛选出了几个合适的房源。");
            reply.append("您可以查看下方的推荐房源，如果有任何疑问，欢迎继续提问！");
        } else {
            reply.append("我已经收到您的消息。请告诉我您的具体租房需求，比如：\n");
            reply.append("- 想在哪个区域找房源\n");
            reply.append("- 需要什么样的房型（一室、两室等）\n");
            reply.append("- 预算范围是多少\n");
            reply.append("- 需要什么设施（电梯、停车位等）\n");
            reply.append("- 什么时候需要入住\n\n");
            reply.append("这样我可以为您提供更精准的房源推荐。");
        }

        return reply.toString();
    }
}
