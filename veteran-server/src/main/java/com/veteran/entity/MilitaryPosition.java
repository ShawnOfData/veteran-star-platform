package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("military_position")
public class MilitaryPosition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceExperienceId;

    private String positionName;

    private LocalDate dutyStart;

    private LocalDate dutyEnd;

    @TableLogic
    private Integer deleted;
}