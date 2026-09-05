package com.veteran.controller.admin;

import com.veteran.annotation.OperationLog;
import com.veteran.common.Result;
import com.veteran.entity.SysConfig;
import com.veteran.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "管理端-系统配置")
@RestController
@RequestMapping("/admin/sys-config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "获取所有系统配置")
    @GetMapping
    public Result<List<SysConfig>> listAll() {
        return Result.ok(sysConfigService.listAll());
    }

    @OperationLog("更新系统配置")
    @Operation(summary = "批量更新系统配置")
    @PutMapping
    public Result<Void> batchUpdate(@RequestBody List<SysConfig> configs) {
        sysConfigService.batchUpdate(configs);
        return Result.ok();
    }

    @Operation(summary = "获取单个配置值")
    @GetMapping("/{key}")
    public Result<Map<String, String>> getByKey(@PathVariable String key) {
        String value = sysConfigService.getConfigValue(key);
        return Result.ok(Map.of(key, value != null ? value : ""));
    }
}
