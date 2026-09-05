package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "积分排行")
public class RankingVO {

    @Schema(description = "学生ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "总积分")
    private Long totalPoints;

    @Schema(description = "服务积分")
    private Long servicePoints;

    @Schema(description = "荣誉积分")
    private Long honorPoints;

    @Schema(description = "证书积分")
    private Long certPoints;
}