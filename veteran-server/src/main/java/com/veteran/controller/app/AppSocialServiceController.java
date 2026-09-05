package com.veteran.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.Result;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.vo.PointsDetailVO;
import com.veteran.vo.SocialServiceRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "小程序端-社会服务")
@RestController
@RequestMapping("/app/social")
@RequiredArgsConstructor
public class AppSocialServiceController {

    private final SocialServiceRecordMapper recordMapper;
    private final StudentMapper studentMapper;
    private final PointsDetailMapper pointsDetailMapper;

    @Operation(summary = "查询学生社会服务记录")
    @GetMapping("/record/list/{studentId}")
    public Result<List<SocialServiceRecord>> listRecords(@PathVariable Long studentId) {
        LambdaQueryWrapper<SocialServiceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SocialServiceRecord::getStudentId, studentId)
                .orderByDesc(SocialServiceRecord::getServiceDate);
        return Result.ok(recordMapper.selectList(wrapper));
    }

    @Operation(summary = "查询学生积分明细")
    @GetMapping("/points/{studentId}")
    public Result<List<PointsDetailVO>> listPoints(@PathVariable Long studentId) {
        LambdaQueryWrapper<PointsDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsDetail::getStudentId, studentId)
                .orderByDesc(PointsDetail::getCreateTime);
        return Result.ok(pointsDetailMapper.selectList(wrapper).stream().map(d -> {
            PointsDetailVO vo = new PointsDetailVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).collect(Collectors.toList()));
    }
}