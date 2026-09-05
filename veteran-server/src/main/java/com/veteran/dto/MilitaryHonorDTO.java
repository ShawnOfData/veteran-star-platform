package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "荣誉请求")
public class MilitaryHonorDTO {

    @Schema(description = "学生ID（serviceExperienceId 为空时用于自动关联）")
    private Long studentId;

    @Schema(description = "服役经历ID（可空，为空时自动关联已审核服役经历）")
    private Long serviceExperienceId;

    @NotBlank(message = "表彰奖励代码不能为空")
    @Schema(description = "表彰奖励代码(dict_honor)")
    private String honorCode;

    @NotBlank(message = "荣誉类别代码不能为空")
    @Schema(description = "荣誉类别代码(dict_honor_category)")
    private String honorCategoryCode;

    @Schema(description = "受奖时间（yyyy-MM）")
    private String awardDate;
}