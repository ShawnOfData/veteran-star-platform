package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "公告请求")
public class AnnouncementDTO {
    @NotBlank(message = "标题不能为空")
    @Schema(description = "公告标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "优先级 0:普通 1:重要 2:紧急")
    private Integer priority;

    @Schema(description = "状态 0:草稿 1:已发布 2:关闭")
    private Integer status;
}
