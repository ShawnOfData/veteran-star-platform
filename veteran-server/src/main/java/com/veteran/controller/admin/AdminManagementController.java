package com.veteran.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.annotation.OperationLog;
import com.veteran.common.BusinessException;
import com.veteran.common.Result;
import com.veteran.entity.AdminUser;
import com.veteran.mapper.AdminUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-管理员账号管理")
@RestController
@RequestMapping("/admin/admin-account")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "管理员列表")
    @GetMapping("/list")
    public Result<List<AdminUser>> list() {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AdminUser::getCreateTime);
        return Result.ok(adminUserMapper.selectList(wrapper));
    }

    @OperationLog("新增管理员")
    @Operation(summary = "新增管理员")
    @PostMapping
    public Result<Void> create(@RequestBody AdminUser admin) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, admin.getUsername());
        if (adminUserMapper.selectCount(wrapper) > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        if (admin.getRole() == null || admin.getRole().isEmpty()) {
            admin.setRole("admin");
        }
        adminUserMapper.insert(admin);
        return Result.ok();
    }

    @OperationLog("更新管理员")
    @Operation(summary = "更新管理员")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AdminUser admin) {
        AdminUser exist = adminUserMapper.selectById(id);
        if (exist == null) {
            throw BusinessException.notFound("管理员不存在");
        }
        if (StringUtils.hasText(admin.getPassword())) {
            exist.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        if (StringUtils.hasText(admin.getRole())) {
            exist.setRole(admin.getRole());
        }
        adminUserMapper.updateById(exist);
        return Result.ok();
    }

    @OperationLog("删除管理员")
    @Operation(summary = "删除管理员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        AdminUser exist = adminUserMapper.selectById(id);
        if (exist == null) {
            throw BusinessException.notFound("管理员不存在");
        }
        adminUserMapper.deleteById(id);
        return Result.ok();
    }
}