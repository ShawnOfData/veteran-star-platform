package com.veteran.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.Result;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.PointsService;
import com.veteran.vo.StudentHomeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "小程序端-首页")
@RestController
@RequestMapping("/app/home")
@RequiredArgsConstructor
public class AppHomeController {

    private final PointsService pointsService;
    private final SocialServiceRecordMapper recordMapper;
    private final MilitaryHonorMapper honorMapper;
    private final ServiceExperienceMapper serviceExperienceMapper;
    private final StudentSkillMapper skillMapper;
    private final OpportunityMapper opportunityMapper;
    private final OpportunityApplicationMapper applicationMapper;
    private final StudentMapper studentMapper;
    private final PointsDetailMapper pointsDetailMapper;

    @Operation(summary = "获取学生首页数据")
    @GetMapping("/{studentId}")
    public Result<StudentHomeVO> home(@PathVariable Long studentId) {
        StudentHomeVO vo = new StudentHomeVO();

        vo.setTotalPoints(pointsService.getTotalPoints(studentId));

        LambdaQueryWrapper<SocialServiceRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(SocialServiceRecord::getStudentId, studentId);
        vo.setServiceCount(recordMapper.selectCount(recordWrapper));
        vo.setTotalHours(recordMapper.selectList(recordWrapper).stream()
                .map(r -> r.getDurationHours() != null ? r.getDurationHours().longValue() : 0L)
                .reduce(0L, Long::sum));

        LambdaQueryWrapper<ServiceExperience> seWrapper = new LambdaQueryWrapper<>();
        seWrapper.eq(ServiceExperience::getStudentId, studentId);
        List<Long> seIds = serviceExperienceMapper.selectList(seWrapper).stream()
                .map(ServiceExperience::getId).collect(java.util.stream.Collectors.toList());
        if (!seIds.isEmpty()) {
            LambdaQueryWrapper<MilitaryHonor> honorWrapper = new LambdaQueryWrapper<>();
            honorWrapper.in(MilitaryHonor::getServiceExperienceId, seIds);
            vo.setHonorCount(honorMapper.selectCount(honorWrapper));
        } else {
            vo.setHonorCount(0L);
        }

        LambdaQueryWrapper<StudentSkill> skillWrapper = new LambdaQueryWrapper<>();
        skillWrapper.eq(StudentSkill::getStudentId, studentId);
        vo.setCertCount(skillMapper.selectCount(skillWrapper));

        LambdaQueryWrapper<Opportunity> oppWrapper = new LambdaQueryWrapper<>();
        oppWrapper.eq(Opportunity::getStatus, 1);
        vo.setAvailableOpportunities(opportunityMapper.selectCount(oppWrapper));

        LambdaQueryWrapper<OpportunityApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(OpportunityApplication::getStudentId, studentId);
        vo.setAppliedCount(applicationMapper.selectCount(appWrapper));

        // 最近4条申请记录（含机会标题）
        List<OpportunityApplication> recentApps = applicationMapper.selectList(
                new LambdaQueryWrapper<OpportunityApplication>()
                        .eq(OpportunityApplication::getStudentId, studentId)
                        .orderByDesc(OpportunityApplication::getApplyTime)
                        .last("LIMIT 4")
        );
        List<StudentHomeVO.AppItem> appItems = new ArrayList<>();
        for (OpportunityApplication app : recentApps) {
            StudentHomeVO.AppItem item = new StudentHomeVO.AppItem();
            item.setStatus(app.getStatus());
            if (app.getOpportunityId() != null) {
                Opportunity opp = opportunityMapper.selectById(app.getOpportunityId());
                item.setTitle(opp != null ? opp.getTitle() : "报名申请");
            } else {
                item.setTitle("报名申请");
            }
            appItems.add(item);
        }
        vo.setApplications(appItems);

        int myPoints = vo.getTotalPoints();
        // 排名：从视图计算（student_points_summary）
        vo.setRank(1L);
        try {
            long betterCount = pointsDetailMapper.selectList(null).stream()
                    .filter(d -> d.getStudentId() != null && d.getChangeValue() != null)
                    .collect(Collectors.groupingBy(
                            PointsDetail::getStudentId,
                            Collectors.summingInt(PointsDetail::getChangeValue)))
                    .values().stream().filter(sum -> sum != null && sum > myPoints).count();
            vo.setRank(betterCount + 1);
        } catch (Exception e) {
            // 排名计算失败不影响首页展示
        }

        // 8. 月度积分趋势（近6个月）
        List<PointsDetail> allDetails = pointsDetailMapper.selectList(
                new LambdaQueryWrapper<PointsDetail>()
                        .eq(PointsDetail::getStudentId, studentId)
                        .orderByAsc(PointsDetail::getCreateTime)
        );
        Map<String, Long> monthlyTotals = new LinkedHashMap<>();
        for (PointsDetail d : allDetails) {
            if (d.getCreateTime() != null) {
                String month = d.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthlyTotals.merge(month, (long) d.getChangeValue(), Long::sum);
            }
        }
        List<Map.Entry<String, Long>> entryList = new ArrayList<>(monthlyTotals.entrySet());
        if (entryList.size() > 6) {
            entryList = entryList.subList(entryList.size() - 6, entryList.size());
        }
        List<Long> trend = new ArrayList<>();
        long cum = 0;
        for (Map.Entry<String, Long> entry : entryList) {
            cum += entry.getValue();
            trend.add(cum);
        }
        vo.setPointsTrend(trend);

        // 9. 积分构成（按来源类型）
        Map<String, Long> compRaw = allDetails.stream()
                .filter(d -> d.getChangeValue() > 0)
                .collect(Collectors.groupingBy(
                        PointsDetail::getReasonType,
                        Collectors.summingLong(d -> (long) d.getChangeValue())));
        Map<String, String> typeNameMap = new HashMap<>();
        typeNameMap.put("SERVICE", "社会服务");
        typeNameMap.put("HONOR", "荣誉奖励");
        typeNameMap.put("CERT", "技能证书");
        typeNameMap.put("ADJUST", "管理员调整");
        List<Map<String, Object>> composition = new ArrayList<>();
        compRaw.forEach((type, value) -> {
            if (value > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", typeNameMap.getOrDefault(type, type));
                item.put("value", value);
                composition.add(item);
            }
        });
        vo.setPointsComposition(composition);

        return Result.ok(vo);
    }
}