package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.common.BusinessException;
import com.veteran.dto.OpportunityDTO;
import com.veteran.entity.Opportunity;
import com.veteran.entity.OpportunityApplication;
import com.veteran.entity.OpportunityFavorite;
import com.veteran.entity.Student;
import com.veteran.mapper.OpportunityApplicationMapper;
import com.veteran.mapper.OpportunityFavoriteMapper;
import com.veteran.mapper.OpportunityMapper;
import com.veteran.mapper.StudentMapper;
import com.veteran.service.OpportunityService;
import com.veteran.vo.OpportunityApplicationVO;
import com.veteran.vo.OpportunityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityMapper opportunityMapper;
    private final OpportunityApplicationMapper applicationMapper;
    private final StudentMapper studentMapper;
    private final OpportunityFavoriteMapper favoriteMapper;

    @Override
    public Page<OpportunityVO> pageQuery(Integer page, Integer size, String type, Integer status, String title) {
        LambdaQueryWrapper<Opportunity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(type), Opportunity::getType, type)
                .eq(status != null, Opportunity::getStatus, status)
                .like(StringUtils.hasText(title), Opportunity::getTitle, title)
                .orderByDesc(Opportunity::getCreateTime);

        Page<Opportunity> pageParam = new Page<>(page, size);
        Page<Opportunity> result = opportunityMapper.selectPage(pageParam, wrapper);

        Page<OpportunityVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<OpportunityVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public OpportunityVO getById(Long id) {
        Opportunity entity = opportunityMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("机会不存在");
        }
        return toVO(entity);
    }

    @Override
    @Transactional
    public OpportunityVO create(OpportunityDTO dto, Long publisherId) {
        Opportunity entity = new Opportunity();
        BeanUtils.copyProperties(dto, entity);
        entity.setPublisherId(publisherId);
        entity.setCurrentApplied(0);
        entity.setStatus(0);
        opportunityMapper.insert(entity);
        log.info("创建机会: id={}, title={}", entity.getId(), dto.getTitle());
        return toVO(entity);
    }

    @Override
    @Transactional
    public OpportunityVO update(Long id, OpportunityDTO dto) {
        Opportunity entity = opportunityMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("机会不存在");
        }
        // 只复制非空字段，避免 null 覆盖数据库现有值
        String[] ignored = getNullPropertyNames(dto);
        BeanUtils.copyProperties(dto, entity, ignored);
        entity.setId(id);
        opportunityMapper.updateById(entity);
        return toVO(entity);
    }

    private String[] getNullPropertyNames(Object source) {
        try {
            java.beans.PropertyDescriptor[] pds = java.beans.Introspector.getBeanInfo(source.getClass())
                    .getPropertyDescriptors();
            java.util.List<String> nullNames = new java.util.ArrayList<>();
            for (java.beans.PropertyDescriptor pd : pds) {
                Object value = pd.getReadMethod().invoke(source);
                if (value == null) {
                    nullNames.add(pd.getName());
                }
            }
            return nullNames.toArray(new String[0]);
        } catch (Exception e) {
            log.error("获取空属性名失败", e);
            return new String[0];
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Opportunity entity = opportunityMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("机会不存在");
        }
        opportunityMapper.deleteById(id);
        log.info("删除机会: id={}", id);
    }

    @Override
    @Transactional
    public void publish(Long id) {
        Opportunity entity = opportunityMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("机会不存在");
        }
        entity.setStatus(1);
        opportunityMapper.updateById(entity);
        log.info("发布机会: id={}", id);
    }

    @Override
    @Transactional
    public void close(Long id) {
        Opportunity entity = opportunityMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("机会不存在");
        }
        entity.setStatus(2);
        opportunityMapper.updateById(entity);
        log.info("关闭机会: id={}", id);
    }

    @Override
    @Transactional
    public void apply(Long opportunityId, Long studentId, String remark) {
        Opportunity opportunity = opportunityMapper.selectById(opportunityId);
        if (opportunity == null || opportunity.getStatus() != 1) {
            throw BusinessException.badRequest("机会不可报名");
        }
        if (opportunity.getDemandCount() != null && opportunity.getCurrentApplied() >= opportunity.getDemandCount()) {
            throw BusinessException.badRequest("报名人数已满");
        }

        // 使用悲观锁检查重复报名，防止并发重复插入
        LambdaQueryWrapper<OpportunityApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpportunityApplication::getOpportunityId, opportunityId)
                .eq(OpportunityApplication::getStudentId, studentId)
                .eq(OpportunityApplication::getStatus, 0);
        if (applicationMapper.selectCount(wrapper) > 0) {
            throw BusinessException.conflict("已报名该机会");
        }

        // Look up student info
        Student student = studentMapper.selectById(studentId);

        OpportunityApplication application = new OpportunityApplication();
        application.setOpportunityId(opportunityId);
        application.setStudentId(studentId);
        application.setApplyTime(LocalDateTime.now());
        application.setStatus(0);
        application.setRemark(remark);
        if (student != null) {
            application.setStudentName(student.getName());
            application.setStudentStudentId(student.getStudentNo());
            application.setStudentPhone(student.getPhone());
        }
        applicationMapper.insert(application);

        // 原子更新报名人数，防止并发超报
        int updated = opportunityMapper.updateAppliedCount(opportunityId, 1, opportunity.getDemandCount());
        if (updated == 0) {
            // 名额已满，回滚报名记录
            applicationMapper.deleteById(application.getId());
            throw BusinessException.badRequest("报名人数已满");
        }

        log.info("报名机会: opportunityId={}, studentId={}", opportunityId, studentId);
    }

    @Override
    @Transactional
    public void cancelApply(Long applicationId, Long studentId) {
        OpportunityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw BusinessException.notFound("报名记录不存在");
        }
        if (!application.getStudentId().equals(studentId)) {
            throw BusinessException.forbidden("无权取消他人的报名");
        }
        application.setStatus(1);
        applicationMapper.updateById(application);
        log.info("取消报名: applicationId={}, studentId={}", applicationId, studentId);
    }

    @Override
    public Page<OpportunityVO> listPublished(Integer page, Integer size, String type, String keyword, Long studentId) {
        LambdaQueryWrapper<Opportunity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opportunity::getStatus, 1)
                .eq(StringUtils.hasText(type), Opportunity::getType, type)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Opportunity::getTitle, keyword)
                        .or().like(Opportunity::getDescription, keyword)
                        .or().like(Opportunity::getUnitName, keyword))
                .orderByDesc(Opportunity::getPriority)
                .orderByDesc(Opportunity::getCreateTime);

        Page<Opportunity> pageParam = new Page<>(page, size);
        Page<Opportunity> result = opportunityMapper.selectPage(pageParam, wrapper);

        Page<OpportunityVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<OpportunityVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());

        // 如果传入了 studentId，查询该学生的收藏列表并标记 isFavorited
        if (studentId != null && !voList.isEmpty()) {
            List<Long> oppIds = voList.stream().map(OpportunityVO::getId).collect(Collectors.toList());
            LambdaQueryWrapper<OpportunityFavorite> favWrapper = new LambdaQueryWrapper<>();
            favWrapper.eq(OpportunityFavorite::getStudentId, studentId)
                    .in(OpportunityFavorite::getOpportunityId, oppIds);
            List<OpportunityFavorite> favorites = favoriteMapper.selectList(favWrapper);
            java.util.Set<Long> favoritedIds = favorites.stream()
                    .map(OpportunityFavorite::getOpportunityId)
                    .collect(Collectors.toSet());
            for (OpportunityVO vo : voList) {
                if (favoritedIds.contains(vo.getId())) {
                    vo.setIsFavorited(true);
                }
            }
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<OpportunityApplicationVO> getApplicationPage(Integer page, Integer size, Long opportunityId, Integer status, String keyword) {
        Page<OpportunityApplication> pageParam = new Page<>(page, size);
        Page<OpportunityApplication> result = applicationMapper.selectApplicationPage(pageParam, opportunityId, status, keyword);

        Page<OpportunityApplicationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        if (result.getRecords().isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        // Get opportunity titles
        List<Long> oppIds = result.getRecords().stream().map(OpportunityApplication::getOpportunityId).distinct().collect(Collectors.toList());
        Map<Long, String> oppTitleMap;
        if (!oppIds.isEmpty()) {
            List<Opportunity> opportunities = opportunityMapper.selectBatchIds(oppIds);
            oppTitleMap = opportunities.stream().collect(Collectors.toMap(Opportunity::getId, Opportunity::getTitle));
        } else {
            oppTitleMap = Collections.emptyMap();
        }

        List<OpportunityApplicationVO> voList = result.getRecords().stream()
                .map(app -> {
                    OpportunityApplicationVO vo = new OpportunityApplicationVO();
                    BeanUtils.copyProperties(app, vo);
                    vo.setOpportunityTitle(oppTitleMap.getOrDefault(app.getOpportunityId(), ""));
                    return vo;
                }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void reviewApply(Long applicationId, Integer status) {
        OpportunityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw BusinessException.notFound("报名记录不存在");
        }
        application.setStatus(status);
        applicationMapper.updateById(application);
        log.info("审核报名: applicationId={}, status={}", applicationId, status);
    }

    @Override
    public Page<OpportunityApplicationVO> getMyApplications(Long studentId, Integer page, Integer size) {
        LambdaQueryWrapper<OpportunityApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpportunityApplication::getStudentId, studentId)
                .orderByDesc(OpportunityApplication::getApplyTime);

        Page<OpportunityApplication> pageParam = new Page<>(page, size);
        Page<OpportunityApplication> result = applicationMapper.selectPage(pageParam, wrapper);

        Page<OpportunityApplicationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        if (result.getRecords().isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        // Get opportunity details
        List<Long> oppIds = result.getRecords().stream().map(OpportunityApplication::getOpportunityId).distinct().collect(Collectors.toList());
        List<Opportunity> opportunities = opportunityMapper.selectBatchIds(oppIds);
        Map<Long, Opportunity> oppMap = opportunities.stream().collect(Collectors.toMap(Opportunity::getId, op -> op));

        List<OpportunityApplicationVO> voList = result.getRecords().stream().map(app -> {
            OpportunityApplicationVO vo = new OpportunityApplicationVO();
            BeanUtils.copyProperties(app, vo);
            Opportunity opp = oppMap.get(app.getOpportunityId());
            if (opp != null) {
                vo.setOpportunityTitle(opp.getTitle());
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void favorite(Long studentId, Long opportunityId) {
        // Check for duplicates
        LambdaQueryWrapper<OpportunityFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpportunityFavorite::getStudentId, studentId)
                .eq(OpportunityFavorite::getOpportunityId, opportunityId);
        if (favoriteMapper.selectCount(wrapper) > 0) {
            throw BusinessException.conflict("已收藏该机会");
        }

        OpportunityFavorite favorite = new OpportunityFavorite();
        favorite.setStudentId(studentId);
        favorite.setOpportunityId(opportunityId);
        favoriteMapper.insert(favorite);
        log.info("收藏机会: studentId={}, opportunityId={}", studentId, opportunityId);
    }

    @Override
    @Transactional
    public void unfavorite(Long studentId, Long opportunityId) {
        LambdaQueryWrapper<OpportunityFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpportunityFavorite::getStudentId, studentId)
                .eq(OpportunityFavorite::getOpportunityId, opportunityId);
        favoriteMapper.delete(wrapper);
        log.info("取消收藏: studentId={}, opportunityId={}", studentId, opportunityId);
    }

    @Override
    public Page<OpportunityVO> getMyFavorites(Long studentId, Integer page, Integer size) {
        LambdaQueryWrapper<OpportunityFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpportunityFavorite::getStudentId, studentId)
                .orderByDesc(OpportunityFavorite::getCreateTime);

        Page<OpportunityFavorite> pageParam = new Page<>(page, size);
        Page<OpportunityFavorite> result = favoriteMapper.selectPage(pageParam, wrapper);

        List<Long> oppIds = result.getRecords().stream().map(OpportunityFavorite::getOpportunityId).collect(Collectors.toList());

        Page<OpportunityVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        if (oppIds.isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        List<Opportunity> opportunities = opportunityMapper.selectBatchIds(oppIds);
        List<OpportunityVO> voList = opportunities.stream().map(op -> {
            OpportunityVO vo = toVO(op);
            vo.setIsFavorited(true);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        Opportunity entity = opportunityMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("机会不存在");
        }
        entity.setViewCount(entity.getViewCount() == null ? 1 : entity.getViewCount() + 1);
        opportunityMapper.updateById(entity);
        log.info("增加浏览次数: id={}", id);
    }

    private OpportunityVO toVO(Opportunity entity) {
        OpportunityVO vo = new OpportunityVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setApplyCount(entity.getCurrentApplied());
        return vo;
    }
}
