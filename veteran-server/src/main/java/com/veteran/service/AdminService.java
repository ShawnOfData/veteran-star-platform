package com.veteran.service;

import com.veteran.dto.LoginDTO;
import com.veteran.vo.LoginVO;

public interface AdminService {

    LoginVO login(LoginDTO dto);
}