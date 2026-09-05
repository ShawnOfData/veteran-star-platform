package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("opportunity_application")
public class OpportunityApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long opportunityId;

    private Long studentId;

    private LocalDateTime applyTime;

    private Integer status;

    private String remark;

    private String studentName;

    private String studentStudentId;

    private String studentPhone;
}