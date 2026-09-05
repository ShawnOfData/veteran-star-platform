package com.veteran.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.Result;
import com.veteran.dto.AnnouncementDTO;
import com.veteran.service.AnnouncementService;
import com.veteran.vo.AnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "管理端-公告管理")
@RestController
@RequestMapping("/admin/announcement")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "分页查询公告")
    @GetMapping("/page")
    public Result<Page<AnnouncementVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String keyword) {
        return Result.ok(announcementService.pageQuery(page, size, keyword));
    }

    @Operation(summary = "查询公告详情")
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getById(@PathVariable Long id) {
        return Result.ok(announcementService.getById(id));
    }

    @Operation(summary = "创建公告")
    @PostMapping
    public Result<AnnouncementVO> create(@Valid @RequestBody AnnouncementDTO dto) {
        return Result.ok(announcementService.create(dto));
    }

    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    public Result<AnnouncementVO> update(@PathVariable Long id, @Valid @RequestBody AnnouncementDTO dto) {
        return Result.ok(announcementService.update(id, dto));
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "发布公告")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publish(id);
        return Result.ok();
    }

    @Operation(summary = "关闭公告")
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        announcementService.close(id);
        return Result.ok();
    }
}
