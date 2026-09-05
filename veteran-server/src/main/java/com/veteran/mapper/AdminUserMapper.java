package com.veteran.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.veteran.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}