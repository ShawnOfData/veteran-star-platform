package com.veteran.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.dto.SocialServiceRecordDTO;
import com.veteran.dto.StudentSkillDTO;
import com.veteran.entity.*;
import com.veteran.vo.PointsDetailVO;
import com.veteran.vo.SocialServiceRecordVO;

import java.util.List;

public interface SocialServiceService {

    SocialServiceRecord create(SocialServiceRecordDTO dto);

    void review(Long recordId, Integer status, String rejectReason, Long reviewerId);

    List<SocialServiceRecord> listByStudentId(Long studentId);

    Page<SocialServiceRecordVO> pageRecords(Integer page, Integer size, Integer status,
                                            String studentName, String startDate, String endDate);

    StudentSkill addSkill(StudentSkillDTO dto);

    void removeSkill(Long skillId);

    List<StudentSkill> listSkills(Long studentId);

    List<PointsDetailVO> listPointsDetail(Long studentId, Integer page, Integer size);
}