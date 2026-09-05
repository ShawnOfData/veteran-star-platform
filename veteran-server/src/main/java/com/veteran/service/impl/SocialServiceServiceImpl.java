package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.BusinessException;
import com.veteran.dto.SocialServiceRecordDTO;
import com.veteran.dto.StudentSkillDTO;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.PointsService;
import com.veteran.service.SocialServiceService;
import com.veteran.vo.PointsDetailVO;
import com.veteran.vo.SocialServiceRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialServiceServiceImpl implements SocialServiceService {

    private final SocialServiceRecordMapper recordMapper;
    private final StudentSkillMapper skillMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final DictCertMapper dictCertMapper;
    private final PointsService pointsService;
    private final StudentMapper studentMapper;

    @Override
    @Transactional
    public SocialServiceRecord create(SocialServiceRecordDTO dto) {
        SocialServiceRecord record = new SocialServiceRecord();
        BeanUtils.copyProperties(dto, record);
        record.setStatus(0);
        recordMapper.insert(record);
        log.info("创建社会服务记录: id={}, studentId={}", record.getId(), dto.getStudentId());
        return record;
    }

    @Override
    @Transactional
    public void review(Long recordId, Integer status, String rejectReason, Long reviewerId) {
        SocialServiceRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw BusinessException.notFound("服务记录不存在");
        }
        if (record.getStatus() != 0) {
            throw BusinessException.badRequest("该记录已审核");
        }

        record.setStatus(status);
        record.setReviewerId(reviewerId);
        record.setReviewTime(LocalDateTime.now());

        if (status == 1) {
            int points = record.getDurationHours().intValue() * 2;
            record.setPointsAwarded(points);
            pointsService.addPoints(record.getStudentId(), points,
                    "SERVICE", record.getId(), "social_service_record", "reviewer",
                    "社会服务积分: " + record.getActivityType());
        } else if (status == 2) {
            record.setRejectReason(rejectReason);
        }

        recordMapper.updateById(record);
        log.info("审核服务记录: id={}, status={}", recordId, status);
    }

    @Override
    public List<SocialServiceRecord> listByStudentId(Long studentId) {
        LambdaQueryWrapper<SocialServiceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SocialServiceRecord::getStudentId, studentId)
                .orderByDesc(SocialServiceRecord::getServiceDate);
        return recordMapper.selectList(wrapper);
    }

    @Override
    public Page<SocialServiceRecordVO> pageRecords(Integer page, Integer size, Integer status,
                                                   String studentName, String startDate, String endDate) {
        LambdaQueryWrapper<SocialServiceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SocialServiceRecord::getStatus, status)
                .ge(StringUtils.hasText(startDate), SocialServiceRecord::getServiceDate, startDate)
                .le(StringUtils.hasText(endDate), SocialServiceRecord::getServiceDate, endDate)
                .orderByDesc(SocialServiceRecord::getCreateTime);

        Page<SocialServiceRecord> pageParam = new Page<>(page, size);
        Page<SocialServiceRecord> result = recordMapper.selectPage(pageParam, wrapper);

        List<Long> studentIds = result.getRecords().stream()
                .map(SocialServiceRecord::getStudentId)
                .distinct().collect(Collectors.toList());

        Map<Long, Student> studentMap = loadStudentMap(studentIds, studentName);

        boolean hasNameFilter = StringUtils.hasText(studentName);
        Page<SocialServiceRecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SocialServiceRecordVO> voList = result.getRecords().stream()
                .filter(r -> !hasNameFilter || studentMap.containsKey(r.getStudentId()))
                .map(r -> {
                    SocialServiceRecordVO vo = new SocialServiceRecordVO();
                    BeanUtils.copyProperties(r, vo);
                    vo.setPointsApplied(r.getDurationHours() != null
                            ? r.getDurationHours().intValue() * 2 : 0);
                    Student s = studentMap.get(r.getStudentId());
                    if (s != null) {
                        vo.setStudentName(s.getName());
                        vo.setStudentNo(s.getStudentNo());
                    }
                    return vo;
                }).collect(Collectors.toList());
        voPage.setRecords(voList);
        voPage.setTotal(hasNameFilter ? voList.size() : result.getTotal());
        return voPage;
    }

    private Map<Long, Student> loadStudentMap(List<Long> studentIds, String studentName) {
        if (studentIds.isEmpty()) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(Student::getId, studentIds);
        if (StringUtils.hasText(studentName)) {
            studentWrapper.like(Student::getName, studentName);
        }
        return studentMapper.selectList(studentWrapper).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
    }

    @Override
    @Transactional
    public StudentSkill addSkill(StudentSkillDTO dto) {
        DictCert dictCert = dictCertMapper.selectById(dto.getCertCode());
        if (dictCert == null) {
            throw BusinessException.notFound("证书类型不存在");
        }

        StudentSkill skill = new StudentSkill();
        BeanUtils.copyProperties(dto, skill);
        skill.setPointsAwarded(dictCert.getDefaultPoints());
        skillMapper.insert(skill);

        pointsService.addPoints(dto.getStudentId(), dictCert.getDefaultPoints(),
                "CERT", skill.getId(), "student_skill", "system",
                "获得证书: " + dictCert.getName());

        log.info("添加技能证书: id={}, certCode={}, points={}", skill.getId(), dto.getCertCode(), dictCert.getDefaultPoints());
        return skill;
    }

    @Override
    @Transactional
    public void removeSkill(Long skillId) {
        StudentSkill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw BusinessException.notFound("证书不存在");
        }

        skillMapper.deleteById(skillId);

        if (skill.getPointsAwarded() > 0) {
            pointsService.deductPoints(skill.getStudentId(), skill.getPointsAwarded(),
                    "CERT_REVOKE", skillId, "student_skill", "system",
                    "撤销证书积分");
        }

        log.info("删除技能证书: id={}", skillId);
    }

    @Override
    public List<StudentSkill> listSkills(Long studentId) {
        LambdaQueryWrapper<StudentSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentSkill::getStudentId, studentId)
                .orderByDesc(StudentSkill::getObtainDate);
        return skillMapper.selectList(wrapper);
    }

    @Override
    public List<PointsDetailVO> listPointsDetail(Long studentId, Integer page, Integer size) {
        LambdaQueryWrapper<PointsDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsDetail::getStudentId, studentId)
                .orderByDesc(PointsDetail::getCreateTime);

        Page<PointsDetail> pageParam = new Page<>(page, size);
        Page<PointsDetail> result = pointsDetailMapper.selectPage(pageParam, wrapper);

        return result.getRecords().stream().map(detail -> {
            PointsDetailVO vo = new PointsDetailVO();
            BeanUtils.copyProperties(detail, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}