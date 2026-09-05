package com.veteran.controller.app;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.Result;
import com.veteran.common.utils.AesUtil;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.AIService;
import com.veteran.service.ProfileService;
import com.veteran.service.SmsService;
import com.veteran.service.SysConfigService;
import com.veteran.vo.AIAnalysisVO;
import com.veteran.vo.PortraitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "小程序端-个人画像与设置")
@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppProfileController {

    private final ProfileService profileService;
    private final StudentPreferenceMapper preferenceMapper;
    private final StudentMapper studentMapper;
    private final AIAnalysisResultMapper aiAnalysisResultMapper;
    private final AIService aiService;
    private final AesUtil aesUtil;
    private final SysConfigService sysConfigService;
    private final SmsService smsService;

    // ========== 个人画像 ==========

    @Operation(summary = "获取学生综合画像")
    @GetMapping("/student/{studentId}/portrait")
    public Result<PortraitVO> getPortrait(@PathVariable Long studentId) {
        return Result.ok(profileService.getPortrait(studentId));
    }

    // ========== 偏好设置 ==========

    @Operation(summary = "获取学生偏好设置")
    @GetMapping("/student/{studentId}/settings")
    public Result<StudentPreference> getSettings(@PathVariable Long studentId) {
        LambdaQueryWrapper<StudentPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentPreference::getStudentId, studentId);
        StudentPreference pref = preferenceMapper.selectOne(wrapper);
        if (pref == null) {
            // 不存在则自动创建默认设置
            pref = new StudentPreference();
            pref.setStudentId(studentId);
            pref.setNotifyEnabled(1);
            pref.setResumeTemplate("military");
            pref.setShowRealName(1);
            preferenceMapper.insert(pref);
        }
        return Result.ok(pref);
    }

    @Operation(summary = "更新学生偏好设置")
    @PutMapping("/student/{studentId}/settings")
    public Result<Void> updateSettings(@PathVariable Long studentId,
                                       @RequestBody StudentPreference pref) {
        LambdaQueryWrapper<StudentPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentPreference::getStudentId, studentId);
        StudentPreference exist = preferenceMapper.selectOne(wrapper);
        if (exist == null) {
            pref.setStudentId(studentId);
            preferenceMapper.insert(pref);
        } else {
            exist.setNotifyEnabled(pref.getNotifyEnabled());
            exist.setResumeTemplate(pref.getResumeTemplate());
            exist.setShowRealName(pref.getShowRealName());
            preferenceMapper.updateById(exist);
        }
        return Result.ok();
    }

    // ========== 账号安全 ==========

    @Operation(summary = "修改手机号")
    @PutMapping("/student/{studentId}/phone")
    public Result<Void> updatePhone(@PathVariable Long studentId,
                                    @RequestBody Map<String, String> params) {
        String oldPhone = params.get("oldPhone");
        String newPhone = params.get("newPhone");
        String smsCode = params.get("smsCode");

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return Result.fail("学生不存在");
        }

        // 验证旧手机号
        try {
            String decrypted = aesUtil.decrypt(student.getPhone());
            if (!oldPhone.equals(decrypted)) {
                return Result.fail("旧手机号不正确");
            }
        } catch (Exception e) {
            return Result.fail("数据异常");
        }

        // 验证码校验（新手机号）
        if (smsCode == null || smsCode.isEmpty()) {
            return Result.fail("请先获取验证码");
        }
        if (!smsService.verifyCode(newPhone, smsCode)) {
            return Result.fail("验证码错误或已过期");
        }

        // 更新手机号
        student.setPhone(aesUtil.encrypt(newPhone));
        student.setPhoneHash(SecureUtil.sha256(newPhone));
        studentMapper.updateById(student);

        return Result.ok();
    }

    // ========== AI 分析 ==========

    @Operation(summary = "获取/刷新 AI 分析结果")
    @GetMapping("/student/{studentId}/ai-analysis")
    public Result<Map<String, Object>> getAIAnalysis(@PathVariable Long studentId,
                                                      @RequestParam(defaultValue = "false") boolean refresh) {
        AIAnalysisVO vo = aiService.analyze(studentId, refresh);
        Map<String, Object> result = new HashMap<>();
        result.put("abilityTags", vo.getAbilityTags());
        result.put("abilityScores", vo.getAbilityScores());
        result.put("pointLevel", vo.getPointLevel());
        result.put("recommendedJobs", vo.getRecommendedJobs());
        result.put("overallComment", vo.getOverallComment());
        return Result.ok(result);
    }

    // ========== 系统版本 ==========

    @Operation(summary = "获取系统版本")
    @GetMapping("/settings/version")
    public Result<String> getVersion() {
        String version = sysConfigService.getConfigValue("site_version");
        return Result.ok(version != null ? version : "v3.0");
    }

    @Operation(summary = "获取系统信息（站点名称、联系方式等）")
    @GetMapping("/settings/info")
    public Result<Map<String, String>> getSystemInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("siteName", sysConfigService.getConfigValue("site_name"));
        info.put("version", sysConfigService.getConfigValue("site_version"));
        info.put("contactPhone", sysConfigService.getConfigValue("contact_phone"));
        info.put("contactEmail", sysConfigService.getConfigValue("contact_email"));
        info.put("contactAddress", sysConfigService.getConfigValue("contact_address"));
        return Result.ok(info);
    }
}
