package com.veteran.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.PageResult;
import com.veteran.common.Result;
import com.veteran.dto.StudentDTO;
import com.veteran.dto.StudentQueryDTO;
import com.veteran.service.StudentService;
import com.veteran.vo.StudentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理端-学生管理")
@RestController
@RequestMapping("/admin/student")
@RequiredArgsConstructor
public class AdminStudentController {

    private final StudentService studentService;

    @Operation(summary = "分页查询学生")
    @GetMapping("/page")
    public Result<Page<StudentVO>> page(StudentQueryDTO query) {
        return Result.ok(studentService.pageQuery(query));
    }

    @Operation(summary = "查询学生详情")
    @GetMapping("/{id}")
    public Result<StudentVO> getById(@PathVariable Long id) {
        return Result.ok(studentService.getById(id));
    }

    @Operation(summary = "新增学生")
    @PostMapping
    public Result<StudentVO> create(@Valid @RequestBody StudentDTO dto) {
        return Result.ok(studentService.create(dto));
    }

    @Operation(summary = "更新学生")
    @PutMapping("/{id}")
    public Result<StudentVO> update(@PathVariable Long id, @Valid @RequestBody StudentDTO dto) {
        return Result.ok(studentService.update(id, dto));
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "批量导入学生(Excel)")
    @PostMapping("/import")
    public Result<String> batchImport(@RequestParam("file") MultipartFile file) {
        return Result.ok("批量导入功能需集成EasyExcel解析");
    }
}