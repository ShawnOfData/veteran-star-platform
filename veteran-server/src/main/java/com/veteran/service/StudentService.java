package com.veteran.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.dto.*;
import com.veteran.vo.*;

import java.util.List;

public interface StudentService {

    Page<StudentVO> pageQuery(StudentQueryDTO query);

    StudentVO getById(Long id);

    StudentVO create(StudentDTO dto);

    StudentVO update(Long id, StudentDTO dto);

    void delete(Long id);

    void batchImport(List<StudentDTO> list);
}