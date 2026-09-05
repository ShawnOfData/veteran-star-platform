package com.veteran.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色: super=超级管理员, admin=普通管理员")
    private String role;

    public static LoginVO of(String token, String username, String role) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUsername(username);
        vo.setRole(role);
        return vo;
    }
}