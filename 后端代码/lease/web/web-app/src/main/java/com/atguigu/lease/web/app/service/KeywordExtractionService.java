package com.atguigu.lease.web.app.service;

import com.atguigu.lease.web.app.vo.ai.KeywordExtractionResult;

/**
 * 关键词提取Service接口
 * 负责从用户输入中识别关键信息
 */
public interface KeywordExtractionService {

    /**
     * 从用户消息中提取关键词
     * 支持提取以下信息：
     * - 地理位置（区域、街道等）
     * - 房型（一室、两室、三室等）
     * - 价格范围
     * - 设施需求
     * - 时间需求（入住时间、租期）
     *
     * @param userMessage 用户输入的消息
     * @return 提取的关键词结果
     */
    KeywordExtractionResult extractKeywords(String userMessage);

    /**
     * 验证关键词的有效性
     *
     * @param result 关键词提取结果
     * @return 是否有效
     */
    boolean validateKeywords(KeywordExtractionResult result);
}
