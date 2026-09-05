package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_detail")
public class PointsDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Integer changeValue;

    private String reasonType;

    private Long relatedId;

    private String sourceTable;

    private String operator;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}