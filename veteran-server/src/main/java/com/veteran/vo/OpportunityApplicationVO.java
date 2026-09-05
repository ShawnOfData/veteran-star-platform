package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "机会报名视图")
public class OpportunityApplicationVO {
    private Long id;
    private Long opportunityId;
    private String opportunityTitle;
    private Long studentId;
    private String studentName;
    private String studentStudentId;
    private String studentPhone;
    private LocalDateTime applyTime;
    private Integer status;
    private String remark;
}
