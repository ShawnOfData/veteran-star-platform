package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("opportunity")
public class Opportunity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String type;

    private String description;

    private Long publisherId;

    private String unitName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer demandCount;

    private Integer currentApplied;

    private String requirements;

    /** 封面图 */
    private String coverUrl;

    /** 地址 */
    private String address;

    /** 薪资范围 */
    private String salaryRange;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 标签（JSON数组） */
    private String tags;

    /** 优先级（越大越靠前） */
    private Integer priority;

    /** 浏览次数 */
    private Integer viewCount;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
