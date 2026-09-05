package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "积分明细视图")
public class PointsDetailVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "变动值")
    private Integer changeValue;

    @Schema(description = "原因类型")
    private String reasonType;

    @Schema(description = "关联ID")
    private Long relatedId;

    @Schema(description = "来源表")
    private String sourceTable;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}