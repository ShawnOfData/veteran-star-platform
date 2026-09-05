package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentNo;

    private String name;

    private String gender;

    private String ethnicity;

    private LocalDate birthDate;

    private String nativePlace;

    private String politicalStatus;

    private String college;

    private String major;

    private String grade;

    private String phone;

    private String phoneHash;

    private String password;

    private Integer status;

    private LocalDate enrollDate;

    private LocalDate retireDate;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}