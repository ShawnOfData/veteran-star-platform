package com.veteran.controller.app;

import com.veteran.common.Result;
import com.veteran.entity.*;
import com.veteran.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小程序端-字典")
@RestController
@RequestMapping("/app/dict")
@RequiredArgsConstructor
public class AppDictController {

    private final DictService dictService;

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
}