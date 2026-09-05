package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("service_experience")
public class ServiceExperience {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String branchCode;

    private LocalDate startDate;

    private LocalDate endDate;

    private String leaderPostCode;

    // 数据库生成列（STORED），由 start_date/end_date 自动计算，插入与更新均不可赋值
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private java.math.BigDecimal serviceYears;

    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String rejectReason;

    @TableLogic
    private Integer deleted;
}
