package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.BusinessException;
import com.veteran.entity.PointsDetail;
import com.veteran.mapper.PointsDetailMapper;
import com.veteran.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private static final String POINTS_CACHE_KEY = "veteran:points:";
    private static final String POINTS_LOCK_KEY = "veteran:points:lock:";
    private static final long LOCK_TIMEOUT = 5;

    private final PointsDetailMapper pointsDetailMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public int getTotalPoints(Long studentId) {
        String cacheKey = POINTS_CACHE_KEY + studentId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return ((Number) cached).intValue();
        }

        int total = calculateTotal(studentId);
        redisTemplate.opsForValue().set(cacheKey, total, 30, TimeUnit.MINUTES);
        return total;
    }

    @Override
    @Transactional
    public void addPoints(Long studentId, int points, String reasonType, Long relatedId,
                          String sourceTable, String operator, String remark) {
        if (points <= 0) {
            return;
        }
        insertPointsDetail(studentId, points, reasonType, relatedId, sourceTable, operator, remark);
        refreshPointsCache(studentId);
        log.info("增加积分: studentId={}, points={}, reasonType={}", studentId, points, reasonType);
    }

    @Override
    @Transactional
    public void deductPoints(Long studentId, int points, String reasonType, Long relatedId,
                             String sourceTable, String operator, String remark) {
        if (points <= 0) {
            return;
        }
        insertPointsDetail(studentId, -points, reasonType, relatedId, sourceTable, operator, remark);
        refreshPointsCache(studentId);
        log.info("扣减积分: studentId={}, points={}, reasonType={}", studentId, points, reasonType);
    }

    @Override
    public void refreshPointsCache(Long studentId) {
        String cacheKey = POINTS_CACHE_KEY + studentId;
        redisTemplate.delete(cacheKey);
        int total = calculateTotal(studentId);
        redisTemplate.opsForValue().set(cacheKey, total, 30, TimeUnit.MINUTES);
    }

    private void insertPointsDetail(Long studentId, int changeValue, String reasonType,
                                    Long relatedId, String sourceTable, String operator, String remark) {
        PointsDetail detail = new PointsDetail();
        detail.setStudentId(studentId);
        detail.setChangeValue(changeValue);
        detail.setReasonType(reasonType);
        detail.setRelatedId(relatedId);
        detail.setSourceTable(sourceTable);
        detail.setOperator(operator);
        detail.setRemark(remark);
        pointsDetailMapper.insert(detail);
    }

    private int calculateTotal(Long studentId) {
        LambdaQueryWrapper<PointsDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsDetail::getStudentId, studentId)
                .select(PointsDetail::getChangeValue);
        Integer sum = pointsDetailMapper.selectList(wrapper).stream()
                .mapToInt(PointsDetail::getChangeValue)
                .sum();
        return sum;
    }
}