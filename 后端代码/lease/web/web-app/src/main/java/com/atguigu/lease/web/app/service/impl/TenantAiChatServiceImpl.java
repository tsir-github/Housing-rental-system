package com.atguigu.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atguigu.lease.model.entity.AiConversation;
import com.atguigu.lease.web.app.config.ai.TenantAiProperties;
import com.atguigu.lease.web.app.mapper.AiConversationMapper;
import com.atguigu.lease.web.app.service.TenantAiChatService;
import com.atguigu.lease.web.app.service.KeywordExtractionService;
import com.atguigu.lease.web.app.service.DeepSeekAiService;
import com.atguigu.lease.web.app.service.RoomRecommendationService;
import com.atguigu.lease.web.app.vo.ai.ChatRequest;
import com.atguigu.lease.web.app.vo.ai.ChatResponse;
import com.atguigu.lease.web.app.vo.ai.KeywordExtractionResult;
import com.atguigu.lease.web.app.vo.ai.RoomRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 租客AI聊天Service实现类
 * 负责AI对话的业务逻辑处理、权限验证、数据转换等
 */
@Slf4j
@Service
public class TenantAiChatServiceImpl implements TenantAiChatService {

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Autowired
    private KeywordExtractionService keywordExtractionService;

    @Autowired
    private DeepSeekAiService deepSeekAiService;

    @Autowired
    private RoomRecommendationService roomRecommendationService;

    @Autowired
    private TenantAiProperties aiProperties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 发送消息给AI并获取回复
     * 业务流程：
     * 1. 验证用户身份
     * 2. 验证消息内容
     * 3. 保存用户消息
     * 4. 提取关键词
     * 5. 调用AI获取回复
     * 6. 查询推荐房源
     * 7. 保存AI回复
     * 8. 返回完整响应
     */
    @Override
    @Transactional
    public ChatResponse sendMessage(Long userId, ChatRequest request) {
        log.info("用户 {} 发送消息: {}", userId, request.getMessage());

        // 1. 验证用户身份和权限（在Controller层已验证，这里再做一次防御性检查）
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }

        // 2. 验证消息内容
        String message = request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息不能为空");
        }
        if (message.length() > 1000) {
            throw new IllegalArgumentException("消息长度不能超过1000字符");
        }

        // 3. 保存用户消息
        AiConversation userMessage = new AiConversation();
        userMessage.setUserId(userId);
        userMessage.setMessageType("USER");
        userMessage.setMessage(message);
        userMessage.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        aiConversationMapper.insert(userMessage);

        // 4. 提取关键词
        KeywordExtractionResult keywords = keywordExtractionService.extractKeywords(message);
        log.info("提取的关键词: {}", keywords);

        // 5. 查询推荐房源（先推荐，再生成AI回复）
        List<RoomRecommendation> recommendations = new ArrayList<>();
        if (keywords.getConfidence() > aiProperties.getKeywordConfidenceThreshold()) {
            // 只有在关键词提取置信度较高时才进行推荐
            recommendations = roomRecommendationService.getRecommendations(userId, keywords);
            if (recommendations.size() > aiProperties.getMaxRecommendations()) {
                recommendations = recommendations.subList(0, aiProperties.getMaxRecommendations());
            }
        }
        log.info("推荐房源数: {}", recommendations.size());

        // 6. 获取对话上下文（用于多轮对话）
        List<AiConversation> context = aiConversationMapper.selectRecentByUserId(userId, aiProperties.getMaxHistory());

        // 7. 调用AI获取回复（基于推荐的房源生成简洁回复）
        long startTime = System.currentTimeMillis();
        String aiReply = generateAiReply(message, keywords, recommendations, context);
        long responseTime = System.currentTimeMillis() - startTime;
        log.info("AI响应时间: {}ms", responseTime);

        // 8. 保存AI回复
        AiConversation aiMessage = new AiConversation();
        aiMessage.setUserId(userId);
        aiMessage.setMessageType("AI");
        aiMessage.setMessage(aiReply);
        aiMessage.setAiModel("local-generation"); // 标记为本地生成的回复
        aiMessage.setKeywords(keywords.toString()); // 转换为JSON字符串
        aiMessage.setRecommendedRooms(extractRoomIds(recommendations)); // 提取房源ID列表
        aiMessage.setResponseTime((int) responseTime);
        aiMessage.setTokensUsed(deepSeekAiService.estimateTokens(aiReply)); // 估算token数
        aiMessage.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        aiConversationMapper.insert(aiMessage);

        // 9. 构建响应
        ChatResponse response = new ChatResponse();
        response.setConversationId(UUID.randomUUID().toString());
        response.setUserMessage(message);
        response.setAiReply(aiReply);
        response.setKeywords(keywords);
        response.setRecommendations(recommendations);
        response.setResponseTime((int) responseTime);
        response.setTokensUsed(deepSeekAiService.estimateTokens(aiReply));

        log.info("对话完成，推荐房源数: {}", recommendations.size());
        return response;
    }

    /**
     * 分页获取用户的对话历史
     */
    @Override
    public Page<AiConversation> getConversationHistory(Long userId, Integer page, Integer pageSize) {
        log.info("获取用户 {} 的对话历史，页码: {}, 每页数量: {}", userId, page, pageSize);

        // 参数验证
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<AiConversation> pageObj = new Page<>(page, pageSize);
        return aiConversationMapper.selectByUserIdPage(pageObj, userId);
    }

    /**
     * 分页获取用户在指定时间范围内的对话历史
     */
    @Override
    public Page<AiConversation> getConversationHistoryByDateRange(
        Long userId,
        String startDate,
        String endDate,
        Integer page,
        Integer pageSize
    ) {
        log.info("获取用户 {} 的对话历史，时间范围: {} 到 {}", userId, startDate, endDate);

        // 参数验证
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        // 解析日期
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null && !startDate.isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate, DATE_FORMATTER).atStartOfDay();
            } catch (Exception e) {
                throw new IllegalArgumentException("开始日期格式错误，应为 yyyy-MM-dd");
            }
        }

        if (endDate != null && !endDate.isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate, DATE_FORMATTER).atTime(23, 59, 59);
            } catch (Exception e) {
                throw new IllegalArgumentException("结束日期格式错误，应为 yyyy-MM-dd");
            }
        }

        Page<AiConversation> pageObj = new Page<>(page, pageSize);
        return aiConversationMapper.selectByUserIdAndDateRangePage(pageObj, userId, startDateTime, endDateTime);
    }

    /**
     * 清空用户的所有对话记录
     */
    @Override
    @Transactional
    public void clearConversationHistory(Long userId) {
        log.info("清空用户 {} 的对话记录", userId);

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }

        int deletedCount = aiConversationMapper.deleteByUserId(userId);
        log.info("已删除 {} 条对话记录", deletedCount);
    }

    /**
     * 获取用户的对话统计信息
     */
    @Override
    public ConversationStatistics getConversationStatistics(Long userId, String startDate, String endDate) {
        log.info("获取用户 {} 的对话统计，时间范围: {} 到 {}", userId, startDate, endDate);

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }

        // 解析日期
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null && !startDate.isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate, DATE_FORMATTER).atStartOfDay();
            } catch (Exception e) {
                throw new IllegalArgumentException("开始日期格式错误，应为 yyyy-MM-dd");
            }
        }

        if (endDate != null && !endDate.isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate, DATE_FORMATTER).atTime(23, 59, 59);
            } catch (Exception e) {
                throw new IllegalArgumentException("结束日期格式错误，应为 yyyy-MM-dd");
            }
        }

        ConversationStatistics stats = new ConversationStatistics();

        // 统计消息数量
        if (startDateTime != null || endDateTime != null) {
            stats.totalMessages = aiConversationMapper.countByUserIdAndDateRange(userId, startDateTime, endDateTime);
            stats.userMessages = aiConversationMapper.countByUserIdAndMessageType(userId, "USER");
            stats.aiMessages = aiConversationMapper.countByUserIdAndMessageType(userId, "AI");
        } else {
            stats.totalMessages = aiConversationMapper.countByUserId(userId);
            stats.userMessages = aiConversationMapper.countByUserIdAndMessageType(userId, "USER");
            stats.aiMessages = aiConversationMapper.countByUserIdAndMessageType(userId, "AI");
        }

        // 统计token消耗
        stats.totalTokensUsed = aiConversationMapper.sumTokensUsedByUserId(userId);

        // 统计平均响应时间
        Double avgResponseTime = aiConversationMapper.avgResponseTimeByUserId(userId);
        stats.averageResponseTime = avgResponseTime != null ? avgResponseTime : 0.0;

        log.info("统计完成: 总消息数={}, 用户消息数={}, AI消息数={}, 总token数={}, 平均响应时间={}ms",
            stats.totalMessages, stats.userMessages, stats.aiMessages, stats.totalTokensUsed, stats.averageResponseTime);

        return stats;
    }

    /**
     * 生成简洁的AI回复
     * 基于推荐的房源生成回复，而不是调用DeepSeek API生成长篇咨询
     */
    private String generateAiReply(String userMessage, KeywordExtractionResult keywords, 
                                   List<RoomRecommendation> recommendations, List<AiConversation> context) {
        StringBuilder reply = new StringBuilder();
        
        // 如果有推荐房源，直接介绍房源
        if (recommendations != null && !recommendations.isEmpty()) {
            reply.append("好的，我为您找到了以下房源：\n\n");
            
            for (int i = 0; i < recommendations.size(); i++) {
                RoomRecommendation room = recommendations.get(i);
                reply.append(String.format("【房源%d】%s\n", i + 1, room.getApartmentName()));
                reply.append(String.format("房间号：%s\n", room.getRoomNumber()));
                reply.append(String.format("租金：%.0f元/月\n", room.getRent()));
                reply.append(String.format("位置：%s\n", room.getAddressDetail()));
                reply.append(String.format("匹配度：%.0f%%\n", room.getMatchScore() * 100));
                
                if (i < recommendations.size() - 1) {
                    reply.append("\n");
                }
            }
            
            reply.append("\n点击房源卡片可查看详细信息。如果您需要其他条件的房源，欢迎继续告诉我！");
        } else {
            // 如果没有推荐房源，生成简洁的询问回复
            reply.append("我理解您的需求。");
            
            // 根据提取的关键词生成简洁回复
            if (keywords != null) {
                if (keywords.getDistrictIds() != null && !keywords.getDistrictIds().isEmpty()) {
                    reply.append("您想在指定区域找房源。");
                }
                if (keywords.getAttrValueIds() != null && !keywords.getAttrValueIds().isEmpty()) {
                    reply.append("您需要特定的房型。");
                }
                if (keywords.getPriceRange() != null) {
                    int max = keywords.getPriceRange().getMax();
                    if (max < Integer.MAX_VALUE) {
                        reply.append(String.format("预算范围是%d-%d元/月。", 
                            keywords.getPriceRange().getMin(), max));
                    }
                }
            }
            
            reply.append("\n\n很遗憾，暂时没有完全匹配的房源。您可以：\n");
            reply.append("1. 调整预算或房型要求\n");
            reply.append("2. 扩大搜索区域\n");
            reply.append("3. 告诉我更多需求信息\n\n");
            reply.append("我会继续为您寻找合适的房源！");
        }
        
        return reply.toString();
    }

    /**
     * 从推荐房源列表转换为JSON字符串
     */
    private String extractRoomIds(List<RoomRecommendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return null;  // 如果没有推荐，返回null而不是空数组
        }
        
        // 使用简单的JSON序列化（或者可以使用Jackson/Gson）
        try {
            // 构建完整的房源推荐JSON数组
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < recommendations.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                RoomRecommendation room = recommendations.get(i);
                sb.append("{");
                sb.append("\"roomId\":").append(room.getRoomId()).append(",");
                sb.append("\"roomNumber\":\"").append(escapeJson(room.getRoomNumber())).append("\",");
                sb.append("\"apartmentName\":\"").append(escapeJson(room.getApartmentName())).append("\",");
                sb.append("\"rent\":").append(room.getRent()).append(",");
                sb.append("\"location\":\"").append(escapeJson(room.getLocation())).append("\",");
                sb.append("\"addressDetail\":\"").append(escapeJson(room.getAddressDetail())).append("\",");
                sb.append("\"matchScore\":").append(room.getMatchScore());
                sb.append("}");
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            log.error("序列化房源推荐失败", e);
            return null;
        }
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
