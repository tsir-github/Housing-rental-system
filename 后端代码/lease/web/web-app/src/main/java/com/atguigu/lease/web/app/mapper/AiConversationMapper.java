package com.atguigu.lease.web.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atguigu.lease.model.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI对话记录Mapper
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {

    /**
     * 分页查询用户的对话历史
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<AiConversation> selectByUserIdPage(Page<AiConversation> page, @Param("userId") Long userId);

    /**
     * 分页查询用户在指定时间范围内的对话历史
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    Page<AiConversation> selectByUserIdAndDateRangePage(
        Page<AiConversation> page,
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 查询用户最近的对话记录（用于构建上下文）
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 对话记录列表
     */
    List<AiConversation> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 删除用户的所有对话记录
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除指定日期之前的对话记录（用于数据清理）
     *
     * @param beforeDate 指定日期
     * @return 删除的记录数
     */
    int deleteBeforeDate(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 统计用户的对话数量
     *
     * @param userId 用户ID
     * @return 对话数量
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户在指定时间范围内的对话数量
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 对话数量
     */
    long countByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 统计用户消息数量
     *
     * @param userId 用户ID
     * @param messageType 消息类型（USER或AI）
     * @return 消息数量
     */
    long countByUserIdAndMessageType(@Param("userId") Long userId, @Param("messageType") String messageType);

    /**
     * 统计用户消耗的总token数
     *
     * @param userId 用户ID
     * @return 总token数
     */
    long sumTokensUsedByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的平均响应时间
     *
     * @param userId 用户ID
     * @return 平均响应时间（毫秒）
     */
    Double avgResponseTimeByUserId(@Param("userId") Long userId);
}
