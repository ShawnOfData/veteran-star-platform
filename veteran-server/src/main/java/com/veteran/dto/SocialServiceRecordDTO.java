package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "社会服务记录请求")
public class SocialServiceRecordDTO {

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private Long studentId;

    @NotBlank(message = "活动类型不能为空")
    @Schema(description = "活动类型")
    private String activityType;

    @NotNull(message = "服务时长不能为空")
    @Schema(description = "服务时长(小时)")
    private BigDecimal durationHours;

    @NotNull(message = "服务日期不能为空")
    @Schema(description = "服务日期")
    private LocalDate serviceDate;

    @Schema(description = "评分 1-5")
    private Integer rating;
}