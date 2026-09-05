package com.veteran.controller.admin;

import com.veteran.common.Result;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Tag(name = "管理端-简历生成")
@RestController
@RequestMapping("/admin/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "获取简历模板列表")
    @GetMapping("/templates")
    public Result<java.util.List<Map<String, Object>>> templates() {
        return Result.ok(resumeService.getTemplates());
    }

    @Operation(summary = "预览简历HTML")
    @GetMapping("/{studentId}/preview")
    public Result<String> preview(@PathVariable Long studentId,
                                  @RequestParam(defaultValue = "military") String template) {
        String html = resumeService.getPreviewHtml(studentId, template);
        return Result.ok(html);
    }

    @Operation(summary = "下载简历PDF")
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
}
