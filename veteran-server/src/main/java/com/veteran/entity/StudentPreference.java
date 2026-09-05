package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_preference")
public class StudentPreference {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** 通知开关: 1-开启 0-关闭 */
    private Integer notifyEnabled;

    /** 简历模板: military/simple */
    private String resumeTemplate;

    /** 排行榜显示真实姓名: 1-显示 0-匿名 */
    private Integer showRealName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
