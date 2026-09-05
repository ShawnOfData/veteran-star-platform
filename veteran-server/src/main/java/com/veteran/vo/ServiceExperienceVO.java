package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "服役经历视图")
public class ServiceExperienceVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "军兵种代码")
    private String branchCode;

    @Schema(description = "军兵种名称")
    private String branchName;

    @Schema(description = "入伍时间")
    private LocalDate startDate;

    @Schema(description = "退役时间")
    private LocalDate endDate;

    @Schema(description = "服役期间职务代码")
    private String leaderPostCode;

    @Schema(description = "服役期间职务名称")
    private String leaderPostName;

    @Schema(description = "服役年限")
    private BigDecimal serviceYears;
}