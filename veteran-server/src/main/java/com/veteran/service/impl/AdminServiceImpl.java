package com.veteran.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veteran.common.BusinessException;
import com.veteran.common.utils.JwtUtil;
import com.veteran.security.LoginTokenManager;
import com.veteran.dto.LoginDTO;
import com.veteran.entity.AdminUser;
import com.veteran.mapper.AdminUserMapper;
import com.veteran.service.AdminService;
import com.veteran.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginTokenManager loginTokenManager;

    @Override
    public LoginVO login(LoginDTO dto) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, dto.getUsername());
        AdminUser admin = adminUserMapper.selectOne(wrapper);

        if (admin == null || !passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername());
        // 写入登录白名单：同管理员账号在其它端登录后，旧端 token 失效（自动登出）
        loginTokenManager.saveAdminToken(admin.getId(), token);

        admin.setLastLogin(LocalDateTime.now());
        adminUserMapper.updateById(admin);

        log.info("管理员登录: username={}, role={}", admin.getUsername(), admin.getRole());
        return LoginVO.of(token, admin.getUsername(), admin.getRole() != null ? admin.getRole() : "admin");
    }
}