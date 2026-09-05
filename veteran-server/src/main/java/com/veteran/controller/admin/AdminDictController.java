package com.veteran.controller.admin;

import com.veteran.annotation.OperationLog;
import com.veteran.common.BusinessException;
import com.veteran.common.Result;
import com.veteran.dto.DictItemDTO;
import com.veteran.entity.*;
import com.veteran.service.DictService;
import com.veteran.service.impl.DictServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-字典管理")
@RestController
@RequestMapping("/admin/dict")
@RequiredArgsConstructor
public class AdminDictController {

    private final DictService dictService;
    private final DictServiceImpl dictServiceImpl;

    @Operation(summary = "查询兵种字典")
    @GetMapping("/branch")
    public Result<List<DictBranch>> listBranch() {
        return Result.ok(dictService.listAll(DictBranch.class));
    }

    @Operation(summary = "查询荣誉字典")
    @GetMapping("/honor")
    public Result<List<DictHonor>> listHonor() {
        return Result.ok(dictService.listAll(DictHonor.class));
    }

    @Operation(summary = "查询证书字典")
    @GetMapping("/cert")
    public Result<List<DictCert>> listCert() {
        return Result.ok(dictService.listAll(DictCert.class));
    }

    @Operation(summary = "查询服务岗位字典")
    @GetMapping("/post-type")
    public Result<List<DictPostType>> listPostType() {
        return Result.ok(dictService.listAll(DictPostType.class));
    }

    @Operation(summary = "查询就业岗位字典")
    @GetMapping("/job-type")
    public Result<List<DictJobType>> listJobType() {
        return Result.ok(dictService.listAll(DictJobType.class));
    }

    @Operation(summary = "查询荣誉类别字典")
    @GetMapping("/honor-category")
    public Result<List<DictHonorCategory>> listHonorCategory() {
        return Result.ok(dictService.listAll(DictHonorCategory.class));
    }

    @Operation(summary = "查询骨干职务字典")
    @GetMapping("/leader-post")
    public Result<List<DictLeaderPost>> listLeaderPost() {
        return Result.ok(dictService.listAll(DictLeaderPost.class));
    }

    @OperationLog("刷新字典缓存")
    @Operation(summary = "刷新字典缓存")
    @PostMapping("/refresh")
    public Result<Void> refreshCache() {
        dictService.refreshCache();
        return Result.ok();
    }

    // ========== 字典 CRUD ==========

    @OperationLog("新增字典条目")
    @Operation(summary = "新增字典条目")
    @PostMapping("/{dictType}")
    public Result<Void> addItem(@PathVariable String dictType, @RequestBody DictItemDTO dto) {
        if (dto.getCode() == null || dto.getCode().isEmpty()) {
            throw BusinessException.badRequest("字典编码不能为空");
        }
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw BusinessException.badRequest("字典名称不能为空");
        }
        Class<?> entityClass = dictServiceImpl.getEntityClass(dictType);
        @SuppressWarnings("rawtypes")
        com.baomidou.mybatisplus.core.mapper.BaseMapper mapper = dictServiceImpl.getMapper(dictType);
        dictService.addDictItem(entityClass, mapper, dto);
        return Result.ok();
    }

    @OperationLog("更新字典条目")
    @Operation(summary = "更新字典条目")
    @PutMapping("/{dictType}/{id}")
    public Result<Void> updateItem(@PathVariable String dictType, @PathVariable Long id,
                                   @RequestBody DictItemDTO dto) {
        Class<?> entityClass = dictServiceImpl.getEntityClass(dictType);
        @SuppressWarnings("rawtypes")
        com.baomidou.mybatisplus.core.mapper.BaseMapper mapper = dictServiceImpl.getMapper(dictType);
        dictService.updateDictItem(entityClass, mapper, id, dto);
        return Result.ok();
    }

    @OperationLog("删除字典条目")
    @Operation(summary = "删除字典条目")
    @DeleteMapping("/{dictType}/{id}")
    public Result<Void> deleteItem(@PathVariable String dictType, @PathVariable Long id) {
        Class<?> entityClass = dictServiceImpl.getEntityClass(dictType);
        @SuppressWarnings("rawtypes")
        com.baomidou.mybatisplus.core.mapper.BaseMapper mapper = dictServiceImpl.getMapper(dictType);
        dictService.deleteDictItem(entityClass, mapper, id);
        return Result.ok();
    }
}
