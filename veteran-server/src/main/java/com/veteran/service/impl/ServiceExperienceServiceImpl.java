package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.BusinessException;
import com.veteran.common.utils.MonthDateUtil;
import com.veteran.dto.MilitaryHonorDTO;
import com.veteran.dto.ServiceExperienceDTO;
import com.veteran.entity.*;
import com.veteran.mapper.*;
import com.veteran.service.PointsService;
import com.veteran.service.ServiceExperienceService;
import com.veteran.vo.MilitaryHonorVO;
import com.veteran.vo.ServiceExperienceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceExperienceServiceImpl implements ServiceExperienceService {

    private final ServiceExperienceMapper serviceExperienceMapper;
    private final MilitaryHonorMapper militaryHonorMapper;
    private final DictBranchMapper dictBranchMapper;
    private final DictHonorMapper dictHonorMapper;
    private final DictHonorCategoryMapper dictHonorCategoryMapper;
    private final DictLeaderPostMapper dictLeaderPostMapper;
    private final PointsService pointsService;

    private LocalDate ym(String value) {
        return MonthDateUtil.toFirstDay(value);
    }

    @Override
    @Transactional
    public ServiceExperienceVO create(ServiceExperienceDTO dto) {
        ServiceExperience entity = new ServiceExperience();
        BeanUtils.copyProperties(dto, entity);
        entity.setStartDate(ym(dto.getStartDate()));
        entity.setEndDate(ym(dto.getEndDate()));
        serviceExperienceMapper.insert(entity);
        log.info("创建服役经历: id={}, studentId={}", entity.getId(), dto.getStudentId());
        return toVO(entity);
    }

    @Override
    @Transactional
    public ServiceExperienceVO update(Long id, ServiceExperienceDTO dto) {
        ServiceExperience entity = serviceExperienceMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("服役经历不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        entity.setStartDate(ym(dto.getStartDate()));
        entity.setEndDate(ym(dto.getEndDate()));
        serviceExperienceMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServiceExperience entity = serviceExperienceMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("服役经历不存在");
        }
        serviceExperienceMapper.deleteById(id);
        log.info("删除服役经历: id={}", id);
    }

    @Override
    public List<ServiceExperienceVO> listByStudentId(Long studentId) {
        LambdaQueryWrapper<ServiceExperience> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceExperience::getStudentId, studentId)
                .orderByDesc(ServiceExperience::getStartDate);
        return serviceExperienceMapper.selectList(wrapper).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MilitaryHonorVO addHonor(MilitaryHonorDTO dto) {
        Long seId = dto.getServiceExperienceId();
        ServiceExperience se = null;
        if (seId != null) {
            se = serviceExperienceMapper.selectById(seId);
            if (se == null) {
                throw BusinessException.notFound("服役经历不存在");
            }
        }

        DictHonor dictHonor = dictHonorMapper.selectById(dto.getHonorCode());
        if (dictHonor == null) {
            throw BusinessException.notFound("表彰奖励类型不存在");
        }
        if (dictHonorCategoryMapper.selectById(dto.getHonorCategoryCode()) == null) {
            throw BusinessException.notFound("荣誉类别不存在");
        }

        // 未指定服役经历时，自动关联该学生已审核通过的服役经历（若存在）
        if (se == null) {
            if (dto.getStudentId() == null) {
                throw BusinessException.badRequest("请提供学生ID或服役经历ID");
            }
            se = serviceExperienceMapper.selectList(
                    new LambdaQueryWrapper<ServiceExperience>()
                            .eq(ServiceExperience::getStudentId, dto.getStudentId())
                            .eq(ServiceExperience::getStatus, 1)
                            .orderByDesc(ServiceExperience::getStartDate)
                            .last("LIMIT 1")
            ).stream().findFirst().orElse(null);
            if (se == null) {
                throw BusinessException.badRequest("请先提交并审核通过服役经历，再提交受奖情况");
            }
        }

        MilitaryHonor honor = new MilitaryHonor();
        honor.setServiceExperienceId(se != null ? se.getId() : null);
        honor.setHonorCode(dto.getHonorCode());
        honor.setHonorCategoryCode(dto.getHonorCategoryCode());
        honor.setAwardDate(ym(dto.getAwardDate()));
        honor.setPointsAwarded(dictHonor.getDefaultPoints());
        honor.setStatus(0);
        militaryHonorMapper.insert(honor);

        // 不立即发分：统一走审核流程（reviewHonor 通过时发放），避免双次入账
        log.info("添加荣誉: id={}, honorCode={}, category={}, points={}",
                honor.getId(), dto.getHonorCode(), dto.getHonorCategoryCode(), dictHonor.getDefaultPoints());
        return toHonorVO(honor);
    }

    @Override
    @Transactional
    public void removeHonor(Long honorId) {
        MilitaryHonor honor = militaryHonorMapper.selectById(honorId);
        if (honor == null) {
            throw BusinessException.notFound("荣誉不存在");
        }

        ServiceExperience se = honor.getServiceExperienceId() != null
                ? serviceExperienceMapper.selectById(honor.getServiceExperienceId()) : null;
        militaryHonorMapper.deleteById(honorId);

        // 仅已审核通过（已发放积分）的记录撤销时回扣积分；待审记录未发分，删除不扣分
        if (se != null && honor.getStatus() != null && honor.getStatus() == 1 && honor.getPointsAwarded() > 0) {
            pointsService.deductPoints(se.getStudentId(), honor.getPointsAwarded(),
                    "HONOR_REVOKE", honorId, "military_honor", "system",
                    "撤销荣誉积分");
        }

        log.info("删除荣誉: id={}", honorId);
    }

    @Override
    public List<MilitaryHonorVO> listHonors(Long serviceExperienceId) {
        LambdaQueryWrapper<MilitaryHonor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MilitaryHonor::getServiceExperienceId, serviceExperienceId)
                .orderByDesc(MilitaryHonor::getAwardDate);
        return militaryHonorMapper.selectList(wrapper).stream()
                .map(this::toHonorVO).collect(Collectors.toList());
    }

    private ServiceExperienceVO toVO(ServiceExperience entity) {
        ServiceExperienceVO vo = new ServiceExperienceVO();
        BeanUtils.copyProperties(entity, vo);
        DictBranch branch = dictBranchMapper.selectById(entity.getBranchCode());
        if (branch != null) {
            vo.setBranchName(branch.getName());
        }
        if (entity.getLeaderPostCode() != null) {
            DictLeaderPost leaderPost = dictLeaderPostMapper.selectById(entity.getLeaderPostCode());
            if (leaderPost != null) {
                vo.setLeaderPostName(leaderPost.getName());
            }
        }
        return vo;
    }

    private MilitaryHonorVO toHonorVO(MilitaryHonor entity) {
        MilitaryHonorVO vo = new MilitaryHonorVO();
        BeanUtils.copyProperties(entity, vo);
        DictHonor dictHonor = dictHonorMapper.selectById(entity.getHonorCode());
        if (dictHonor != null) {
            vo.setHonorName(dictHonor.getName());
        }
        if (entity.getHonorCategoryCode() != null) {
            DictHonorCategory category = dictHonorCategoryMapper.selectById(entity.getHonorCategoryCode());
            if (category != null) {
                vo.setHonorCategoryName(category.getName());
            }
        }
        return vo;
    }
}