package com.veteran.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.BusinessException;
import com.veteran.common.Result;
import com.veteran.common.utils.MonthDateUtil;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.RateLimiterService;
import com.veteran.vo.MilitaryHonorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "小程序端-学生提交")
@RestController
@RequestMapping("/app/student/submit")
@RequiredArgsConstructor
public class AppSubmitController {

    private final ServiceExperienceMapper serviceExperienceMapper;
    private final MilitaryHonorMapper militaryHonorMapper;
    private final StudentSkillMapper studentSkillMapper;
    private final DictHonorMapper dictHonorMapper;
    private final DictCertMapper dictCertMapper;
    private final DictBranchMapper dictBranchMapper;
    private final DictLeaderPostMapper dictLeaderPostMapper;
    private final RateLimiterService rateLimiterService;
    private final StudentMapper studentMapper;
    private final DictHonorCategoryMapper dictHonorCategoryMapper;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${user.dir}")
    private String userDir;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "pdf"));

    private String getProofsDir() {
        String dir = uploadDir;
        if (!new File(dir).isAbsolute()) {
            dir = userDir + File.separator + dir;
        }
        return dir + File.separator + "proofs";
    }

    // ====== DTOs ======

    @Data
    public static class ServiceExperienceSubmitDTO {
        @NotNull(message = "学生ID不能为空")
        private Long studentId;
        @NotBlank(message = "军兵种代码不能为空")
        private String branchCode;
        @NotBlank(message = "入伍时间不能为空")
        private String startDate;
        @NotBlank(message = "退役时间不能为空")
        private String endDate;
        private String leaderPostCode;
    }

    @Data
    public static class HonorSubmitDTO {
        @NotNull(message = "学生ID不能为空")
        private Long studentId;
        private Long serviceExperienceId;
        @NotBlank(message = "表彰奖励代码不能为空")
        private String honorCode;
        @NotBlank(message = "荣誉类别代码不能为空")
        private String honorCategoryCode;
        private String awardDate;
    }

    @Data
    public static class SkillSubmitDTO {
        @NotNull(message = "学生ID不能为空")
        private Long studentId;
        @NotBlank(message = "证书代码不能为空")
        private String certCode;
        private String certNo;
        private String obtainDate;
        private String validUntil;
        private String proofUrl;
    }

    // ====== 文件上传 ======

    @Operation(summary = "上传证明材料（png/jpg/pdf）")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的文件");
        }
        String originalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw BusinessException.badRequest("仅支持 png、jpg、pdf 格式");
        }
        String dirPath = getProofsDir();
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw BusinessException.badRequest("文件上传失败：" + e.getMessage());
        }
        String url = "/uploads/proofs/" + fileName;
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("originalName", originalName);
        return Result.ok(result);
    }

    // ====== Endpoints ======

    @Operation(summary = "学生提交服役经历")
    @PostMapping("/service-experience")
    public Result<?> submitServiceExperience(@Valid @RequestBody ServiceExperienceSubmitDTO dto) {
        if (!rateLimiterService.trySubmit(dto.getStudentId())) {
            long cooldown = rateLimiterService.getCooldownSeconds(dto.getStudentId());
            throw BusinessException.badRequest("提交过于频繁，请 " + cooldown + " 秒后再试");
        }
        // 校验服役日期不晚于退役日期
        LocalDate startDate = MonthDateUtil.toFirstDay(dto.getStartDate());
        LocalDate endDate = MonthDateUtil.toFirstDay(dto.getEndDate());
        validateServiceDates(dto.getStudentId(), startDate, endDate);

        // 检查是否已有服役经历（待审或已通过）
        LambdaQueryWrapper<ServiceExperience> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ServiceExperience::getStudentId, dto.getStudentId())
                .in(ServiceExperience::getStatus, 0, 1)
                .last("LIMIT 1");
        ServiceExperience existing = serviceExperienceMapper.selectOne(checkWrapper);
        if (existing != null) {
            boolean changed = !Objects.equals(existing.getBranchCode(), dto.getBranchCode())
                    || !Objects.equals(existing.getLeaderPostCode(), dto.getLeaderPostCode())
                    || !Objects.equals(existing.getStartDate(), startDate)
                    || !Objects.equals(existing.getEndDate(), endDate);
            if (changed) {
                Map<String, Object> conflict = new HashMap<>();
                conflict.put("conflict", true);
                Map<String, Object> oldData = new HashMap<>();
                oldData.put("branchCode", existing.getBranchCode());
                DictBranch oldBranch = dictBranchMapper.selectById(existing.getBranchCode());
                oldData.put("branchName", oldBranch != null ? oldBranch.getName() : null);
                oldData.put("leaderPostCode", existing.getLeaderPostCode());
                if (existing.getLeaderPostCode() != null) {
                    DictLeaderPost oldPost = dictLeaderPostMapper.selectById(existing.getLeaderPostCode());
                    oldData.put("leaderPostName", oldPost != null ? oldPost.getName() : null);
                }
                oldData.put("startDate", existing.getStartDate());
                oldData.put("endDate", existing.getEndDate());
                conflict.put("existing", oldData);
                return Result.ok(conflict);
            }
            return Result.ok(existing);
        }

        ServiceExperience entity = new ServiceExperience();
        BeanUtils.copyProperties(dto, entity);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setStatus(0);
        serviceExperienceMapper.insert(entity);
        return Result.ok(entity);
    }

    @Operation(summary = "学生修改服役经历（覆盖更新）")
    @PutMapping("/service-experience")
    public Result<ServiceExperience> updateServiceExperience(@Valid @RequestBody ServiceExperienceSubmitDTO dto) {
        LocalDate startDate = MonthDateUtil.toFirstDay(dto.getStartDate());
        LocalDate endDate = MonthDateUtil.toFirstDay(dto.getEndDate());
        validateServiceDates(dto.getStudentId(), startDate, endDate);

        LambdaQueryWrapper<ServiceExperience> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceExperience::getStudentId, dto.getStudentId())
                .in(ServiceExperience::getStatus, 0, 1)
                .last("LIMIT 1");
        ServiceExperience existing = serviceExperienceMapper.selectOne(wrapper);
        if (existing == null) {
            throw BusinessException.notFound("未找到已有服役经历记录");
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setStartDate(startDate);
        existing.setEndDate(endDate);
        existing.setStatus(0);
        existing.setReviewerId(null);
        existing.setReviewTime(null);
        existing.setRejectReason(null);
        serviceExperienceMapper.updateById(existing);
        return Result.ok(existing);
    }

    /**
     * 校验服役日期不晚于该学生的退役日期
     */
    private void validateServiceDates(Long studentId, LocalDate startDate, LocalDate endDate) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw BusinessException.notFound("学生不存在");
        }
        if (student.getEnrollDate() != null && startDate.isBefore(student.getEnrollDate())) {
            throw BusinessException.badRequest("服役开始日期不能早于入学日期");
        }
        if (student.getRetireDate() != null && endDate.isAfter(student.getRetireDate())) {
            throw BusinessException.badRequest("服役结束日期不能晚于退役日期(" + student.getRetireDate() + ")");
        }
    }

    @Operation(summary = "学生提交荣誉")
    @PostMapping("/honor")
    public Result<MilitaryHonorVO> submitHonor(@Valid @RequestBody HonorSubmitDTO dto) {
        if (!rateLimiterService.trySubmit(dto.getStudentId())) {
            long cooldown = rateLimiterService.getCooldownSeconds(dto.getStudentId());
            throw BusinessException.badRequest("提交过于频繁，请 " + cooldown + " 秒后再试");
        }
        DictHonor dictHonor = dictHonorMapper.selectById(dto.getHonorCode());
        if (dictHonor == null) {
            throw BusinessException.notFound("表彰奖励类型不存在");
        }
        if (dictHonorCategoryMapper.selectById(dto.getHonorCategoryCode()) == null) {
            throw BusinessException.notFound("荣誉类别不存在");
        }

        // 未指定服役经历时，自动关联该学生已审核通过的服役经历
        Long seId = dto.getServiceExperienceId();
        if (seId == null) {
            ServiceExperience se = serviceExperienceMapper.selectList(
                    new LambdaQueryWrapper<ServiceExperience>()
                            .eq(ServiceExperience::getStudentId, dto.getStudentId())
                            .eq(ServiceExperience::getStatus, 1)
                            .orderByDesc(ServiceExperience::getStartDate)
                            .last("LIMIT 1")
            ).stream().findFirst().orElse(null);
            if (se == null) {
                throw BusinessException.badRequest("请先提交并审核通过服役经历，再提交受奖情况");
            }
            seId = se.getId();
        } else {
            // 显式指定服役经历时，必须校验归属当前学生，防止越权挂靠他人记录导致积分错记
            ServiceExperience se = serviceExperienceMapper.selectById(seId);
            if (se == null) {
                throw BusinessException.notFound("服役经历不存在");
            }
            if (!Objects.equals(se.getStudentId(), dto.getStudentId())) {
                throw BusinessException.badRequest("服役经历不属于当前学生，不能挂靠提交受奖情况");
            }
        }

        MilitaryHonor honor = new MilitaryHonor();
        honor.setServiceExperienceId(seId);
        honor.setHonorCode(dto.getHonorCode());
        honor.setHonorCategoryCode(dto.getHonorCategoryCode());
        honor.setAwardDate(MonthDateUtil.toFirstDay(dto.getAwardDate()));
        honor.setPointsAwarded(dictHonor.getDefaultPoints());
        honor.setStatus(0);
        militaryHonorMapper.insert(honor);

        MilitaryHonorVO vo = new MilitaryHonorVO();
        BeanUtils.copyProperties(honor, vo);
        vo.setHonorName(dictHonor.getName());
        return Result.ok(vo);
    }

    @Operation(summary = "学生提交技能证书")
    @PostMapping("/skill")
    public Result<StudentSkill> submitSkill(@Valid @RequestBody SkillSubmitDTO dto) {
        if (!rateLimiterService.trySubmit(dto.getStudentId())) {
            long cooldown = rateLimiterService.getCooldownSeconds(dto.getStudentId());
            throw BusinessException.badRequest("提交过于频繁，请 " + cooldown + " 秒后再试");
        }
        DictCert dictCert = dictCertMapper.selectById(dto.getCertCode());
        if (dictCert == null) {
            throw BusinessException.notFound("证书类型不存在");
        }
        StudentSkill skill = new StudentSkill();
        skill.setStudentId(dto.getStudentId());
        skill.setCertCode(dto.getCertCode());
        skill.setCertNo(dto.getCertNo());
        skill.setObtainDate(MonthDateUtil.toFirstDay(dto.getObtainDate()));
        skill.setValidUntil(MonthDateUtil.toFirstDay(dto.getValidUntil()));
        skill.setProofUrl(dto.getProofUrl());
        skill.setPointsAwarded(dictCert.getDefaultPoints());
        skill.setStatus(0);
        studentSkillMapper.insert(skill);
        return Result.ok(skill);
    }

    @Operation(summary = "查询自己的提交记录")
    @GetMapping("/list/{studentId}")
    public Result<Map<String, Object>> listSubmissions(@PathVariable Long studentId) {
        // 服役经历
        LambdaQueryWrapper<ServiceExperience> seWrapper = new LambdaQueryWrapper<>();
        seWrapper.eq(ServiceExperience::getStudentId, studentId)
                .orderByDesc(ServiceExperience::getStartDate);
        List<Map<String, Object>> experiences = serviceExperienceMapper.selectList(seWrapper).stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", e.getId());
                    m.put("studentId", e.getStudentId());
                    m.put("branchCode", e.getBranchCode());
                    DictBranch branch = dictBranchMapper.selectById(e.getBranchCode());
                    m.put("branchName", branch != null ? branch.getName() : null);
                    m.put("leaderPostCode", e.getLeaderPostCode());
                    if (e.getLeaderPostCode() != null) {
                        DictLeaderPost post = dictLeaderPostMapper.selectById(e.getLeaderPostCode());
                        m.put("leaderPostName", post != null ? post.getName() : null);
                    }
                    m.put("startDate", e.getStartDate());
                    m.put("endDate", e.getEndDate());
                    m.put("serviceYears", e.getServiceYears());
                    m.put("status", e.getStatus());
                    m.put("rejectReason", e.getRejectReason());
                    m.put("reviewTime", e.getReviewTime());
                    return m;
                }).collect(Collectors.toList());

        // 荣誉（通过服役经历关联）
        List<Long> seIds = serviceExperienceMapper.selectList(
                new LambdaQueryWrapper<ServiceExperience>()
                        .eq(ServiceExperience::getStudentId, studentId)
                        .select(ServiceExperience::getId)
        ).stream().map(ServiceExperience::getId).collect(Collectors.toList());

        List<Map<String, Object>> honors = new ArrayList<>();
        if (!seIds.isEmpty()) {
            LambdaQueryWrapper<MilitaryHonor> honorWrapper = new LambdaQueryWrapper<>();
            honorWrapper.in(MilitaryHonor::getServiceExperienceId, seIds)
                    .orderByDesc(MilitaryHonor::getAwardDate);
            honors = militaryHonorMapper.selectList(honorWrapper).stream()
                    .map(h -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", h.getId());
                        m.put("serviceExperienceId", h.getServiceExperienceId());
                        m.put("honorCode", h.getHonorCode());
                        DictHonor dict = dictHonorMapper.selectById(h.getHonorCode());
                        m.put("honorName", dict != null ? dict.getName() : null);
                        m.put("honorCategoryCode", h.getHonorCategoryCode());
                        if (h.getHonorCategoryCode() != null) {
                            DictHonorCategory hc = dictHonorCategoryMapper.selectById(h.getHonorCategoryCode());
                            m.put("honorCategoryName", hc != null ? hc.getName() : null);
                        }
                        m.put("awardDate", h.getAwardDate());
                        m.put("pointsAwarded", h.getPointsAwarded());
                        m.put("status", h.getStatus());
                        m.put("rejectReason", h.getRejectReason());
                        m.put("reviewTime", h.getReviewTime());
                        return m;
                    }).collect(Collectors.toList());
        }

        // 技能证书
        LambdaQueryWrapper<StudentSkill> skillWrapper = new LambdaQueryWrapper<>();
        skillWrapper.eq(StudentSkill::getStudentId, studentId)
                .orderByDesc(StudentSkill::getObtainDate);
        List<Map<String, Object>> skills = studentSkillMapper.selectList(skillWrapper).stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getId());
                    m.put("studentId", s.getStudentId());
                    m.put("certCode", s.getCertCode());
                    DictCert dict = dictCertMapper.selectById(s.getCertCode());
                    m.put("certName", dict != null ? dict.getName() : null);
                    m.put("certNo", s.getCertNo());
                    m.put("obtainDate", s.getObtainDate());
                    m.put("validUntil", s.getValidUntil());
                    m.put("pointsAwarded", s.getPointsAwarded());
                    m.put("status", s.getStatus());
                    m.put("rejectReason", s.getRejectReason());
                    m.put("proofUrl", s.getProofUrl());
                    m.put("reviewTime", s.getReviewTime());
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("serviceExperiences", experiences);
        result.put("honors", honors);
        result.put("skills", skills);
        return Result.ok(result);
    }
}
