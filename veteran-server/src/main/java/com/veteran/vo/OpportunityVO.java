package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "机会/活动视图")
public class OpportunityVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "单位名称")
    private String unitName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "需求人数")
    private Integer demandCount;

    @Schema(description = "已报名人数")
    private Integer currentApplied;

    @Schema(description = "要求")
    private String requirements;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "标签（JSON数组）")
    private String tags;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "报名人数")
    private Integer applyCount;

    @Schema(description = "是否收藏")
    private Boolean isFavorited = false;
}
