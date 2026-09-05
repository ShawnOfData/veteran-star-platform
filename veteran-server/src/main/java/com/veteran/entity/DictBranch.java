package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict_branch")
public class DictBranch {

    @TableId
    private String code;

    private String name;

    private Integer sortOrder;
}