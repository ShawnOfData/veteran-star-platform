package com.veteran.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lowagie.text.pdf.BaseFont;
import com.veteran.common.BusinessException;
import com.veteran.common.utils.AesUtil;
import com.veteran.dto.ResumeEditDTO;
import com.veteran.entity.ResumeRecord;
import com.veteran.entity.ResumeTemplate;
import com.veteran.entity.Student;
import com.veteran.mapper.ResumeRecordMapper;
import com.veteran.mapper.ResumeTemplateMapper;
import com.veteran.mapper.StudentMapper;
import com.veteran.service.ProfileService;
import com.veteran.service.ResumeService;
import com.veteran.vo.PortraitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private static final String CACHE_KEY_PREFIX = "resume:pdf:";
    private static final String OUTPUT_DIR = "uploads/resumes/";
    private static final long CACHE_TTL_HOURS = 24;

    private final ProfileService profileService;
    private final ResumeRecordMapper recordMapper;
    private final ResumeTemplateMapper templateMapper;
    private final TemplateEngine templateEngine;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StudentMapper studentMapper;
    private final AesUtil aesUtil;

    @Value("${resume.font-path:fonts/NotoSansSC-Regular.ttf}")
    private String fontPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    private ITextRenderer renderer;

    @PostConstruct
    public void init() {
        try {
            renderer = new ITextRenderer();
            Path fontFile = Paths.get(fontPath);
            if (Files.exists(fontFile)) {
                renderer.getFontResolver().addFont(
                        fontFile.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } else {
                log.warn("未找到自定义字体 {}，使用系统默认字体（PDF 中文可能乱码）", fontPath);
            }
        } catch (Exception e) {
            log.warn("字体初始化失败: {}", e.getMessage());
        }
        try { Files.createDirectories(Paths.get(OUTPUT_DIR)); } catch (Exception ignored) {}
    }

    @Override
    public String generatePdf(Long studentId, String templateCode) {
        PortraitVO portrait = profileService.getPortrait(studentId);
        if (portrait.getBasic() == null) return null;

        String contentHash = computeContentHash(portrait);

        String cacheKey = CACHE_KEY_PREFIX + studentId + ":" + templateCode;
        Object cachedPath = redisTemplate.opsForValue().get(cacheKey + ":path");
        Object cachedHash = redisTemplate.opsForValue().get(cacheKey + ":hash");
        if (cachedPath != null && contentHash.equals(cachedHash)) {
            String path = cachedPath.toString();
            if (Files.exists(Paths.get(path))) {
                return path;
            }
        }

        String html = buildResumeHtml(portrait, templateCode, null);

        String outputPath = OUTPUT_DIR + studentId + "_" + templateCode + "_" +
                System.currentTimeMillis() + ".pdf";
        try {
            generatePdfFromHtml(html, outputPath);
        } catch (Exception e) {
            log.error("PDF生成失败: studentId={}, template={}", studentId, templateCode, e);
            String friendly = mapPdfErrorMessage(e);
            throw new BusinessException(500, "简历生成失败：" + friendly);
        }

        ResumeRecord record = new ResumeRecord();
        record.setStudentId(studentId);
        record.setTemplateCode(templateCode);
        record.setPdfPath(outputPath);
        record.setFileSize(FileUtil.size(new File(outputPath)));
        record.setStatus(1);
        record.setContentHash(contentHash);
        recordMapper.insert(record);

        redisTemplate.opsForValue().set(cacheKey + ":path", outputPath, CACHE_TTL_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(cacheKey + ":hash", contentHash, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return outputPath;
    }

    @Override
    public String getPreviewHtml(Long studentId, String templateCode) {
        PortraitVO portrait = profileService.getPortrait(studentId);
        if (portrait.getBasic() == null) return "<p>暂无数据</p>";
        return buildResumeHtml(portrait, templateCode, null);
    }

    @Override
    public String generatePdfWithEdit(Long studentId, ResumeEditDTO editData, String templateCode) {
        PortraitVO portrait = profileService.getPortrait(studentId);
        if (portrait.getBasic() == null) return null;

        String html = buildResumeHtml(portrait, templateCode, editData);

        String outputPath = OUTPUT_DIR + studentId + "_" + templateCode + "_edit_" +
                System.currentTimeMillis() + ".pdf";
        try {
            generatePdfFromHtml(html, outputPath);
        } catch (Exception e) {
            log.error("编辑后PDF生成失败: studentId={}", studentId, e);
            String friendly = mapPdfErrorMessage(e);
            throw new BusinessException(500, "简历生成失败：" + friendly);
        }

        ResumeRecord record = new ResumeRecord();
        record.setStudentId(studentId);
        record.setTemplateCode(templateCode);
        record.setPdfPath(outputPath);
        record.setFileSize(FileUtil.size(new File(outputPath)));
        record.setStatus(1);
        recordMapper.insert(record);

        return outputPath;
    }

    @Override
    public String getPreviewHtmlWithEdit(Long studentId, ResumeEditDTO editData, String templateCode) {
        PortraitVO portrait = profileService.getPortrait(studentId);
        if (portrait.getBasic() == null) return "<p>暂无数据</p>";
        return buildResumeHtml(portrait, templateCode, editData);
    }

    @Override
    public List<Map<String, Object>> getTemplates() {
        LambdaQueryWrapper<ResumeTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResumeTemplate::getStatus, 1).orderByAsc(ResumeTemplate::getSort);
        List<ResumeTemplate> list = templateMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ResumeTemplate t : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", t.getCode());
            item.put("name", t.getName());
            item.put("thumbnail", t.getThumbnail());
            result.add(item);
        }
        return result;
    }

    @Override
    public String computeContentHash(PortraitVO portrait) {
        StringBuilder sb = new StringBuilder();
        sb.append(portrait.getBasic().getStudentNo())
          .append(portrait.getPoints() != null ? portrait.getPoints().getTotal() : 0);
        if (portrait.getHonors() != null) {
            portrait.getHonors().forEach(h -> sb.append(h.getName()).append(h.getAwardDate()));
        }
        if (portrait.getCerts() != null) {
            portrait.getCerts().forEach(c -> sb.append(c.getName()));
        }
        return SecureUtil.md5(sb.toString());
    }

    @Override
    public Map<String, Object> getEditInitData(Long studentId) {
        PortraitVO portrait = profileService.getPortrait(studentId);
        Map<String, Object> result = new HashMap<>();

        // 基本信息
        if (portrait.getBasic() != null) {
            PortraitVO.BasicInfo basic = portrait.getBasic();
            result.put("name", basic.getName());
            result.put("college", basic.getCollege());
            result.put("major", basic.getMajor());
            result.put("grade", basic.getGrade());
        }

        // 手机号（从 Student 解密）
        Student student = studentMapper.selectById(studentId);
        if (student != null && student.getPhone() != null) {
            try {
                result.put("phone", aesUtil.decrypt(student.getPhone()));
            } catch (Exception e) {
                log.warn("手机号解密失败: studentId={}", studentId);
                result.put("phone", "");
            }
        } else {
            result.put("phone", "");
        }
        // 邮箱暂无字段
        result.put("email", "");

        // 默认技能：从证书名提取前 5 个
        List<String> defaultSkills = new ArrayList<>();
        if (portrait.getCerts() != null) {
            for (PortraitVO.CertInfo c : portrait.getCerts()) {
                if (c.getName() != null && !c.getName().isEmpty() && !defaultSkills.contains(c.getName())) {
                    defaultSkills.add(c.getName());
                    if (defaultSkills.size() >= 5) break;
                }
            }
        }
        result.put("skills", defaultSkills);

        // 求职意向（从画像回填）
        if (portrait.getIntention() != null) {
            result.put("expectedCity", portrait.getIntention().getExpectedCity());
            result.put("expectedSalary", portrait.getIntention().getExpectedSalary());
        }
        result.put("expectedJob", "");
        result.put("personalSummary", "");
        result.put("avatarUrl", "");

        // 模块开关默认全部开启
        result.put("showHonors", true);
        result.put("showCerts", true);
        result.put("showServices", true);
        result.put("showAcademic", true);
        result.put("showIntention", true);
        result.put("showInternships", true);
        result.put("showProjects", true);
        // 实习/项目/自定义模块默认空列表（用户在编辑表单中自行添加）
        result.put("internships", Collections.emptyList());
        result.put("projects", Collections.emptyList());
        result.put("customSections", Collections.emptyList());

        // 画像完整度信息（前端展示用）
        Map<String, Object> portraitMeta = new HashMap<>();
        portraitMeta.put("honorCount", portrait.getHonors() != null ? portrait.getHonors().size() : 0);
        portraitMeta.put("certCount", portrait.getCerts() != null ? portrait.getCerts().size() : 0);
        portraitMeta.put("serviceCount", portrait.getServices() != null ? portrait.getServices().size() : 0);
        portraitMeta.put("academicCount", portrait.getAcademic() != null ? portrait.getAcademic().size() : 0);
        portraitMeta.put("hasMilitary", portrait.getMilitary() != null);
        portraitMeta.put("hasIntention", portrait.getIntention() != null);
        result.put("portraitMeta", portraitMeta);

        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentRecords(Long studentId, int limit) {
        LambdaQueryWrapper<ResumeRecord> wrapper = new LambdaQueryWrapper<ResumeRecord>()
                .eq(ResumeRecord::getStudentId, studentId)
                .eq(ResumeRecord::getStatus, 1)
                .orderByDesc(ResumeRecord::getCreateTime);
        if (limit > 0) {
            wrapper.last("LIMIT " + Math.min(limit, 20));
        }
        return recordMapper.selectList(wrapper).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("templateCode", r.getTemplateCode());
            m.put("pdfPath", r.getPdfPath());
            m.put("fileSize", r.getFileSize());
            m.put("createTime", r.getCreateTime());
            return m;
        }).collect(Collectors.toList());
    }

    // ========== 内部方法 ==========

    /**
     * 合并编辑数据到 PortraitVO 的 Context 中
     */
    private void applyEditData(Context ctx, PortraitVO portrait, ResumeEditDTO editData) {
        // 基本信息覆盖（editData为null时使用portrait原始值）
        if (editData != null && editData.getName() != null && !editData.getName().isEmpty()) {
            ctx.setVariable("basicName", editData.getName());
        } else {
            ctx.setVariable("basicName", portrait.getBasic().getName());
        }

        if (editData != null && editData.getCollege() != null) {
            ctx.setVariable("basicCollege", editData.getCollege());
        } else {
            ctx.setVariable("basicCollege", portrait.getBasic().getCollege());
        }

        if (editData != null && editData.getMajor() != null) {
            ctx.setVariable("basicMajor", editData.getMajor());
        } else {
            ctx.setVariable("basicMajor", portrait.getBasic().getMajor());
        }

        if (editData != null && editData.getGrade() != null) {
            ctx.setVariable("basicGrade", editData.getGrade());
        } else {
            ctx.setVariable("basicGrade", portrait.getBasic().getGrade());
        }

        // 联系方式
        ctx.setVariable("phone", editData != null && editData.getPhone() != null ? editData.getPhone() : "");
        ctx.setVariable("email", editData != null && editData.getEmail() != null ? editData.getEmail() : "");

        // 头像 URL：把相对路径转成绝对路径，PDF 渲染时才能加载图片
        String avatarUrl = editData != null && editData.getAvatarUrl() != null ? editData.getAvatarUrl() : "";
        if (!avatarUrl.isEmpty() && !avatarUrl.startsWith("http") && !avatarUrl.startsWith("data:")) {
            avatarUrl = appBaseUrl + (avatarUrl.startsWith("/") ? avatarUrl : "/" + avatarUrl);
        }
        ctx.setVariable("avatarUrl", avatarUrl);

        // 个人简介
        ctx.setVariable("personalSummary",
                editData != null && editData.getPersonalSummary() != null ? editData.getPersonalSummary() : "");

        // 求职意向
        ctx.setVariable("expectedJob",
                editData != null && editData.getExpectedJob() != null ? editData.getExpectedJob() : "");
        ctx.setVariable("expectedCity",
                editData != null && editData.getExpectedCity() != null ? editData.getExpectedCity() : "");
        ctx.setVariable("expectedSalary",
                editData != null && editData.getExpectedSalary() != null ? editData.getExpectedSalary() : "");

        // 技能
        ctx.setVariable("skills", editData != null && editData.getSkills() != null ? editData.getSkills() : Collections.emptyList());

        // 模块开关（默认显示）
        ctx.setVariable("showHonors", editData != null && editData.getShowHonors() != null ? editData.getShowHonors() : true);
        ctx.setVariable("showCerts", editData != null && editData.getShowCerts() != null ? editData.getShowCerts() : true);
        ctx.setVariable("showServices", editData != null && editData.getShowServices() != null ? editData.getShowServices() : true);
        ctx.setVariable("showAcademic", editData != null && editData.getShowAcademic() != null ? editData.getShowAcademic() : true);
        ctx.setVariable("showIntention", editData != null && editData.getShowIntention() != null ? editData.getShowIntention() : true);

        // 实习经历 / 项目经历 / 自定义模块（用户自填，默认空列表，模板中 th:if 判空）
        ctx.setVariable("internships", editData != null && editData.getInternships() != null ? editData.getInternships() : Collections.emptyList());
        ctx.setVariable("projects", editData != null && editData.getProjects() != null ? editData.getProjects() : Collections.emptyList());
        ctx.setVariable("customSections", editData != null && editData.getCustomSections() != null ? editData.getCustomSections() : Collections.emptyList());

        ctx.setVariable("showInternships", editData != null && editData.getShowInternships() != null ? editData.getShowInternships() : true);
        ctx.setVariable("showProjects", editData != null && editData.getShowProjects() != null ? editData.getShowProjects() : true);
    }

    private String buildResumeHtml(PortraitVO portrait, String templateCode, ResumeEditDTO editData) {
        Context ctx = new Context(Locale.CHINA);

        // 基础数据
        ctx.setVariable("basic", portrait.getBasic());
        ctx.setVariable("points", portrait.getPoints());
        ctx.setVariable("military", portrait.getMilitary());
        ctx.setVariable("honors", portrait.getHonors());
        ctx.setVariable("certs", portrait.getCerts());
        ctx.setVariable("services", portrait.getServices());
        ctx.setVariable("academic", portrait.getAcademic());
        ctx.setVariable("intention", portrait.getIntention());
        ctx.setVariable("willingness", portrait.getWillingness());

        // 编辑数据覆盖
        applyEditData(ctx, portrait, editData);

        ctx.setVariable("generatedDate", java.time.LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        ctx.setVariable("formatter", DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String tpl;
        switch (templateCode != null ? templateCode : "military") {
            case "simple":
                tpl = "resume-simple";
                break;
            case "government":
                tpl = "resume-government";
                break;
            default:
                tpl = "resume-military";
        }

        return templateEngine.process(tpl, ctx);
    }

    private void generatePdfFromHtml(String html, String outputPath) throws Exception {
        // Thymeleaf 输出的已是完整 HTML 文档，不再额外包裹
        // 1. 把 HTML 实体替换为 XML 数字实体（Flying Saucer 要求严格 XHTML）
        String xhtml = html
                .replace("&nbsp;", "&#160;")
                .replace("&copy;", "&#169;")
                .replace("&reg;", "&#174;")
                .replace("&trade;", "&#8482;")
                .replace("&mdash;", "&#8212;")
                .replace("&ndash;", "&#8211;")
                .replace("&hellip;", "&#8230;")
                .replace("&laquo;", "&#171;")
                .replace("&raquo;", "&#187;")
                .replace("&ldquo;", "&#8220;")
                .replace("&rdquo;", "&#8221;")
                .replace("&lsquo;", "&#8216;")
                .replace("&rsquo;", "&#8217;");

        // 2. 注入字体 CSS 到 <head> 中（若没有 <head>，则插入到文档开头）
        String fontCssTag = "<style>" + loadFontCss() + "</style>";
        if (xhtml.contains("</head>")) {
            xhtml = xhtml.replace("</head>", fontCssTag + "</head>");
        } else if (xhtml.contains("<body")) {
            xhtml = xhtml.replaceFirst("<body", fontCssTag + "<body");
        } else {
            xhtml = fontCssTag + xhtml;
        }

        ITextRenderer renderer = new ITextRenderer();
        try {
            Path fontPathObj = Paths.get(fontPath);
            if (Files.exists(fontPathObj)) {
                renderer.getFontResolver().addFont(
                        fontPathObj.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        } catch (Exception e) {
            log.warn("字体加载失败，PDF 中文可能乱码");
        }

        renderer.setDocumentFromString(xhtml);
        // 设置 base URL，让 Flying Saucer 能解析相对路径的图片（如 /uploads/avatars/xxx.jpg）
        renderer.getSharedContext().setBaseURL(appBaseUrl);
        renderer.layout();

        try (OutputStream os = new FileOutputStream(outputPath)) {
            renderer.createPDF(os);
        }
    }

    private String loadFontCss() {
        StringBuilder css = new StringBuilder();
        css.append("* { font-family: 'SimSun', '宋体', 'Noto Sans SC', sans-serif; }");
        css.append("h1, h2, h3 { font-family: 'SimHei', '黑体', 'Noto Sans SC', sans-serif; }");
        css.append("body { width: 210mm; margin: 0; padding: 0; }");
        css.append(".page { width: 190mm; margin: 15mm auto; }");
        return css.toString();
    }

    /**
     * 把底层异常映射为用户可读的中文提示（不暴露技术细节）
     */
    private String mapPdfErrorMessage(Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        String lower = msg.toLowerCase();
        if (lower.contains("saxexception") || lower.contains("scanner state") || lower.contains("transformerexception")) {
            return "简历模板内容格式异常，请联系管理员";
        }
        if (lower.contains("filenotfoundexception") || lower.contains("permission denied") || lower.contains("access denied")) {
            return "文件写入权限不足，请联系管理员";
        }
        if (lower.contains("font") || lower.contains("字体")) {
            return "字体加载失败，PDF 中文可能乱码";
        }
        if (lower.contains("timeout") || lower.contains("timed out")) {
            return "生成超时，请稍后重试";
        }
        if (lower.contains("outofmemoryerror") || lower.contains("out of memory")) {
            return "服务器内存不足，请稍后重试";
        }
        return "请检查简历内容后重试";
    }
}
