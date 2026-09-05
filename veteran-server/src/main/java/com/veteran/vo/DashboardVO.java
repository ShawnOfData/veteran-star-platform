package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "管理端看板统计")
public class DashboardVO {

    @Schema(description = "退役学生总数")
    private Long totalStudents;

    @Schema(description = "在校学生数")
    private Long activeStudents;

    @Schema(description = "积分中位数")
    private Long medianPoints;

    @Schema(description = "最高积分")
    private Long maxPoints;

    @Schema(description = "本月服务次数")
    private Long monthlyServices;

    @Schema(description = "累计服务次数")
    private Long totalServices;

    @Schema(description = "待审核记录数")
    private Long pendingReviews;

    @Schema(description = "积分分布（各分数段人数）：[0-20, 21-40, 41-60, 61-80, 81-100, 100+]")
    private List<Long> pointsDistribution;

    @Schema(description = "各学院退役人数分布：[{name, value}, ...]")
    private List<Map<String, Object>> collegeDistribution;
}