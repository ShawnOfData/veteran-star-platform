package com.veteran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "学生创建/更新请求")
public class StudentDTO {

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别：男/女")
    private String gender;

    @Schema(description = "民族")
    private String ethnicity;

    @Schema(description = "出生年月（年月精度）")
    private String birthDate;

    @Schema(description = "籍贯（省/市/县）")
    private String nativePlace;

    @Schema(description = "政治面貌：中共党员/中共预备党员/共青团员/群众")
    private String politicalStatus;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "年级")
    private String grade;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态 1-在校 2-毕业 3-休学")
    private Integer status;

    @Schema(description = "入学时间（年月精度，YYYY-MM）")
    private String enrollDate;

    @Schema(description = "退役返校日期（年月精度，YYYY-MM）")
    private String retireDate;
}