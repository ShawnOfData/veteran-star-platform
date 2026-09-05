package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("student_skill")
public class StudentSkill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String certCode;

    private String certNo;

    private LocalDate obtainDate;

    private LocalDate validUntil;

    private Integer pointsAwarded;

    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String rejectReason;

    private String proofUrl;

    @TableLogic
    private Integer deleted;
}
