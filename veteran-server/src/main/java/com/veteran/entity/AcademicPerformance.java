package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("academic_performance")
public class AcademicPerformance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String semester;

    private BigDecimal gpa;

    private String scholarship;

    private String studentLeader;

    private String activityParticipation;

    private Integer pointsAwarded;

    @TableLogic
    private Integer deleted;
}