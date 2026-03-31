package com.atguigu.lease.web.app.service;

import com.atguigu.lease.web.app.vo.ai.KeywordExtractionResult;
import com.atguigu.lease.web.app.vo.ai.RoomRecommendation;

import java.util.List;

/**
 * 房源推荐Service接口
 * 负责根据关键词查询和推荐房源
 */
public interface RoomRecommendationService {

    /**
     * 根据关键词获取推荐房源
     * 推荐算法：
     * 1. 精确匹配：根据关键词精确查询房源
     * 2. 历史偏好：分析用户浏览和预约历史
     * 3. 相似推荐：推荐相似区域或价格范围的房源
     * 4. 排序优化：按相关度、热度、新发布时间排序
     *
     * @param userId 用户ID
     * @param keywords 提取的关键词
     * @return 推荐房源列表
     */
    List<RoomRecommendation> getRecommendations(Long userId, KeywordExtractionResult keywords);

    /**
     * 获取相似房源
     * 根据指定房源的特征（地区、价格、设施）查询相似房源
     *
     * @param userId 用户ID
     * @param roomId 房源ID
     * @return 相似房源列表
     */
    List<RoomRecommendation> getSimilarRooms(Long userId, Long roomId);

    /**
     * 计算房源与关键词的匹配度
     *
     * @param roomId 房源ID
     * @param keywords 关键词
     * @return 匹配度评分（0-1）
     */
    Double calculateMatchScore(Long roomId, KeywordExtractionResult keywords);
}
