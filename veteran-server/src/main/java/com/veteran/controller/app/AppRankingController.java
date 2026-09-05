package com.veteran.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.Result;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.vo.RankingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "小程序端-积分排行")
@RestController
@RequestMapping("/app/ranking")
@RequiredArgsConstructor
public class AppRankingController {

    private final StudentMapper studentMapper;
    private final PointsDetailMapper pointsDetailMapper;
    private final SocialServiceRecordMapper recordMapper;
    private final MilitaryHonorMapper honorMapper;
    private final StudentSkillMapper skillMapper;

    @Operation(summary = "积分排行榜(Top)")
    @GetMapping("/top")
    public Result<List<RankingVO>> top(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size) {
        List<RankingVO> all = buildRankingList(null, null);
        int start = (page - 1) * size;
        int end = Math.min(start + size, all.size());
        return Result.ok(start < all.size() ? all.subList(start, end) : Collections.emptyList());
    }

    @Operation(summary = "积分排行榜")
    @GetMapping("/list")
    public Result<Page<RankingVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "20") Integer size,
                                        @RequestParam(required = false) String college,
                                        @RequestParam(required = false) String grade) {
        List<RankingVO> all = buildRankingList(college, grade);

        int start = (page - 1) * size;
        int end = Math.min(start + size, all.size());
        List<RankingVO> paged = start < all.size() ? all.subList(start, end) : Collections.emptyList();

        Page<RankingVO> result = new Page<>(page, size, all.size());
        result.setRecords(paged);
        return Result.ok(result);
    }

    private List<RankingVO> buildRankingList(String college, String grade) {
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StringUtils.hasText(college), Student::getCollege, college)
                .eq(StringUtils.hasText(grade), Student::getGrade, grade);
        List<Student> students = studentMapper.selectList(studentWrapper);

        return students.stream().map(s -> {
            RankingVO vo = new RankingVO();
            vo.setId(s.getId());
            vo.setName(s.getName());
            vo.setStudentNo(s.getStudentNo());
            vo.setCollege(s.getCollege());
            vo.setGrade(s.getGrade());

            LambdaQueryWrapper<PointsDetail> pdWrapper = new LambdaQueryWrapper<>();
            pdWrapper.eq(PointsDetail::getStudentId, s.getId());
            int total = pointsDetailMapper.selectList(pdWrapper).stream()
                    .mapToInt(PointsDetail::getChangeValue).sum();
            vo.setTotalPoints((long) total);

            pdWrapper.eq(PointsDetail::getReasonType, "SERVICE");
            vo.setServicePoints(pointsDetailMapper.selectList(pdWrapper).stream()
                    .mapToInt(PointsDetail::getChangeValue).sum() + 0L);

            LambdaQueryWrapper<PointsDetail> honorPd = new LambdaQueryWrapper<>();
            honorPd.eq(PointsDetail::getStudentId, s.getId()).eq(PointsDetail::getReasonType, "HONOR");
            vo.setHonorPoints(pointsDetailMapper.selectList(honorPd).stream()
                    .mapToInt(PointsDetail::getChangeValue).sum() + 0L);

            LambdaQueryWrapper<PointsDetail> certPd = new LambdaQueryWrapper<>();
            certPd.eq(PointsDetail::getStudentId, s.getId()).eq(PointsDetail::getReasonType, "CERT");
            vo.setCertPoints(pointsDetailMapper.selectList(certPd).stream()
                    .mapToInt(PointsDetail::getChangeValue).sum() + 0L);

            return vo;
        }).sorted((a, b) -> Long.compare(b.getTotalPoints(), a.getTotalPoints()))
                .collect(Collectors.toList());
    }
}