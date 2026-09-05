package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.BusinessException;
import com.veteran.common.utils.AesUtil;
import com.veteran.common.utils.MonthDateUtil;
import com.veteran.dto.StudentDTO;
import com.veteran.dto.StudentQueryDTO;
import com.veteran.entity.Student;
import com.veteran.mapper.StudentMapper;
import com.veteran.service.PointsService;
import com.veteran.service.StudentService;
import com.veteran.vo.StudentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final AesUtil aesUtil;
    private final PointsService pointsService;

    @Override
    public Page<StudentVO> pageQuery(StudentQueryDTO query) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getStudentNo()), Student::getStudentNo, query.getStudentNo())
                .like(StringUtils.hasText(query.getName()), Student::getName, query.getName())
                .eq(StringUtils.hasText(query.getCollege()), Student::getCollege, query.getCollege())
                .eq(StringUtils.hasText(query.getGrade()), Student::getGrade, query.getGrade())
                .eq(query.getStatus() != null, Student::getStatus, query.getStatus())
                .orderByDesc(Student::getCreateTime);

        Page<Student> page = new Page<>(query.getPage(), query.getSize());
        Page<Student> result = studentMapper.selectPage(page, wrapper);

        Page<StudentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<StudentVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public StudentVO getById(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw BusinessException.notFound("学生不存在");
        }
        return toVO(student);
    }

    @Override
    @Transactional
    public StudentVO create(StudentDTO dto) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, dto.getStudentNo());
        if (studentMapper.selectCount(wrapper) > 0) {
            throw BusinessException.conflict("学号已存在");
        }

        Student student = new Student();
        BeanUtils.copyProperties(dto, student);
        // 年月字段显式归一为当月 1 日，避免 BeanUtils 隐式转换失败导致静默丢失
        student.setBirthDate(MonthDateUtil.toFirstDay(dto.getBirthDate()));
        student.setEnrollDate(MonthDateUtil.toFirstDay(dto.getEnrollDate()));
        student.setRetireDate(MonthDateUtil.toFirstDay(dto.getRetireDate()));
        student.setPhone(aesUtil.encrypt(dto.getPhone()));
        student.setPhoneHash(aesUtil.hashPhone(dto.getPhone()));
        student.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        studentMapper.insert(student);
        log.info("创建学生: id={}, studentNo={}", student.getId(), student.getStudentNo());
        return toVO(student);
    }

    @Override
    @Transactional
    public StudentVO update(Long id, StudentDTO dto) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw BusinessException.notFound("学生不存在");
        }

        if (!student.getStudentNo().equals(dto.getStudentNo())) {
            LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Student::getStudentNo, dto.getStudentNo());
            if (studentMapper.selectCount(wrapper) > 0) {
                throw BusinessException.conflict("学号已存在");
            }
        }

        BeanUtils.copyProperties(dto, student);
        student.setId(id);
        // 年月字段显式归一为当月 1 日，避免 BeanUtils 隐式转换失败导致静默丢失
        student.setBirthDate(MonthDateUtil.toFirstDay(dto.getBirthDate()));
        student.setEnrollDate(MonthDateUtil.toFirstDay(dto.getEnrollDate()));
        student.setRetireDate(MonthDateUtil.toFirstDay(dto.getRetireDate()));
        if (StringUtils.hasText(dto.getPhone())) {
            student.setPhone(aesUtil.encrypt(dto.getPhone()));
            student.setPhoneHash(aesUtil.hashPhone(dto.getPhone()));
        }

        studentMapper.updateById(student);
        log.info("更新学生: id={}", id);
        return toVO(student);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw BusinessException.notFound("学生不存在");
        }
        studentMapper.deleteById(id);
        log.info("删除学生: id={}", id);
    }

    @Override
    @Transactional
    public void batchImport(List<StudentDTO> list) {
        int successCount = 0;
        int failCount = 0;
        for (StudentDTO dto : list) {
            try {
                create(dto);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("导入学生失败: studentNo={}, error={}", dto.getStudentNo(), e.getMessage());
            }
        }
        log.info("批量导入完成: 成功={}, 失败={}", successCount, failCount);
    }

    private StudentVO toVO(Student student) {
        StudentVO vo = new StudentVO();
        BeanUtils.copyProperties(student, vo);
        try {
            String phone = aesUtil.decrypt(student.getPhone());
            vo.setPhone(aesUtil.maskPhone(phone));
        } catch (Exception e) {
            vo.setPhone("***");
        }
        vo.setTotalPoints((long) pointsService.getTotalPoints(student.getId()));
        return vo;
    }
}