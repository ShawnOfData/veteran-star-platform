package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("student_employment_intention")
public class StudentEmploymentIntention {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String areaPreference;

    private String jobCodes;

    private String updateSemester;

    @TableLogic
    private Integer deleted;
}