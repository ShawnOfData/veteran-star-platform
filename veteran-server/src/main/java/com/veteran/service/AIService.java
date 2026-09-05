package com.veteran.service;

import com.veteran.vo.AIAnalysisVO;

/**
 * AI 分析服务 - 调用大模型接口生成退役学生能力画像
 */
public interface AIService {

    /**
     * 生成 AI 分析报告
     * @param studentId 学生ID
     * @param forceRefresh 是否强制刷新（忽略缓存）
     */
    AIAnalysisVO analyze(Long studentId, boolean forceRefresh);

    /**
     * 清除指定学生的 AI 分析缓存
     */
    void clearCache(Long studentId);
}
