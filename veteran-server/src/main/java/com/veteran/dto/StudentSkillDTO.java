package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "技能证书请求")
public class StudentSkillDTO {

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private Long studentId;

    @NotBlank(message = "证书代码不能为空")
    @Schema(description = "证书代码")
    private String certCode;

    @Schema(description = "证书编号")
    private String certNo;

    @Schema(description = "获得日期")
    private LocalDate obtainDate;

    @Schema(description = "有效期至")
    private LocalDate validUntil;
}