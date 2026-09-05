package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.PointsService;
import com.veteran.service.ProfileService;
import com.veteran.vo.PortraitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final StudentMapper studentMapper;
    private final ServiceExperienceMapper serviceExperienceMapper;
    private final MilitaryHonorMapper militaryHonorMapper;
    private final MilitaryPositionMapper militaryPositionMapper;
    private final StudentSkillMapper skillMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final SocialServiceRecordMapper socialServiceRecordMapper;
    private final AcademicPerformanceMapper academicPerformanceMapper;
    private final StudentServiceWillingnessMapper willingnessMapper;
    private final StudentEmploymentIntentionMapper intentionMapper;
    private final DictBranchMapper dictBranchMapper;
    private final DictHonorMapper dictHonorMapper;
    private final DictHonorCategoryMapper dictHonorCategoryMapper;
    private final DictLeaderPostMapper dictLeaderPostMapper;
    private final DictCertMapper dictCertMapper;
    private final PointsService pointsService;

    private static final DateTimeFormatter YM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /** 年月精度字段输出 YYYY-MM（历史精确到日的数据仅截取年月展示） */
    private String ym(LocalDate date) {
        return date != null ? date.format(YM_FORMATTER) : null;
    }

    @Override
    public PortraitVO getPortrait(Long studentId) {
        PortraitVO vo = new PortraitVO();

        // ===== 1. 基本信息 =====
        Student student = studentMapper.selectById(studentId);
        if (student == null) return vo;

        PortraitVO.BasicInfo basic = new PortraitVO.BasicInfo();
        basic.setId(student.getId());
        basic.setStudentNo(student.getStudentNo());
        basic.setName(student.getName());
        basic.setGender(student.getGender());
        basic.setEthnicity(student.getEthnicity());
        basic.setBirthDate(ym(student.getBirthDate()));
        basic.setNativePlace(student.getNativePlace());
        basic.setPoliticalStatus(student.getPoliticalStatus());
        basic.setCollege(student.getCollege());
        basic.setMajor(student.getMajor());
        basic.setGrade(student.getGrade());
        basic.setStatus(student.getStatus());
        basic.setRetireDate(ym(student.getRetireDate()));
        vo.setBasic(basic);

        // ===== 2. 积分总览 =====
        int total = pointsService.getTotalPoints(studentId);
        PortraitVO.PointsInfo points = new PortraitVO.PointsInfo();
        points.setTotal(total);
        points.setHonorPoints(sumByType(studentId, "HONOR"));
        points.setCertPoints(sumByType(studentId, "CERT"));
        points.setServicePoints(sumByType(studentId, "SERVICE"));
        points.setAcademicPoints(sumByType(studentId, "ACADEMIC"));
        points.setLevel(getPointLevel(total));
        vo.setPoints(points);

        // ===== 3. 服役经历（取第一条已审核通过的） =====
        LambdaQueryWrapper<ServiceExperience> seW = new LambdaQueryWrapper<ServiceExperience>()
                .eq(ServiceExperience::getStudentId, studentId)
                .eq(ServiceExperience::getStatus, 1)
                .orderByDesc(ServiceExperience::getStartDate);
        ServiceExperience se = serviceExperienceMapper.selectList(seW).stream()
                .findFirst().orElse(null);
        if (se != null) {
            PortraitVO.MilitaryInfo military = new PortraitVO.MilitaryInfo();
            DictBranch branch = dictBranchMapper.selectById(se.getBranchCode());
            military.setBranchName(branch != null ? branch.getName() : "");
            if (se.getLeaderPostCode() != null) {
                DictLeaderPost leaderPost = dictLeaderPostMapper.selectById(se.getLeaderPostCode());
                military.setLeaderPostName(leaderPost != null ? leaderPost.getName() : "");
            } else {
                military.setLeaderPostName("");
            }
            military.setStartDate(ym(se.getStartDate()));
            military.setEndDate(ym(se.getEndDate()));
            if (se.getServiceYears() != null) military.setServiceYears(se.getServiceYears() + "年");
            // 取第一个职务
            LambdaQueryWrapper<MilitaryPosition> posW = new LambdaQueryWrapper<MilitaryPosition>()
                    .eq(MilitaryPosition::getServiceExperienceId, se.getId());
            List<MilitaryPosition> positions = militaryPositionMapper.selectList(posW);
            if (!positions.isEmpty()) military.setPosition(positions.get(0).getPositionName());
            vo.setMilitary(military);
        }

        // ===== 4. 荣誉列表（通过服役经历关联，只取已审核通过的） =====
        List<ServiceExperience> allSe = serviceExperienceMapper.selectList(
                new LambdaQueryWrapper<ServiceExperience>()
                        .eq(ServiceExperience::getStudentId, studentId));
        Set<Long> seIds = allSe.stream().map(ServiceExperience::getId).collect(Collectors.toSet());

        List<PortraitVO.HonorInfo> honors = new ArrayList<>();
        if (!seIds.isEmpty()) {
            List<MilitaryHonor> honorList = militaryHonorMapper.selectList(
                    new LambdaQueryWrapper<MilitaryHonor>()
                            .in(MilitaryHonor::getServiceExperienceId, seIds)
                            .eq(MilitaryHonor::getStatus, 1)
                            .eq(MilitaryHonor::getDeleted, 0)
                            .orderByDesc(MilitaryHonor::getAwardDate));
            for (MilitaryHonor h : honorList) {
                PortraitVO.HonorInfo info = new PortraitVO.HonorInfo();
                DictHonor dh = dictHonorMapper.selectById(h.getHonorCode());
                info.setName(dh != null ? dh.getName() : "");
                if (h.getHonorCategoryCode() != null) {
                    DictHonorCategory hc = dictHonorCategoryMapper.selectById(h.getHonorCategoryCode());
                    info.setCategoryName(hc != null ? hc.getName() : "");
                }
                info.setAwardDate(ym(h.getAwardDate()));
                info.setPoints(h.getPointsAwarded());
                honors.add(info);
            }
        }
        vo.setHonors(honors);

        // ===== 5. 证书列表 =====
        List<PortraitVO.CertInfo> certs = skillMapper.selectList(
                new LambdaQueryWrapper<StudentSkill>()
                        .eq(StudentSkill::getStudentId, studentId)
                        .eq(StudentSkill::getStatus, 1)
                        .eq(StudentSkill::getDeleted, 0)
                        .orderByDesc(StudentSkill::getObtainDate)
        ).stream().map(s -> {
            PortraitVO.CertInfo info = new PortraitVO.CertInfo();
            DictCert dc = dictCertMapper.selectById(s.getCertCode());
            info.setName(dc != null ? dc.getName() : "");
            info.setCertNo(s.getCertNo());
            if (s.getObtainDate() != null) info.setObtainDate(s.getObtainDate().toString());
            if (s.getValidUntil() != null) info.setValidUntil(s.getValidUntil().toString());
            info.setPoints(s.getPointsAwarded());
            return info;
        }).collect(Collectors.toList());
        vo.setCerts(certs);

        // ===== 6. 社会服务记录 =====
        List<PortraitVO.ServiceInfo> services = socialServiceRecordMapper.selectList(
                new LambdaQueryWrapper<SocialServiceRecord>()
                        .eq(SocialServiceRecord::getStudentId, studentId)
                        .eq(SocialServiceRecord::getStatus, 1)
                        .eq(SocialServiceRecord::getDeleted, 0)
                        .orderByDesc(SocialServiceRecord::getServiceDate)
        ).stream().map(r -> {
            PortraitVO.ServiceInfo info = new PortraitVO.ServiceInfo();
            info.setTitle(r.getActivityType());
            if (r.getServiceDate() != null) info.setServiceDate(r.getServiceDate().toString());
            info.setHours(r.getDurationHours() != null ? r.getDurationHours().doubleValue() : 0.0);
            info.setPoints(r.getPointsAwarded());
            return info;
        }).collect(Collectors.toList());
        vo.setServices(services);

        // ===== 7. 学业表现 =====
        List<PortraitVO.AcademicInfo> academic = academicPerformanceMapper.selectList(
                new LambdaQueryWrapper<AcademicPerformance>()
                        .eq(AcademicPerformance::getStudentId, studentId)
                        .eq(AcademicPerformance::getDeleted, 0)
                        .orderByDesc(AcademicPerformance::getSemester)
        ).stream().map(a -> {
            PortraitVO.AcademicInfo info = new PortraitVO.AcademicInfo();
            info.setSemester(a.getSemester());
            info.setGpa(a.getGpa() != null ? a.getGpa().doubleValue() : null);
            info.setScholarship(a.getScholarship());
            info.setPoints(a.getPointsAwarded());
            return info;
        }).collect(Collectors.toList());
        vo.setAcademic(academic);

        // ===== 8. 服务意愿 =====
        StudentServiceWillingness willingness = willingnessMapper.selectOne(
                new LambdaQueryWrapper<StudentServiceWillingness>()
                        .eq(StudentServiceWillingness::getStudentId, studentId));
        if (willingness != null) {
            PortraitVO.WillingnessInfo wInfo = new PortraitVO.WillingnessInfo();
            if (willingness.getPostCodes() != null) {
                wInfo.setPostTypes(Arrays.asList(willingness.getPostCodes().split(",")));
            }
            wInfo.setAvailableTime(willingness.getAvailableTimeSlot());
            vo.setWillingness(wInfo);
        }

        // ===== 9. 就业意向 =====
        StudentEmploymentIntention intention = intentionMapper.selectOne(
                new LambdaQueryWrapper<StudentEmploymentIntention>()
                        .eq(StudentEmploymentIntention::getStudentId, studentId));
        if (intention != null) {
            PortraitVO.IntentionInfo iInfo = new PortraitVO.IntentionInfo();
            if (intention.getJobCodes() != null) {
                iInfo.setJobTypes(Arrays.asList(intention.getJobCodes().split(",")));
            }
            iInfo.setExpectedSalary(intention.getUpdateSemester()); // Note: field reuse
            iInfo.setExpectedCity(intention.getAreaPreference());
            vo.setIntention(iInfo);
        }

        return vo;
    }

    private int sumByType(Long studentId, String type) {
        return pointsDetailMapper.selectList(
                new LambdaQueryWrapper<PointsDetail>()
                        .eq(PointsDetail::getStudentId, studentId)
                        .eq(PointsDetail::getReasonType, type)
        ).stream().mapToInt(PointsDetail::getChangeValue).sum();
    }

    private String getPointLevel(int total) {
        if (total >= 100) return "A";
        if (total >= 50) return "B";
        if (total >= 10) return "C";
        return "D";
    }
}
