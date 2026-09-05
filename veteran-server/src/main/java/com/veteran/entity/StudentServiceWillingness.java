package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_service_willingness")
public class StudentServiceWillingness {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String availableTimeSlot;

    private Integer acceptOffCampus;

    private String postCodes;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}