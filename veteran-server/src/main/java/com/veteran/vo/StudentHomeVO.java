package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "学生端首页数据")
public class StudentHomeVO {

    @Schema(description = "总积分")
    private Integer totalPoints;

    @Schema(description = "排名")
    private Long rank;

    @Schema(description = "服务次数")
    private Long serviceCount;

    @Schema(description = "累计服务时长")
    private Long totalHours;

    @Schema(description = "获得荣誉数")
    private Long honorCount;

    @Schema(description = "技能证书数")
    private Long certCount;

    @Schema(description = "可报机会数")
    private Long availableOpportunities;

    @Schema(description = "已报名数")
    private Long appliedCount;

    @Schema(description = "月度积分趋势（近6个月累计值）")
    private List<Long> pointsTrend;

    @Schema(description = "积分构成（按来源类型）[{name, value}, ...]")
    private List<Map<String, Object>> pointsComposition;

    @Schema(description = "最近申请记录")
    private List<AppItem> applications;

    @Data
    @Schema(description = "申请项")
    public static class AppItem {
        private String title;
        private Integer status;
    }
}