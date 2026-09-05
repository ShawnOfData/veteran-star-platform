package com.veteran.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.veteran.entity.Opportunity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OpportunityMapper extends BaseMapper<Opportunity> {

    /**
     * 原子更新报名人数，仅在未超过名额限制时递增
     * @param id 机会ID
     * @param increment 递增数量
     * @param maxCount 名额上限（null表示不限制）
     * @return 影响行数（0表示名额已满）
     */
    @Update("UPDATE opportunity SET current_applied = current_applied + #{increment} " +
            "WHERE id = #{id} AND (current_applied + #{increment} <= #{maxCount} OR #{maxCount} IS NULL)")
    int updateAppliedCount(Long id, int increment, Integer maxCount);
}