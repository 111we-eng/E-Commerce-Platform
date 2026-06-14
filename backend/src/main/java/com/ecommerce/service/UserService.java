package com.ecommerce.service;

import com.ecommerce.domain.User;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.RegisterRequest;

public interface UserService {
    /**
     * 用户登录，返回包含 JWT Token 的响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    User register(RegisterRequest request);

    /**
     * 根据 ID 获取用户
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
}
