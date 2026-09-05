package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict_leader_post")
public class DictLeaderPost {

    @TableId
    private String code;

    private String name;

    private Integer sortOrder;
}
