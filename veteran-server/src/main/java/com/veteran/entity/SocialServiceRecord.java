package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("social_service_record")
public class SocialServiceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String activityType;

    private BigDecimal durationHours;

    private LocalDate serviceDate;

    private Integer rating;

    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String rejectReason;

    private Integer pointsAwarded;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}