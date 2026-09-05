package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict_post_type")
public class DictPostType {

    @TableId
    private String code;

    private String name;
}