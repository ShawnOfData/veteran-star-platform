package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "学生查询条件")
public class StudentQueryDTO {

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "兵种代码")
    private String branchCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;
}