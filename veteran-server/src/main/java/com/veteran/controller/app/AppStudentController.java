package com.veteran.controller.app;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.Result;
import com.veteran.common.utils.AesUtil;
import com.veteran.common.utils.JwtUtil;
import com.veteran.security.LoginTokenManager;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.PointsService;
import com.veteran.service.SmsService;
import com.veteran.vo.MilitaryHonorVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.veteran.vo.PointsDetailVO;
import com.veteran.vo.ServiceExperienceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "小程序端-学生信息")
@RestController
@RequestMapping("/app/student")
@RequiredArgsConstructor
public class AppStudentController {

    private final StudentMapper studentMapper;
    private final ServiceExperienceMapper serviceExperienceMapper;
    private final MilitaryHonorMapper militaryHonorMapper;
    private final StudentSkillMapper skillMapper;
    private final DictBranchMapper dictBranchMapper;
    private final DictHonorMapper dictHonorMapper;
    private final DictHonorCategoryMapper dictHonorCategoryMapper;
    private final DictLeaderPostMapper dictLeaderPostMapper;
    private final DictCertMapper dictCertMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final SocialServiceRecordMapper socialServiceRecordMapper;
    private final PointsService pointsService;
    private final AesUtil aesUtil;
    private final JwtUtil jwtUtil;
    private final LoginTokenManager loginTokenManager;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "学号不能为空")
        private String studentNo;
        @NotBlank(message = "姓名不能为空")
        private String name;
        @NotBlank(message = "手机号不能为空")
        private String phone;
        private String smsCode;     // 验证码（可选，为空时跳过校验）
        @NotBlank(message = "密码不能为空")
        private String password;
        private String college;
        private String major;
        private String grade;
        private String retireDate;
        private String gender;
        private String ethnicity;
        private String birthDate;
        private String nativePlace;
        private String politicalStatus;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "学号不能为空")
        private String studentNo;
        private String phone;       // 验证码登录时需要
        private String smsCode;     // 验证码登录时需要
        private String password;    // 密码登录时需要
        private String loginMode;   // "password" 或 "sms"
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String gender;
        private String ethnicity;
        private String birthDate;      // 出生年月（YYYY-MM）
        private String nativePlace;
        private String politicalStatus;
        private String college;
        private String major;
        private String grade;
        private String enrollDate;     // 入学时间（YYYY-MM）
        private String retireDate;     // 退役时间（YYYY-MM）
        private String phone;
    }

    @Operation(summary = "学生注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        // 验证码校验（可选：smsCode 为空时跳过，适配移动端无验证码注册）
        if (req.getSmsCode() != null && !req.getSmsCode().isEmpty()) {
            if (!smsService.verifyCode(req.getPhone(), req.getSmsCode())) {
                return Result.fail("验证码错误或已过期");
            }
        }

        // 检查学号是否已存在
        LambdaQueryWrapper<Student> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Student::getStudentNo, req.getStudentNo());
        if (studentMapper.selectCount(checkWrapper) > 0) {
            return Result.fail("该学号已注册");
        }

        Student student = new Student();
        student.setStudentNo(req.getStudentNo());
        student.setName(req.getName());
        student.setGender(req.getGender());
        student.setEthnicity(req.getEthnicity());
        if (req.getBirthDate() != null && !req.getBirthDate().isEmpty()) {
            student.setBirthDate(com.veteran.common.utils.MonthDateUtil.toFirstDay(req.getBirthDate()));
        }
        student.setNativePlace(req.getNativePlace());
        student.setPoliticalStatus(req.getPoliticalStatus());
        student.setCollege(req.getCollege());
        student.setMajor(req.getMajor());
        student.setGrade(req.getGrade());
        student.setStatus(1);

        // 加密手机号
        String phone = req.getPhone();
        student.setPhone(aesUtil.encrypt(phone));
        student.setPhoneHash(SecureUtil.sha256(phone));

        // 密码 BCrypt 加密
        student.setPassword(passwordEncoder.encode(req.getPassword()));

        if (req.getRetireDate() != null && !req.getRetireDate().isEmpty()) {
            student.setRetireDate(com.veteran.common.utils.MonthDateUtil.toFirstDay(req.getRetireDate()));
        }

        studentMapper.insert(student);

        Map<String, Object> result = new HashMap<>();
        result.put("id", student.getId());
        result.put("studentNo", student.getStudentNo());
        result.put("name", student.getName());
        result.put("phone", phone);

        return Result.ok(result);
    }

    @Operation(summary = "学生登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, req.getStudentNo());
        Student student = studentMapper.selectOne(wrapper);

        if (student == null) {
            return Result.fail("学号未注册");
        }

        // 密码登录模式
        if ("password".equals(req.getLoginMode())) {
            if (req.getPassword() == null || req.getPassword().isEmpty()) {
                return Result.fail("请输入密码");
            }
            if (student.getPassword() == null) {
                return Result.fail("您还未设置密码，请使用验证码登录后修改密码");
            }
            if (!passwordEncoder.matches(req.getPassword(), student.getPassword())) {
                return Result.fail("密码不正确");
            }
        }
        // 验证码登录模式（默认）
        else {
            if (req.getPhone() == null || req.getPhone().isEmpty()) {
                return Result.fail("请输入手机号");
            }
            if (req.getSmsCode() != null && !req.getSmsCode().isEmpty()) {
                // 验证码方式
                if (!smsService.verifyCode(req.getPhone(), req.getSmsCode())) {
                    return Result.fail("验证码错误或已过期");
                }
            } else {
                // 短信模式下，无验证码时用手机号验证（向后兼容）
                try {
                    String decryptedPhone = aesUtil.decrypt(student.getPhone());
                    if (!req.getPhone().equals(decryptedPhone)) {
                        return Result.fail("手机号不正确");
                    }
                } catch (Exception e) {
                    return Result.fail("账号数据异常，请联系管理员");
                }
            }
        }

        String token = jwtUtil.generateToken(student.getId(), student.getStudentNo());
        // 写入登录白名单：同学生账号在其它端登录后，旧端 token 失效（自动登出）
        loginTokenManager.saveStudentToken(student.getId(), token);

        Map<String, Object> result = new HashMap<>();
        result.put("id", student.getId());
        result.put("studentNo", student.getStudentNo());
        result.put("name", student.getName());
        result.put("phone", req.getPhone());
        result.put("token", token);

        return Result.ok(result);
    }

    @Operation(summary = "获取学生积分总览")
    @GetMapping("/points")
    public Result<Map<String, Object>> getPoints(@RequestParam Long studentId) {
        int total = pointsService.getTotalPoints(studentId);

        LambdaQueryWrapper<SocialServiceRecord> sw = new LambdaQueryWrapper<>();
        sw.eq(SocialServiceRecord::getStudentId, studentId);
        long serviceCount = socialServiceRecordMapper.selectCount(sw);

        LambdaQueryWrapper<ServiceExperience> ew = new LambdaQueryWrapper<>();
        ew.eq(ServiceExperience::getStudentId, studentId);
        List<Long> seIds = serviceExperienceMapper.selectList(ew).stream()
                .map(ServiceExperience::getId).collect(java.util.stream.Collectors.toList());
        long honorCount = 0;
        if (!seIds.isEmpty()) {
            LambdaQueryWrapper<MilitaryHonor> hw = new LambdaQueryWrapper<>();
            hw.in(MilitaryHonor::getServiceExperienceId, seIds);
            honorCount = militaryHonorMapper.selectCount(hw);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("serviceCount", serviceCount);
        result.put("honorCount", honorCount);
        return Result.ok(result);
    }

    @Operation(summary = "获取学生积分明细")
    @PostMapping("/points/detail")
    public Result<List<PointsDetailVO>> getPointsDetail(@RequestBody Map<String, Object> params) {
        Long studentId = Long.valueOf(params.get("studentId").toString());
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String reasonType = params.containsKey("reasonType") ? params.get("reasonType").toString() : null;

        LambdaQueryWrapper<PointsDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsDetail::getStudentId, studentId);
        wrapper.eq(reasonType != null && !reasonType.isEmpty(), PointsDetail::getReasonType, reasonType);
        wrapper.orderByDesc(PointsDetail::getCreateTime);

        IPage<PointsDetail> detailPage = pointsDetailMapper.selectPage(new Page<>(page, size), wrapper);
        List<PointsDetailVO> voList = detailPage.getRecords().stream().map(d -> {
            PointsDetailVO vo = new PointsDetailVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        return Result.ok(voList);
    }

    @Operation(summary = "查询学生详情")
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        Student student = studentMapper.selectById(id);
        if (student != null) {
            student.setPhone(null);
            student.setPhoneHash(null);
        }
        return Result.ok(student);
    }

    @Operation(summary = "更新学生信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateRequest req) {
        Student exist = studentMapper.selectById(id);
        if (exist == null) {
            return Result.fail("学生不存在");
        }
        exist.setName(req.getName());
        exist.setGender(req.getGender());
        exist.setEthnicity(req.getEthnicity());
        exist.setBirthDate(com.veteran.common.utils.MonthDateUtil.toFirstDay(req.getBirthDate()));
        exist.setNativePlace(req.getNativePlace());
        exist.setPoliticalStatus(req.getPoliticalStatus());
        exist.setCollege(req.getCollege());
        exist.setMajor(req.getMajor());
        exist.setGrade(req.getGrade());
        exist.setEnrollDate(com.veteran.common.utils.MonthDateUtil.toFirstDay(req.getEnrollDate()));
        exist.setRetireDate(com.veteran.common.utils.MonthDateUtil.toFirstDay(req.getRetireDate()));
        // 手机号加密存储
        if (req.getPhone() != null && !req.getPhone().isEmpty()) {
            exist.setPhone(aesUtil.encrypt(req.getPhone()));
            exist.setPhoneHash(SecureUtil.sha256(req.getPhone()));
        }
        studentMapper.updateById(exist);
        return Result.ok();
    }

    @Operation(summary = "查询学生服役经历")
    @GetMapping("/service-experience/list/{studentId}")
    public Result<List<ServiceExperienceVO>> listExperiences(@PathVariable Long studentId) {
        LambdaQueryWrapper<ServiceExperience> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceExperience::getStudentId, studentId)
                .orderByDesc(ServiceExperience::getStartDate);
        return Result.ok(serviceExperienceMapper.selectList(wrapper).stream()
                .map(this::toExperienceVO).collect(Collectors.toList()));
    }

    @Operation(summary = "查询学生荣誉")
    @GetMapping("/honor/list/{serviceExperienceId}")
    public Result<List<MilitaryHonorVO>> listHonors(@PathVariable Long serviceExperienceId) {
        LambdaQueryWrapper<MilitaryHonor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MilitaryHonor::getServiceExperienceId, serviceExperienceId)
                .orderByDesc(MilitaryHonor::getAwardDate);
        return Result.ok(militaryHonorMapper.selectList(wrapper).stream()
                .map(this::toHonorVO).collect(Collectors.toList()));
    }

    @Operation(summary = "查询学生技能证书")
    @GetMapping("/skill/list/{studentId}")
    public Result<List<StudentSkill>> listSkills(@PathVariable Long studentId) {
        LambdaQueryWrapper<StudentSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentSkill::getStudentId, studentId)
                .orderByDesc(StudentSkill::getObtainDate);
        return Result.ok(skillMapper.selectList(wrapper));
    }

    @Operation(summary = "上传学生头像")
    @PostMapping("/{studentId}/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @PathVariable Long studentId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            return Result.fail("仅支持 JPG/PNG/WebP 格式");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.fail("文件大小不能超过 2MB");
        }

        try {
            // 生成文件名
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String uuidName = java.util.UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存到 {uploadDir}/avatars/（生产环境由 APP_UPLOAD_DIR 指向 docker 卷目录）
            java.io.File dir = new java.io.File(uploadDir + "/avatars/");
            if (!dir.exists()) dir.mkdirs();
            java.io.File dest = new java.io.File(dir, uuidName);
            file.transferTo(dest);

            String avatarUrl = "/uploads/avatars/" + uuidName;
            Map<String, String> result = new HashMap<>();
            result.put("url", avatarUrl);
            result.put("filename", uuidName);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修改密码")
    @PutMapping("/student/{studentId}/password")
    public Result<Void> updatePassword(@PathVariable Long studentId,
                                       @RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.fail("参数不完整");
        }
        if (newPassword.length() < 6) {
            return Result.fail("新密码至少6位");
        }

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return Result.fail("学生不存在");
        }

        // 校验旧密码
        if (student.getPassword() == null) {
            return Result.fail("您还未设置密码，请先通过短信验证码登录后设置");
        }
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            return Result.fail("当前密码不正确");
        }

        // 更新密码
        student.setPassword(passwordEncoder.encode(newPassword));
        studentMapper.updateById(student);

        return Result.ok();
    }

    private ServiceExperienceVO toExperienceVO(ServiceExperience entity) {
        ServiceExperienceVO vo = new ServiceExperienceVO();
        BeanUtils.copyProperties(entity, vo);
        DictBranch branch = dictBranchMapper.selectById(entity.getBranchCode());
        if (branch != null) {
            vo.setBranchName(branch.getName());
        }
        if (entity.getLeaderPostCode() != null) {
            DictLeaderPost post = dictLeaderPostMapper.selectById(entity.getLeaderPostCode());
            if (post != null) {
                vo.setLeaderPostName(post.getName());
            }
        }
        return vo;
    }

    private MilitaryHonorVO toHonorVO(MilitaryHonor entity) {
        MilitaryHonorVO vo = new MilitaryHonorVO();
        BeanUtils.copyProperties(entity, vo);
        DictHonor dictHonor = dictHonorMapper.selectById(entity.getHonorCode());
        if (dictHonor != null) {
            vo.setHonorName(dictHonor.getName());
        }
        if (entity.getHonorCategoryCode() != null) {
            DictHonorCategory hc = dictHonorCategoryMapper.selectById(entity.getHonorCategoryCode());
            if (hc != null) {
                vo.setHonorCategoryName(hc.getName());
            }
        }
        return vo;
    }
}