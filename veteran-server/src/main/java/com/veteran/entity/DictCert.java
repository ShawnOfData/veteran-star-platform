package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict_cert")
public class DictCert {

    @TableId
    private String code;

    private String name;

    private Integer defaultPoints;

    private Integer validityMonths;
}