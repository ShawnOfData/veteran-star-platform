package com.veteran.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.Result;
import com.veteran.dto.ServiceReviewDTO;
import com.veteran.dto.SocialServiceRecordDTO;
import com.veteran.dto.StudentSkillDTO;
import com.veteran.entity.SocialServiceRecord;
import com.veteran.entity.StudentSkill;
import com.veteran.service.SocialServiceService;
import com.veteran.vo.PointsDetailVO;
import com.veteran.vo.SocialServiceRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理端-社会服务与技能管理")
@RestController
@RequestMapping("/admin/social")
@RequiredArgsConstructor
public class AdminSocialServiceController {

    private final SocialServiceService socialServiceService;

    @Operation(summary = "查询学生社会服务记录")
    @GetMapping("/record/list/{studentId}")
    public Result<List<SocialServiceRecord>> listRecords(@PathVariable Long studentId) {
        return Result.ok(socialServiceService.listByStudentId(studentId));
    }

    @Operation(summary = "分页查询所有社会服务记录(管理员审核用)")
    @GetMapping("/record/page")
    public Result<Page<SocialServiceRecordVO>> pageRecords(@RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer size,
                                                           @RequestParam(required = false) Integer status,
                                                           @RequestParam(required = false) String studentName,
                                                           @RequestParam(required = false) String startDate,
                                                           @RequestParam(required = false) String endDate) {
        return Result.ok(socialServiceService.pageRecords(page, size, status, studentName, startDate, endDate));
    }

    @Operation(summary = "新增社会服务记录")
    @PostMapping("/record")
    public Result<SocialServiceRecord> createRecord(@Valid @RequestBody SocialServiceRecordDTO dto) {
        return Result.ok(socialServiceService.create(dto));
    }

    @Operation(summary = "审核社会服务记录")
    @PutMapping("/record/review")
    public Result<Void> review(@Valid @RequestBody ServiceReviewDTO dto, Authentication authentication) {
        Long reviewerId = (Long) authentication.getCredentials();
        socialServiceService.review(dto.getRecordId(), dto.getStatus(), dto.getRejectReason(), reviewerId);
        return Result.ok();
    }

    @Operation(summary = "查询学生技能证书")
    @GetMapping("/skill/list/{studentId}")
    public Result<List<StudentSkill>> listSkills(@PathVariable Long studentId) {
        return Result.ok(socialServiceService.listSkills(studentId));
    }

    @Operation(summary = "添加技能证书")
    @PostMapping("/skill")
    public Result<StudentSkill> addSkill(@Valid @RequestBody StudentSkillDTO dto) {
        return Result.ok(socialServiceService.addSkill(dto));
    }

    @Operation(summary = "删除技能证书")
    @DeleteMapping("/skill/{skillId}")
    public Result<Void> removeSkill(@PathVariable Long skillId) {
        socialServiceService.removeSkill(skillId);
        return Result.ok();
    }

    @Operation(summary = "查询学生积分明细")
    @GetMapping("/points/{studentId}")
    public Result<List<PointsDetailVO>> listPointsDetail(@PathVariable Long studentId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "20") Integer size) {
        return Result.ok(socialServiceService.listPointsDetail(studentId, page, size));
    }
}