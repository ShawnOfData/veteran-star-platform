package com.veteran.controller.admin;

import com.veteran.common.Result;
import com.veteran.dto.MilitaryHonorDTO;
import com.veteran.dto.ServiceExperienceDTO;
import com.veteran.service.ServiceExperienceService;
import com.veteran.vo.MilitaryHonorVO;
import com.veteran.vo.ServiceExperienceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理端-服役经历管理")
@RestController
@RequestMapping("/admin/service-experience")
@RequiredArgsConstructor
public class AdminServiceExperienceController {

    private final ServiceExperienceService serviceExperienceService;

    @Operation(summary = "查询学生服役经历列表")
    @GetMapping("/list/{studentId}")
    public Result<List<ServiceExperienceVO>> listByStudentId(@PathVariable Long studentId) {
        return Result.ok(serviceExperienceService.listByStudentId(studentId));
    }

    @Operation(summary = "新增服役经历")
    @PostMapping
    public Result<ServiceExperienceVO> create(@Valid @RequestBody ServiceExperienceDTO dto) {
        return Result.ok(serviceExperienceService.create(dto));
    }

    @Operation(summary = "更新服役经历")
    @PutMapping("/{id}")
    public Result<ServiceExperienceVO> update(@PathVariable Long id, @Valid @RequestBody ServiceExperienceDTO dto) {
        return Result.ok(serviceExperienceService.update(id, dto));
    }

    @Operation(summary = "删除服役经历")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceExperienceService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "添加荣誉")
    @PostMapping("/honor")
    public Result<MilitaryHonorVO> addHonor(@Valid @RequestBody MilitaryHonorDTO dto) {
        return Result.ok(serviceExperienceService.addHonor(dto));
    }

    @Operation(summary = "删除荣誉")
    @DeleteMapping("/honor/{honorId}")
    public Result<Void> removeHonor(@PathVariable Long honorId) {
        serviceExperienceService.removeHonor(honorId);
        return Result.ok();
    }

    @Operation(summary = "查询服役经历的荣誉列表")
    @GetMapping("/honor/list/{serviceExperienceId}")
    public Result<List<MilitaryHonorVO>> listHonors(@PathVariable Long serviceExperienceId) {
        return Result.ok(serviceExperienceService.listHonors(serviceExperienceId));
    }
}