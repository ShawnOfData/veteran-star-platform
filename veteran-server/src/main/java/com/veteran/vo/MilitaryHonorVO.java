package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "荣誉视图")
public class MilitaryHonorVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "服役经历ID")
    private Long serviceExperienceId;

    @Schema(description = "表彰奖励代码")
    private String honorCode;

    @Schema(description = "表彰奖励名称")
    private String honorName;

    @Schema(description = "荣誉类别代码")
    private String honorCategoryCode;

    @Schema(description = "荣誉类别名称")
    private String honorCategoryName;

    @Schema(description = "受奖时间")
    private LocalDate awardDate;

    @Schema(description = "获得积分")
    private Integer pointsAwarded;

    @Schema(description = "审核状态 0-待审 1-通过 2-驳回")
    private Integer status;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;
}