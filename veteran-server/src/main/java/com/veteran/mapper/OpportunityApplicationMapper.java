package com.veteran.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veteran.entity.OpportunityApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OpportunityApplicationMapper extends BaseMapper<OpportunityApplication> {

    @Select("<script>" +
            "SELECT oa.* FROM opportunity_application oa " +
            "LEFT JOIN opportunity o ON oa.opportunity_id = o.id " +
            "WHERE 1=1 " +
            "<if test='opportunityId != null'> AND oa.opportunity_id = #{opportunityId} </if>" +
            "<if test='status != null'> AND oa.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (o.title LIKE CONCAT('%', #{keyword}, '%') OR oa.student_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY oa.apply_time DESC" +
            "</script>")
    Page<OpportunityApplication> selectApplicationPage(Page<OpportunityApplication> page,
                                                        @Param("opportunityId") Long opportunityId,
                                                        @Param("status") Integer status,
                                                        @Param("keyword") String keyword);
}