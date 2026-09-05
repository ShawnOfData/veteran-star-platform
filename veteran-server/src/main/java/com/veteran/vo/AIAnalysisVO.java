package com.veteran.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * AI 分析结果 VO
 */
@Data
public class AIAnalysisVO {

    /** 能力标签列表，如 ["领导力", "执行力", "团队协作"] */
    private List<String> abilityTags;

    /** 能力评分，如 {"领导力": 85, "执行力": 92} */
    private Map<String, Integer> abilityScores;

    /** 积分等级 A/B/C/D */
    private String pointLevel;

    /** 推荐岗位列表 */
    private List<String> recommendedJobs;

    /** AI 综合评语（2-3句话） */
    private String overallComment;
}
