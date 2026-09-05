package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "服役经历请求")
public class ServiceExperienceDTO {

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private Long studentId;

    @NotBlank(message = "军兵种代码不能为空")
    @Schema(description = "军兵种代码")
    private String branchCode;

    @NotBlank(message = "入伍时间不能为空")
    @Schema(description = "入伍时间（yyyy-MM）")
    private String startDate;

    @NotBlank(message = "退役时间不能为空")
    @Schema(description = "退役时间（yyyy-MM）")
    private String endDate;

    @Schema(description = "服役期间职务代码(dict_leader_post)")
    private String leaderPostCode;
}