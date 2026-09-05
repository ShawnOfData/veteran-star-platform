package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict_honor")
public class DictHonor {

    @TableId
    private String code;

    private String name;

    private Integer defaultPoints;

    private Integer sortOrder;
}