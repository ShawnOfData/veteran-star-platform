package com.veteran.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.Result;
import com.veteran.dto.OpportunityDTO;
import com.veteran.service.OpportunityService;
import com.veteran.vo.OpportunityApplicationVO;
import com.veteran.vo.OpportunityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Tag(name = "管理端-机会管理")
@RestController
@RequestMapping("/admin/opportunity")
@RequiredArgsConstructor
public class AdminOpportunityController {

    private final OpportunityService opportunityService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private String getUploadDir() {
        String dir = uploadDir;
        if (!dir.startsWith("/") && !dir.contains(":")) {
            dir = System.getProperty("user.dir") + "/" + dir;
        }
        return dir + "/covers/";
    }

    @Operation(summary = "上传封面图")
    @PostMapping("/upload-cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("请选择文件");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            ext = ".jpg";
        }
        String filename = UUID.randomUUID().toString().replaceAll("-", "") + ext;
        String dirPath = getUploadDir();
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        try {
            file.transferTo(new File(dirPath + filename));
            return Result.ok("/uploads/covers/" + filename);
        } catch (IOException e) {
            log.error("封面上传失败", e);
            return Result.fail("上传失败");
        }
    }

    @Operation(summary = "分页查询机会")
    @GetMapping("/page")
    public Result<Page<OpportunityVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) String title) {
        return Result.ok(opportunityService.pageQuery(page, size, type, status, title));
    }

    @Operation(summary = "查询机会详情")
    @GetMapping("/{id}")
    public Result<OpportunityVO> getById(@PathVariable Long id) {
        return Result.ok(opportunityService.getById(id));
    }

    @Operation(summary = "创建机会")
    @PostMapping
    public Result<OpportunityVO> create(@Valid @RequestBody OpportunityDTO dto, Authentication authentication) {
        Long publisherId = (Long) authentication.getCredentials();
        return Result.ok(opportunityService.create(dto, publisherId));
    }

    @Operation(summary = "更新机会")
    @PutMapping("/{id}")
    public Result<OpportunityVO> update(@PathVariable Long id, @Valid @RequestBody OpportunityDTO dto) {
        return Result.ok(opportunityService.update(id, dto));
    }

    @Operation(summary = "删除机会")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "发布机会")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        opportunityService.publish(id);
        return Result.ok();
    }

    @Operation(summary = "关闭机会")
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        opportunityService.close(id);
        return Result.ok();
    }

    @Operation(summary = "分页查询报名列表")
    @GetMapping("/applications/page")
    public Result<Page<OpportunityApplicationVO>> getApplicationPage(@RequestParam(defaultValue = "1") Integer page,
                                                                      @RequestParam(defaultValue = "10") Integer size,
                                                                      @RequestParam(required = false) Long opportunityId,
                                                                      @RequestParam(required = false) Integer status,
                                                                      @RequestParam(required = false) String keyword) {
        return Result.ok(opportunityService.getApplicationPage(page, size, opportunityId, status, keyword));
    }

    @Operation(summary = "审核报名")
    @PutMapping("/applications/{applicationId}/review")
    public Result<Void> reviewApply(@PathVariable Long applicationId, @RequestParam Integer status) {
        opportunityService.reviewApply(applicationId, status);
        return Result.ok();
    }
}
