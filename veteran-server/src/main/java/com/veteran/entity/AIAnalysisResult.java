package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_analysis_result")
public class AIAnalysisResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** 能力标签 JSON 数组 */
    private String abilityTags;

    /** 能力评分 JSON 对象 */
    private String abilityScores;

    /** 积分等级 */
    private String pointLevel;

    /** 推荐岗位 JSON 数组 */
    private String recommendedJobs;

    /** 综合评语 */
    private String overallComment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
