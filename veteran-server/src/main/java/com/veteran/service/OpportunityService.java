package com.veteran.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.dto.OpportunityDTO;
import com.veteran.vo.OpportunityApplicationVO;
import com.veteran.vo.OpportunityVO;

public interface OpportunityService {

    Page<OpportunityVO> pageQuery(Integer page, Integer size, String type, Integer status, String title);

    OpportunityVO getById(Long id);

    OpportunityVO create(OpportunityDTO dto, Long publisherId);

    OpportunityVO update(Long id, OpportunityDTO dto);

    void delete(Long id);

    void publish(Long id);

    void close(Long id);

    void apply(Long opportunityId, Long studentId, String remark);

    void cancelApply(Long applicationId, Long studentId);

    Page<OpportunityVO> listPublished(Integer page, Integer size, String type, String keyword, Long studentId);

    Page<OpportunityApplicationVO> getApplicationPage(Integer page, Integer size, Long opportunityId, Integer status, String keyword);

    void reviewApply(Long applicationId, Integer status);

    Page<OpportunityApplicationVO> getMyApplications(Long studentId, Integer page, Integer size);

    void favorite(Long studentId, Long opportunityId);

    void unfavorite(Long studentId, Long opportunityId);

    Page<OpportunityVO> getMyFavorites(Long studentId, Integer page, Integer size);

    void incrementViewCount(Long id);
}
