package com.ecommerce.controller;

import com.ecommerce.domain.User;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.dto.Result;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录 — 返回 JWT Token
     *
     * 流程：
     * 1. 接收 {username, password}
     * 2. UserService 验证用户名密码
     * 3. 调用 JwtUtil.generateToken() 生成 Token
     * 4. 返回 {token, userId, username, role}
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.ok("登录成功", response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        // 注册成功不返回密码
        user.setPassword(null);
        return Result.ok("注册成功", user);
    }

    /**
     * 获取当前用户信息（需要 Token 认证）
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.ok(user);
    }
}
