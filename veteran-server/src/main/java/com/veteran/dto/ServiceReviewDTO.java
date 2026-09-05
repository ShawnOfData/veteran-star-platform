package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "社会服务审核请求")
public class ServiceReviewDTO {

    @NotNull(message = "记录ID不能为空")
    @Schema(description = "记录ID")
    private Long recordId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态 1-通过 2-驳回")
    private Integer status;

    @Schema(description = "驳回原因")
    private String rejectReason;
}