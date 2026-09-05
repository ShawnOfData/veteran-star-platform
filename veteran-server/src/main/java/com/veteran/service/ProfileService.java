package com.veteran.service;

import com.veteran.vo.PortraitVO;

/**
 * 学生画像服务 - 聚合学生全量数据
 */
public interface ProfileService {

    /**
     * 获取学生综合画像
     */
    PortraitVO getPortrait(Long studentId);
}
