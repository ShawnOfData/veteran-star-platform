package com.veteran.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.dto.AnnouncementDTO;
import com.veteran.vo.AnnouncementVO;

import java.util.List;

public interface AnnouncementService {
    Page<AnnouncementVO> pageQuery(Integer page, Integer size, String keyword);
    AnnouncementVO getById(Long id);
    AnnouncementVO create(AnnouncementDTO dto);
    AnnouncementVO update(Long id, AnnouncementDTO dto);
    void delete(Long id);
    void publish(Long id);
    void close(Long id);
    List<AnnouncementVO> getActiveList();
}
