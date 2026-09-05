package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("resume_template")
public class ResumeTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String thumbnail;
    private Integer status;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private java.time.LocalDateTime updateTime;
}
