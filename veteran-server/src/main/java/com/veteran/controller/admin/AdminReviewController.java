package com.veteran.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.BusinessException;
import com.veteran.common.Result;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "管理端-审核管理")
@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ServiceExperienceMapper serviceExperienceMapper;
    private final MilitaryHonorMapper militaryHonorMapper;
    private final StudentSkillMapper studentSkillMapper;
    private final StudentMapper studentMapper;
    private final PointsService pointsService;
    private final DictHonorMapper dictHonorMapper;
    private final DictCertMapper dictCertMapper;
    private final DictBranchMapper dictBranchMapper;
    private final DictLeaderPostMapper dictLeaderPostMapper;
    private final DictHonorCategoryMapper dictHonorCategoryMapper;

    // ====== DTOs ======

    @Data
    @Schema(description = "审核请求")
    public static class ReviewDTO {
        @NotNull(message = "ID不能为空")
        @Schema(description = "记录ID")
        private Long id;
        @NotNull(message = "审核状态不能为空")
        @Schema(description = "审核状态 1-通过 2-驳回")
        private Integer status;
        @Schema(description = "驳回原因")
        private String rejectReason;
    }

    @Data
    @Schema(description = "待审项")
    public static class PendingItem {
        @Schema(description = "类型: SERVICE_EXPERIENCE / HONOR / SKILL")
        private String type;
        @Schema(description = "ID")
        private Long id;
        @Schema(description = "学生ID")
        private Long studentId;
        @Schema(description = "学生姓名")
        private String studentName;
        @Schema(description = "详情数据")
        private Map<String, Object> detail;
    }

    // ====== Endpoints ======

    @Operation(summary = "查询所有待审项")
    @GetMapping("/pending")
    public Result<List<PendingItem>> listPending(@RequestParam(required = false) String type) {
        List<PendingItem> items = new ArrayList<>();

        // 查询待审服役经历
        if (type == null || "SERVICE_EXPERIENCE".equals(type)) {
            List<ServiceExperience> seList = serviceExperienceMapper.selectList(
                    new LambdaQueryWrapper<ServiceExperience>().eq(ServiceExperience::getStatus, 0));
            Map<Long, String> studentNames = loadStudentNames(
                    seList.stream().map(ServiceExperience::getStudentId).collect(Collectors.toList()));
            for (ServiceExperience se : seList) {
                PendingItem item = new PendingItem();
                item.setType("SERVICE_EXPERIENCE");
                item.setId(se.getId());
                item.setStudentId(se.getStudentId());
                item.setStudentName(studentNames.getOrDefault(se.getStudentId(), null));
                Map<String, Object> detail = new HashMap<>();
                detail.put("branchCode", se.getBranchCode());
                DictBranch branch = dictBranchMapper.selectById(se.getBranchCode());
                detail.put("branchName", branch != null ? branch.getName() : null);
                detail.put("leaderPostCode", se.getLeaderPostCode());
                if (se.getLeaderPostCode() != null) {
                    DictLeaderPost post = dictLeaderPostMapper.selectById(se.getLeaderPostCode());
                    detail.put("leaderPostName", post != null ? post.getName() : null);
                }
                detail.put("startDate", se.getStartDate());
                detail.put("endDate", se.getEndDate());
                detail.put("serviceYears", se.getServiceYears());
                item.setDetail(detail);
                items.add(item);
            }
        }

        // 查询待审荣誉
        if (type == null || "HONOR".equals(type)) {
            List<MilitaryHonor> honorList = militaryHonorMapper.selectList(
                    new LambdaQueryWrapper<MilitaryHonor>().eq(MilitaryHonor::getStatus, 0));
            // 通过服役经历获取学生ID
            Map<Long, Long> seStudentMap = loadServiceExperienceStudentMap(
                    honorList.stream().map(MilitaryHonor::getServiceExperienceId)
                            .filter(Objects::nonNull).collect(Collectors.toList()));
            Set<Long> allStudentIds = new HashSet<>(seStudentMap.values());
            Map<Long, String> studentNames = loadStudentNames(new ArrayList<>(allStudentIds));
            for (MilitaryHonor h : honorList) {
                PendingItem item = new PendingItem();
                item.setType("HONOR");
                item.setId(h.getId());
                Long sid = seStudentMap.get(h.getServiceExperienceId());
                item.setStudentId(sid);
                item.setStudentName(sid != null ? studentNames.get(sid) : null);
                Map<String, Object> detail = new HashMap<>();
                detail.put("serviceExperienceId", h.getServiceExperienceId());
                detail.put("honorCode", h.getHonorCode());
                DictHonor dict = dictHonorMapper.selectById(h.getHonorCode());
                detail.put("honorName", dict != null ? dict.getName() : null);
                detail.put("honorCategoryCode", h.getHonorCategoryCode());
                if (h.getHonorCategoryCode() != null) {
                    DictHonorCategory hc = dictHonorCategoryMapper.selectById(h.getHonorCategoryCode());
                    detail.put("honorCategoryName", hc != null ? hc.getName() : null);
                }
                detail.put("awardDate", h.getAwardDate());
                detail.put("pointsAwarded", h.getPointsAwarded());
                item.setDetail(detail);
                items.add(item);
            }
        }

        // 查询待审技能证书
        if (type == null || "SKILL".equals(type)) {
            List<StudentSkill> skillList = studentSkillMapper.selectList(
                    new LambdaQueryWrapper<StudentSkill>().eq(StudentSkill::getStatus, 0));
            Map<Long, String> studentNames = loadStudentNames(
                    skillList.stream().map(StudentSkill::getStudentId).collect(Collectors.toList()));
            for (StudentSkill s : skillList) {
                PendingItem item = new PendingItem();
                item.setType("SKILL");
                item.setId(s.getId());
                item.setStudentId(s.getStudentId());
                item.setStudentName(studentNames.getOrDefault(s.getStudentId(), null));
                Map<String, Object> detail = new HashMap<>();
                detail.put("certCode", s.getCertCode());
                DictCert dict = dictCertMapper.selectById(s.getCertCode());
                detail.put("certName", dict != null ? dict.getName() : null);
                detail.put("certNo", s.getCertNo());
                detail.put("obtainDate", s.getObtainDate());
                detail.put("validUntil", s.getValidUntil());
                detail.put("pointsAwarded", s.getPointsAwarded());
                detail.put("proofUrl", s.getProofUrl());
                item.setDetail(detail);
                items.add(item);
            }
        }

        return Result.ok(items);
    }

    @Operation(summary = "审核服役经历")
    @PutMapping("/service-experience")
    @Transactional
    public Result<Void> reviewServiceExperience(@Valid @RequestBody ReviewDTO dto, Authentication authentication) {
        Long reviewerId = (Long) authentication.getCredentials();

        ServiceExperience entity = serviceExperienceMapper.selectById(dto.getId());
        if (entity == null) {
            throw BusinessException.notFound("服役经历不存在");
        }
        if (entity.getStatus() != 0) {
            throw BusinessException.badRequest("该记录已审核");
        }

        entity.setStatus(dto.getStatus());
        entity.setReviewerId(reviewerId);
        entity.setReviewTime(LocalDateTime.now());
        if (dto.getStatus() == 2) {
            entity.setRejectReason(dto.getRejectReason());
        }
        // 服役经历本身不发放积分，荣誉有独立的审核流程
        serviceExperienceMapper.updateById(entity);
        log.info("审核服役经历: id={}, status={}", dto.getId(), dto.getStatus());
        return Result.ok();
    }

    @Operation(summary = "审核荣誉")
    @PutMapping("/honor")
    @Transactional
    public Result<Void> reviewHonor(@Valid @RequestBody ReviewDTO dto, Authentication authentication) {
        Long reviewerId = (Long) authentication.getCredentials();

        MilitaryHonor honor = militaryHonorMapper.selectById(dto.getId());
        if (honor == null) {
            throw BusinessException.notFound("荣誉不存在");
        }
        if (honor.getStatus() != 0) {
            throw BusinessException.badRequest("该记录已审核");
        }

        honor.setStatus(dto.getStatus());
        honor.setReviewerId(reviewerId);
        honor.setReviewTime(LocalDateTime.now());

        if (dto.getStatus() == 1) {
            // 通过审核，发放积分
            ServiceExperience se = serviceExperienceMapper.selectById(honor.getServiceExperienceId());
            if (se == null) {
                throw BusinessException.badRequest("关联的服役经历不存在");
            }
            DictHonor dictHonor = dictHonorMapper.selectById(honor.getHonorCode());
            String remark = dictHonor != null ? "荣誉审核通过: " + dictHonor.getName() : "荣誉审核通过";
            pointsService.addPoints(se.getStudentId(), honor.getPointsAwarded(),
                    "HONOR", honor.getId(), "military_honor", String.valueOf(reviewerId), remark);
        } else if (dto.getStatus() == 2) {
            honor.setRejectReason(dto.getRejectReason());
        }

        militaryHonorMapper.updateById(honor);
        log.info("审核荣誉: id={}, status={}", dto.getId(), dto.getStatus());
        return Result.ok();
    }

    @Operation(summary = "审核技能证书")
    @PutMapping("/skill")
    @Transactional
    public Result<Void> reviewSkill(@Valid @RequestBody ReviewDTO dto, Authentication authentication) {
        Long reviewerId = (Long) authentication.getCredentials();

        StudentSkill skill = studentSkillMapper.selectById(dto.getId());
        if (skill == null) {
            throw BusinessException.notFound("证书不存在");
        }
        if (skill.getStatus() != 0) {
            throw BusinessException.badRequest("该记录已审核");
        }

        skill.setStatus(dto.getStatus());
        skill.setReviewerId(reviewerId);
        skill.setReviewTime(LocalDateTime.now());

        if (dto.getStatus() == 1) {
            // 通过审核，发放积分
            DictCert dictCert = dictCertMapper.selectById(skill.getCertCode());
            String remark = dictCert != null ? "证书审核通过: " + dictCert.getName() : "证书审核通过";
            pointsService.addPoints(skill.getStudentId(), skill.getPointsAwarded(),
                    "CERT", skill.getId(), "student_skill", String.valueOf(reviewerId), remark);
        } else if (dto.getStatus() == 2) {
            skill.setRejectReason(dto.getRejectReason());
        }

        studentSkillMapper.updateById(skill);
        log.info("审核技能证书: id={}, status={}", dto.getId(), dto.getStatus());
        return Result.ok();
    }

    // ====== Private methods ======

    private Map<Long, String> loadStudentNames(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getId, studentIds));
        return students.stream().collect(Collectors.toMap(Student::getId, Student::getName));
    }

    private Map<Long, Long> loadServiceExperienceStudentMap(List<Long> seIds) {
        if (seIds.isEmpty()) {
            return new HashMap<>();
        }
        List<ServiceExperience> list = serviceExperienceMapper.selectList(
                new LambdaQueryWrapper<ServiceExperience>()
                        .in(ServiceExperience::getId, seIds)
                        .select(ServiceExperience::getId, ServiceExperience::getStudentId));
        return list.stream().collect(Collectors.toMap(ServiceExperience::getId, ServiceExperience::getStudentId));
    }
}
