package com.veteran.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.veteran.common.Result;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "管理端-数据看板")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final StudentMapper studentMapper;
    private final SocialServiceRecordMapper recordMapper;
    private final PointsDetailMapper pointsDetailMapper;

    @Operation(summary = "获取看板统计数据")
    @GetMapping("/stats")
    public Result<DashboardVO> stats() {
        DashboardVO vo = new DashboardVO();

        // 1. 学生总数
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        vo.setTotalStudents(studentMapper.selectCount(studentWrapper));

        // 2. 在校学生数
        LambdaQueryWrapper<Student> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Student::getStatus, 1);
        vo.setActiveStudents(studentMapper.selectCount(activeWrapper));

        // 3. 积分数据（中位数、最高分、分布）
        List<Long> allPoints = pointsDetailMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(PointsDetail::getStudentId, Collectors.summingLong(PointsDetail::getChangeValue)))
                .values().stream().sorted().collect(Collectors.toList());
        if (!allPoints.isEmpty()) {
            int mid = allPoints.size() / 2;
            vo.setMedianPoints(allPoints.get(mid));
            vo.setMaxPoints(allPoints.get(allPoints.size() - 1));
        } else {
            vo.setMedianPoints(0L);
            vo.setMaxPoints(0L);
        }

        // 4. 服务次数
        LambdaQueryWrapper<SocialServiceRecord> recordWrapper = new LambdaQueryWrapper<>();
        vo.setTotalServices(recordMapper.selectCount(recordWrapper));

        LambdaQueryWrapper<SocialServiceRecord> monthWrapper = new LambdaQueryWrapper<>();
        LocalDate now = LocalDate.now();
        monthWrapper.ge(SocialServiceRecord::getServiceDate, now.withDayOfMonth(1))
                .le(SocialServiceRecord::getServiceDate, now);
        vo.setMonthlyServices(recordMapper.selectCount(monthWrapper));

        // 5. 待审核记录
        LambdaQueryWrapper<SocialServiceRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(SocialServiceRecord::getStatus, 0);
        vo.setPendingReviews(recordMapper.selectCount(pendingWrapper));

        // 6. 积分分布（各分数段人数）
        long[] bins = new long[6]; // 0-20, 21-40, 41-60, 61-80, 81-100, 100+
        for (long points : allPoints) {
            if (points <= 20) bins[0]++;
            else if (points <= 40) bins[1]++;
            else if (points <= 60) bins[2]++;
            else if (points <= 80) bins[3]++;
            else if (points <= 100) bins[4]++;
            else bins[5]++;
        }
        vo.setPointsDistribution(Arrays.stream(bins).boxed().collect(Collectors.toList()));

        // 7. 各学院退役人数分布
        List<Map<String, Object>> collegeRows = studentMapper.selectMaps(
                new QueryWrapper<Student>()
                        .select("COALESCE(college, '未知') as name, COUNT(*) as value")
                        .groupBy("college")
                        .orderByDesc("value")
        );
        vo.setCollegeDistribution(collegeRows);

        return Result.ok(vo);
    }
}