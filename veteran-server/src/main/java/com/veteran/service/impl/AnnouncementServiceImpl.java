package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.dto.AnnouncementDTO;
import com.veteran.entity.Announcement;
import com.veteran.common.BusinessException;
import com.veteran.mapper.AnnouncementMapper;
import com.veteran.service.AnnouncementService;
import com.veteran.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public Page<AnnouncementVO> pageQuery(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Announcement::getTitle, keyword);
        }
        wrapper.orderByDesc(Announcement::getPriority, Announcement::getCreateTime);
        Page<Announcement> entityPage = announcementMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AnnouncementVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public AnnouncementVO getById(Long id) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("公告不存在");
        }
        return toVO(entity);
    }

    @Override
    @Transactional
    public AnnouncementVO create(AnnouncementDTO dto) {
        Announcement entity = new Announcement();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getStatus() == null) entity.setStatus(0);
        if (entity.getPriority() == null) entity.setPriority(0);
        announcementMapper.insert(entity);
        log.info("创建公告: id={}, title={}", entity.getId(), dto.getTitle());
        return toVO(entity);
    }

    @Override
    @Transactional
    public AnnouncementVO update(Long id, AnnouncementDTO dto) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("公告不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        announcementMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("公告不存在");
        }
        announcementMapper.deleteById(id);
        log.info("删除公告: id={}", id);
    }

    @Override
    @Transactional
    public void publish(Long id) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("公告不存在");
        }
        entity.setStatus(1);
        entity.setId(id);
        announcementMapper.updateById(entity);
        log.info("发布公告: id={}", id);
    }

    @Override
    @Transactional
    public void close(Long id) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.notFound("公告不存在");
        }
        entity.setStatus(2);
        entity.setId(id);
        announcementMapper.updateById(entity);
        log.info("关闭公告: id={}", id);
    }

    @Override
    public List<AnnouncementVO> getActiveList() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1);
        wrapper.orderByDesc(Announcement::getPriority, Announcement::getCreateTime);
        List<Announcement> entities = announcementMapper.selectList(wrapper);
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    private AnnouncementVO toVO(Announcement entity) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
