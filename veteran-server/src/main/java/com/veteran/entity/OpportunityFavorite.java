package com.veteran.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("opportunity_favorite")
public class OpportunityFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long opportunityId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
