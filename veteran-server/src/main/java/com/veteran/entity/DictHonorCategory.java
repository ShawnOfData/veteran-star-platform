package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict_honor_category")
public class DictHonorCategory {

    @TableId
    private String code;

    private String name;

    private Integer sortOrder;
}
