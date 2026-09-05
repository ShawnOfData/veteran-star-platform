package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("resume_record")
public class ResumeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String templateCode;
    private String pdfPath;
    private Long fileSize;
    private Integer status;
    private String contentHash;
    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createTime;
}
