package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "学生信息视图")
public class StudentVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "性别：男/女")
    private String gender;

    @Schema(description = "民族")
    private String ethnicity;

    @Schema(description = "出生年月")
    private LocalDate birthDate;

    @Schema(description = "籍贯（省/市/县）")
    private String nativePlace;

    @Schema(description = "政治面貌")
    private String politicalStatus;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "手机号(脱敏)")
    private String phone;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "入学日期")
    private LocalDate enrollDate;

    @Schema(description = "退役返校日期")
    private LocalDate retireDate;

    @Schema(description = "总积分")
    private Long totalPoints;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}