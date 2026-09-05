package com.veteran.controller.app;

import com.veteran.common.Result;
import com.veteran.dto.ResumeEditDTO;
import com.veteran.entity.ResumeRecord;
import com.veteran.mapper.ResumeRecordMapper;
import com.veteran.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "小程序端-简历生成")
@RestController
@RequestMapping("/app/resume")
@RequiredArgsConstructor
public class AppResumeController {

    private final ResumeService resumeService;
    private final ResumeRecordMapper resumeRecordMapper;

    @Operation(summary = "获取简历模板列表")
    @GetMapping("/templates")
    public Result<java.util.List<Map<String, Object>>> templates() {
        return Result.ok(resumeService.getTemplates());
    }

    @Operation(summary = "获取简历编辑初始数据（从画像回填）")
    @GetMapping("/edit-data/{studentId}")
    public Result<Map<String, Object>> editData(@PathVariable Long studentId) {
        return Result.ok(resumeService.getEditInitData(studentId));
    }

    @Operation(summary = "获取学生简历生成历史记录")
    @GetMapping("/records/{studentId}")
    public Result<java.util.List<Map<String, Object>>> records(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(resumeService.getRecentRecords(studentId, limit));
    }

    @Operation(summary = "预览简历HTML")
    @GetMapping("/{studentId}/preview")
    public Result<String> preview(@PathVariable Long studentId,
                                  @RequestParam(defaultValue = "military") String template) {
        String html = resumeService.getPreviewHtml(studentId, template);
        return Result.ok(html);
    }

    @Operation(summary = "下载简历PDF（按模板即时生成）")
    @GetMapping("/{studentId}/download")
    public ResponseEntity<FileSystemResource> download(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "military") String template) {
        String path = resumeService.generatePdf(studentId, template);
        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(file);
        String filename = "resume_" + studentId + "_" + template + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Operation(summary = "按记录 ID 重新下载已生成的 PDF")
    @GetMapping("/record/{recordId}/download")
    public ResponseEntity<FileSystemResource> downloadByRecord(@PathVariable Long recordId) {
        ResumeRecord record = resumeRecordMapper.selectById(recordId);
        if (record == null || record.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(record.getPdfPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(file);
        String filename = "resume_" + record.getStudentId() + "_" + record.getTemplateCode() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Operation(summary = "预览简历HTML（含编辑数据）")
    @PostMapping("/preview")
    public Result<String> previewWithEdit(
            @RequestParam Long studentId,
            @RequestBody ResumeEditDTO editData) {
        String templateCode = editData.getTemplateCode() != null ? editData.getTemplateCode() : "military";
        String html = resumeService.getPreviewHtmlWithEdit(studentId, editData, templateCode);
        return Result.ok(html);
    }

    @Operation(summary = "生成简历PDF（含编辑数据）")
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateWithEdit(
            @RequestParam Long studentId,
            @RequestBody ResumeEditDTO editData) {
        String templateCode = editData.getTemplateCode() != null ? editData.getTemplateCode() : "military";
        String path = resumeService.generatePdfWithEdit(studentId, editData, templateCode);

        // 查询最新一条记录（generatePdfWithEdit 中已插入）
        java.util.List<Map<String, Object>> recent = resumeService.getRecentRecords(studentId, 1);
        Long recordId = null;
        if (recent != null && !recent.isEmpty()) {
            Object idObj = recent.get(0).get("id");
            if (idObj != null) recordId = Long.valueOf(idObj.toString());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("path", path);
        // 返回按记录 ID 下载的 URL，避免每次点下载都重新生成
        result.put("url", "/app/resume/record/" + recordId + "/download");
        result.put("recordId", recordId);
        return Result.ok(result);
    }
}
