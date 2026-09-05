package com.veteran.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.Result;
import com.veteran.dto.ApplicationDTO;
import com.veteran.service.OpportunityService;
import com.veteran.vo.OpportunityApplicationVO;
import com.veteran.vo.OpportunityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Tag(name = "小程序端-机会")
@RestController
@RequestMapping("/app/opportunity")
@RequiredArgsConstructor
public class AppOpportunityController {

    private final OpportunityService opportunityService;

    @Operation(summary = "查看已发布机会列表")
    @GetMapping("/list")
    public Result<Page<OpportunityVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long studentId) {
        return Result.ok(opportunityService.listPublished(page, size, type, keyword, studentId));
    }

    @Operation(summary = "查看机会详情")
    @GetMapping("/{id}")
    public Result<OpportunityVO> getById(@PathVariable Long id) {
        return Result.ok(opportunityService.getById(id));
    }

    @Operation(summary = "报名机会")
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody ApplicationDTO dto) {
        opportunityService.apply(dto.getOpportunityId(), dto.getStudentId(), dto.getRemark());
        return Result.ok();
    }

    @Operation(summary = "取消报名")
    @PutMapping("/cancel/{applicationId}")
    public Result<Void> cancelApply(@PathVariable Long applicationId,
                                    @RequestParam Long studentId) {
        opportunityService.cancelApply(applicationId, studentId);
        return Result.ok();
    }

    @Operation(summary = "查看我的报名列表")
    @GetMapping("/my-applications")
    public Result<Page<OpportunityApplicationVO>> getMyApplications(@RequestParam Long studentId,
                                                          @RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(opportunityService.getMyApplications(studentId, page, size));
    }

    @Operation(summary = "收藏机会")
    @PostMapping("/favorite")
    public Result<Void> favorite(@RequestBody Map<String, Long> body) {
        opportunityService.favorite(body.get("studentId"), body.get("opportunityId"));
        return Result.ok();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorite/{studentId}/{opportunityId}")
    public Result<Void> unfavorite(@PathVariable Long studentId, @PathVariable Long opportunityId) {
        opportunityService.unfavorite(studentId, opportunityId);
        return Result.ok();
    }

    @Operation(summary = "查看我的收藏列表")
    @GetMapping("/my-favorites")
    public Result<Page<OpportunityVO>> getMyFavorites(@RequestParam Long studentId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(opportunityService.getMyFavorites(studentId, page, size));
    }

    @Operation(summary = "增加浏览次数")
    @PutMapping("/view/{id}")
    public Result<Void> incrementViewCount(@PathVariable Long id) {
        opportunityService.incrementViewCount(id);
        return Result.ok();
    }
}
