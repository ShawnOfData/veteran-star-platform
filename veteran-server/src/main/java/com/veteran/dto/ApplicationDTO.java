package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "报名请求")
public class ApplicationDTO {

    @NotNull(message = "机会ID不能为空")
    @Schema(description = "机会ID")
    private Long opportunityId;

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "备注")
    private String remark;
}