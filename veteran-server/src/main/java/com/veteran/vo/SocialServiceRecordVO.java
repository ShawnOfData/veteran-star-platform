package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "社会服务记录视图(含学生信息)")
public class SocialServiceRecordVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "活动类型")
    private String activityType;

    @Schema(description = "活动描述")
    private String activityDesc;

    @Schema(description = "服务时长(小时)")
    private BigDecimal durationHours;

    @Schema(description = "申请积分")
    private Integer pointsApplied;

    @Schema(description = "服务日期")
    private LocalDate serviceDate;

    @Schema(description = "评分")
    private Integer rating;

    @Schema(description = "审核状态 0-待审核 1-通过 2-驳回")
    private Integer status;

    @Schema(description = "审核人ID")
    private Long reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "实际获得积分")
    private Integer pointsAwarded;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}