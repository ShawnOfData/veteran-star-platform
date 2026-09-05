package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("military_honor")
public class MilitaryHonor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceExperienceId;

    private String honorCode;

    private String honorCategoryCode;

    private LocalDate awardDate;

    private Integer pointsAwarded;

    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String rejectReason;

    @TableLogic
    private Integer deleted;
}
