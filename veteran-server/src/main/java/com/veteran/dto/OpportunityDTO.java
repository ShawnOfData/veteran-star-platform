package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Schema(description = "机会/活动请求")
public class OpportunityDTO {

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @NotBlank(message = "类型不能为空")
    @Schema(description = "类型")
    private String type;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "单位名称")
    private String unitName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "需求人数")
    private Integer demandCount;

    @Schema(description = "要求")
    private String requirements;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "标签（JSON数组）")
    private String tags;

    @Schema(description = "优先级")
    private Integer priority;
}
